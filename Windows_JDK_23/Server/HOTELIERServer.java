package Server;

import Oggetti_Fondamentali.Hotel;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HOTELIERServer {

    private static int PORTA_SERVER;
    private static String IP_MULTICAST;
    private static int PORTA_MULTICAST;
    private static int INTERVALLO_AGGIORNAMENTO_CLASSIFICA;
    private ExecutorService poolThread;
    private MulticastSocket socketMulticast;
    private InetAddress indirizzoMulticast;
    private Map<Socket, String> utentiLoggati= new ConcurrentHashMap<>() ;
    private Database_utenti databaseUtenti;
    private Database_hotels databaseHotels;
    private Map<String, String> primoHotelPerCittà = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler; // Aggiungo lo scheduler qui

    // Metodo per aggiungere il timestamp ai log
    private void logConTimestamp(String messaggio) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("[" + timestamp + "] " + messaggio);
    }

    public HOTELIERServer() throws IOException {
        caricaConfigurazione();
        databaseUtenti = new Database_utenti("./Server/utenti.json");
        logConTimestamp("Database utenti caricato.");
        databaseHotels = new Database_hotels("./Server/hotels.json");
        logConTimestamp("Database hotel caricato.");
        poolThread = Executors.newCachedThreadPool();
        socketMulticast = new MulticastSocket();
        indirizzoMulticast = InetAddress.getByName(IP_MULTICAST);
        scheduler = Executors.newScheduledThreadPool(1);  // Inizializziamo il scheduler

        // Inizializza la mappa dei primi hotel per ogni città
        inizializzaMappaPrimoHotel();

        // Aggiungi il shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logConTimestamp("Chiusura del server in corso...");
            chiudiRisorse();
        }));
    }

    private void chiudiRisorse() {
        // Chiudi il socket multicast
        if (socketMulticast != null && !socketMulticast.isClosed()) {
            socketMulticast.close(); // Questo può generare IOException
            logConTimestamp("Socket multicast chiuso correttamente.");
        }

        // Ferma il thread pool
        if (poolThread != null) {
            poolThread.shutdown(); // Inizia la chiusura del thread pool
            try {
                if (!poolThread.awaitTermination(60, TimeUnit.SECONDS)) {
                    poolThread.shutdownNow(); // Forza la chiusura se non termina
                    logConTimestamp("Thread pool forzato a chiudere.");
                } else {
                    logConTimestamp("Thread pool chiuso correttamente.");
                }
            } catch (InterruptedException e) {
                poolThread.shutdownNow(); // Forza la chiusura se viene interrotto
                logConTimestamp("Thread pool interrotto durante l'attesa di chiusura.");
            }
        }
    }
    

    // Metodo per inizializzare la mappa del primo hotel in ogni città
    private void inizializzaMappaPrimoHotel() {
        for (String città : databaseHotels.getAllCities()) {
            List<Hotel> hotelInCittà = databaseHotels.trovaHotelsPerCittaOrdinatiPerRanking(città);
            if (!hotelInCittà.isEmpty()) {
                primoHotelPerCittà.put(città, hotelInCittà.get(0).getName());
                logConTimestamp("Primo hotel in " + città + ": " + hotelInCittà.get(0).getName());
            }
        }
    }

    private void caricaConfigurazione() {
    Properties properties = new Properties();

    try (InputStream input = new FileInputStream("./Server/Config.properties")) {
        properties.load(input);
        logConTimestamp("Caricamento configurazione...");

        // Porta del server
        try {
            PORTA_SERVER = Integer.parseInt(properties.getProperty("server_port"));
            logConTimestamp("PORTA_SERVER: " + PORTA_SERVER);
        } catch (NumberFormatException e) {
            logConTimestamp("Errore: Porta del server non valida, utilizzo il valore di default: 8080");
            PORTA_SERVER = 8080; // Valore di default in caso di errore
        }

        // IP multicast
        IP_MULTICAST = properties.getProperty("MCAST_IP");
        if (IP_MULTICAST == null || IP_MULTICAST.isEmpty()) {
            logConTimestamp("Errore: IP multicast non valido, utilizzo il valore di default: 224.0.0.1");
            IP_MULTICAST = "224.0.0.1"; // Valore di default in caso di errore
        }
        logConTimestamp("IP_MULTICAST: " + IP_MULTICAST);

        // Porta multicast
        try {
            PORTA_MULTICAST = Integer.parseInt(properties.getProperty("MCAST_PORT"));
            logConTimestamp("PORTA_MULTICAST: " + PORTA_MULTICAST);
        } catch (NumberFormatException e) {
            logConTimestamp("Errore: Porta multicast non valida, utilizzo il valore di default: 5000");
            PORTA_MULTICAST = 5000; // Valore di default in caso di errore
        }

        // Intervallo aggiornamento classifica
        try {
            INTERVALLO_AGGIORNAMENTO_CLASSIFICA = Integer.parseInt(properties.getProperty("ranking_update_interval"));
            logConTimestamp("INTERVALLO_AGGIORNAMENTO_CLASSIFICA: " + INTERVALLO_AGGIORNAMENTO_CLASSIFICA);
        } catch (NumberFormatException e) {
            logConTimestamp("Errore: Intervallo aggiornamento classifica non valido, utilizzo il valore di default: 60");
            INTERVALLO_AGGIORNAMENTO_CLASSIFICA = 60; // Valore di default in caso di errore
        }

    } catch (IOException e) {
        logConTimestamp("Errore durante il caricamento del file di configurazione: " + e.getMessage());
        // Imposta valori di default in caso di errore di I/O
        PORTA_SERVER = 8080;
        IP_MULTICAST = "224.0.0.1";
        PORTA_MULTICAST = 5000;
        INTERVALLO_AGGIORNAMENTO_CLASSIFICA = 60;
    }
}



    public void avvia() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORTA_SERVER)) {
            logConTimestamp("Server in attesa di connessioni sulla porta " + PORTA_SERVER + "...");
            avviaNotifierMulticast();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logConTimestamp("Connessione accettata da: " + clientSocket.getRemoteSocketAddress());
                poolThread.execute(new ClientHandler(clientSocket, databaseUtenti, utentiLoggati, databaseHotels));
            }
        } finally {
            shutdown();  // Assicura che il server chiuda correttamente le risorse
        }
    }
//Notifier Multiscast
    private void avviaNotifierMulticast() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                aggiornaClassificheEInviaNotifiche();
            } catch (IOException e) {
                logConTimestamp("Errore durante l'aggiornamento delle classifiche: " + e.getMessage());
            }
        }, 0, INTERVALLO_AGGIORNAMENTO_CLASSIFICA, TimeUnit.SECONDS);
    }

    // Metodo per aggiornare le classifiche e inviare notifiche se necessario
    private void aggiornaClassificheEInviaNotifiche() throws IOException {
        logConTimestamp("Aggiornamento delle classifiche degli hotel...");

        // Itera su tutte le città gestite nel database degli hotel
        for (String città : databaseHotels.getAllCities()) {
            List<Hotel> hotelInCittà = databaseHotels.trovaHotelsPerCittaOrdinatiPerRanking(città);

            if (!hotelInCittà.isEmpty()) {
                String primoHotelCorrente = hotelInCittà.get(0).getName().trim();
                String primoHotelPrecedente = primoHotelPerCittà.getOrDefault(città, "").trim();

                // Controlla se il primo classificato è cambiato
                if (!primoHotelCorrente.equals(primoHotelPrecedente)) {
                    primoHotelPerCittà.put(città, primoHotelCorrente);

                    // Costruisci e invia il messaggio di notifica
                    String messaggio = "Primo classificato a " + città + " è " + primoHotelCorrente + "             "; //Non capisco perche senza spazio mi trova il numero dell'hotel , empiricamente funziona

                    DatagramPacket pacchetto = new DatagramPacket(
                            messaggio.getBytes(StandardCharsets.UTF_8),
                            messaggio.length(),
                            indirizzoMulticast,
                            PORTA_MULTICAST
                    );

                    socketMulticast.send(pacchetto);
                    logConTimestamp("Notifica inviata ai client: " + messaggio);
                } else {
                    logConTimestamp("Nessun cambiamento nel ranking per la città: " + città);
                }
            } else {
                logConTimestamp("Nessun hotel disponibile per la città: " + città);
            }
        }

    }

    // Metodo per chiudere tutte le risorse
    private void shutdown() {
        logConTimestamp("Chiusura del server in corso...");

        // Chiusura del thread pool
        poolThread.shutdown();
        try {
            if (!poolThread.awaitTermination(60, TimeUnit.SECONDS)) {
                poolThread.shutdownNow();
            }
        } catch (InterruptedException ex) {
            poolThread.shutdownNow();
        }

        // Chiusura del scheduler
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException ex) {
            scheduler.shutdownNow();
        }

        // Chiude il socket multicast
        if (socketMulticast != null && !socketMulticast.isClosed()) {
            socketMulticast.close();
            logConTimestamp("Socket multicast chiuso.");
        }

        logConTimestamp("Server chiuso correttamente.");
    }

    public static void main(String[] args) {
        try {
            HOTELIERServer server = new HOTELIERServer();
            server.avvia();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
