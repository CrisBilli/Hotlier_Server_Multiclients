package Client;

import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HOTELIERCustomerClient {

    private String indirizzoServer;
    private int portaServer;
    private String IPMulticast;
    private int portaMulticast;
    private Socket socket;
    private BufferedReader lettore;
    private PrintWriter scrittore;
    private Scanner scanner = new Scanner(System.in);
    private Condivisore condivisore;
    private Thread threadCondivisore;
    private Queue<String> codaNotifiche = new ConcurrentLinkedQueue<>();


    // Costruttore che carica le configurazioni dal file Config.properties e avvia la connessione multicast
    public HOTELIERCustomerClient() throws IOException {
        caricaConfigurazione();  // Legge il file di configurazione
        connettiAlServer();  // Stabilisce la connessione con il server
        condivisore = new Condivisore(portaMulticast, IPMulticast);  // Crea un'istanza del ricevitore multicast
        threadCondivisore = new Thread(condivisore);  // Avvia il thread per la ricezione delle notifiche
        threadCondivisore.start();
    }

    // Metodo per caricare la configurazione dal file Config.properties
    private void caricaConfigurazione() throws IOException {
        InputStream input = new FileInputStream(System.getProperty("user.dir") + File.separator + "./Client/Config.properties");
        Properties properties = new Properties();
        properties.load(input);
        indirizzoServer = properties.getProperty("server_address");
        portaServer = Integer.parseInt(properties.getProperty("server_port"));
        IPMulticast = properties.getProperty("MCAST_IP");
        portaMulticast = Integer.parseInt(properties.getProperty("MCAST_PORT"));
    }

    // Metodo per connettersi al server
    private void connettiAlServer() {
        try {
            socket = new Socket(indirizzoServer, portaServer);
            lettore = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            scrittore = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connessione al server " + indirizzoServer + ":" + portaServer + " riuscita.");
        } catch (IOException e) {
            System.err.println("Errore durante la connessione al server - Server probabilmente non avviato: " + e.getMessage());
            System.exit(1);
        }
    }

    // Metodo per chiudere le risorse (socket, lettori e scrittori)
    private void chiudiRisorse() {
        try {
            if (lettore != null) {
                lettore.close();
            }
            if (scrittore != null) {
                scrittore.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Errore durante la chiusura delle risorse: " + e.getMessage());
        }
    }

    // Metodo per gestire l'interazione CLI con l'utente
    public void avviaCLI() {
        while (true) {
            // Controlla se ci sono notifiche da mostrare dopo ogni comando
            Queue<String> notifiche = condivisore.ottieniNotifiche();
            if (!notifiche.isEmpty()) {
                // Stampa tutte le notifiche accumulate
                for (String notifica : notifiche) {
                    System.out.println("\033[0;33m+--------------------------------------------------+");
                    System.out.println("|                  NOTIFICA                        |");
                    System.out.println("+--------------------------------------------------+");
                    System.out.println("| " + notifica);
                    System.out.println("+--------------------------------------------------+\033[0m");
                }
            }

            // Visualizzazione del menu principale
            System.out.println("+--------------------------------------------------+");
            System.out.println("|           \033[0;32mHOTELIER - Menu Principale\033[0m             |");
            System.out.println("+--------------------------------------------------+");
            System.out.println("| 1. \033[0;34mRegistrati\033[0m                                    |");
            System.out.println("| 2. \033[0;34mLogin\033[0m                                         |");
            System.out.println("| 3. \033[0;34mLogout\033[0m                                        |");
            System.out.println("| 4. \033[0;34mCerca Hotel\033[0m                                   |");
            System.out.println("| 5. \033[0;34mCerca tutti gli hotel in una città\033[0m            |");
            System.out.println("| 6. \033[0;34mInserisci una recensione\033[0m                      |");
            System.out.println("| 7. \033[0;34mMostra i miei distintivi\033[0m                      |");
            System.out.println("| 8. \033[0;31mEsci\033[0m                                          |");
            System.out.println("+--------------------------------------------------+");
            System.out.print("Seleziona un'opzione: ");

            String scelta = scanner.nextLine();

            // Gestione delle scelte dell'utente
            switch (scelta) {
                case "1":
                    registrati();
                    break;
                case "2":
                    login();
                    break;
                case "3":
                    logout();
                    break;
                case "4":
                    cercaHotel();
                    break;
                case "5":
                    cercaTuttiGliHotel();
                    break;
                case "6":
                    inserisciRecensione();
                    break;
                case "7":
                    mostraDistintivi();
                    break;
                case "8":
                    chiudiRisorse();
                    System.exit(0);
                default:
                    System.out.println("\033[0;33mScelta non valida, riprova.\033[0m");
            }
        }
    }

    // Metodo per mostrare i distintivi dell'utente
    private void mostraDistintivi() {
        scrittore.println("SHOW_MY_BADGES");
        try {
            String risposta = lettore.readLine();
            System.out.println("\033[0;35mDistintivo: " + risposta + "\033[0m");
        } catch (IOException e) {
            System.err.println("Errore durante la lettura della risposta: " + e.getMessage());
        }
    }

    // Metodo per l'inserimento di una recensione
    private void inserisciRecensione() {
        System.out.print("Inserisci nome dell'hotel: ");
        String nomeHotel = scanner.nextLine().trim();
        System.out.print("Inserisci città dell'hotel: ");
        String citta = scanner.nextLine().trim();

        // Inserisci i punteggi per le varie categorie
        System.out.print("Inserisci punteggio per la posizione (0-5): ");
        int punteggioPosizione = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Inserisci punteggio per la pulizia (0-5): ");
        int punteggioPulizia = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Inserisci punteggio per il servizio (0-5): ");
        int punteggioServizio = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Inserisci punteggio per il prezzo (0-5): ");
        int punteggioPrezzo = Integer.parseInt(scanner.nextLine().trim());

        // Calcola il punteggio globale come media dei punteggi delle categorie
        double punteggioGlobale = (punteggioPosizione + punteggioPulizia + punteggioServizio + punteggioPrezzo) / 4.0;

        // Invia i dati della recensione al server
        scrittore.println("INSERT_REVIEW&" + nomeHotel + "&" + citta + "&" + punteggioGlobale + "&" + punteggioPosizione + "&" + punteggioPulizia + "&" + punteggioServizio + "&" + punteggioPrezzo);

        try {
            String risposta = lettore.readLine();
            System.out.println("\033[0;35mRisposta del server: " + risposta + "\033[0m");
        } catch (IOException e) {
            System.err.println("Errore durante la lettura della risposta: " + e.getMessage());
        }
    }

    // Metodo per la registrazione
    private void registrati() {
        System.out.print("Inserisci username: ");
        String username = scanner.nextLine();
        System.out.print("Inserisci password: ");
        String password = scanner.nextLine();
        scrittore.println("REGISTER&" + username + "&" + password);
        try {
            String risposta = lettore.readLine();
            System.out.println("\033[0;35mRisposta del server: " + risposta + "\033[0m");

        } catch (IOException e) {
            System.err.println("Errore durante la lettura della risposta: " + e.getMessage());
        }
    }

    // Metodo per il login
    private void login() {
        System.out.print("Inserisci username: ");
        String username = scanner.nextLine();
        System.out.print("Inserisci password: ");
        String password = scanner.nextLine();
        scrittore.println("LOGIN&" + username + "&" + password);
        try {
            String risposta = lettore.readLine();
            System.out.println("\033[0;35mRisposta del server: " + risposta + "\033[0m");

        } catch (IOException e) {
            System.err.println("Errore durante la lettura della risposta: " + e.getMessage());
        }
    }

    // Metodo per il logout
    private void logout() {
        System.out.print("Inserisci username per il logout: ");
        String username = scanner.nextLine();
        scrittore.println("LOGOUT&" + username);
        try {
            String risposta = lettore.readLine();
            System.out.println("\033[0;35mRisposta del server: " + risposta + "\033[0m");

        } catch (IOException e) {
            System.err.println("Errore durante la lettura della risposta: " + e.getMessage());
        }
    }

    // Metodo per la ricerca di un hotel
    private void cercaHotel() {
        System.out.print("Inserisci nome dell'hotel: ");
        String nomeHotel = scanner.nextLine();
        System.out.print("Inserisci città dell'hotel: ");
        String citta = scanner.nextLine();

        scrittore.println("SEARCH_HOTEL&" + nomeHotel + "&" + citta);

        try {
            String risposta;
            while (!(risposta = lettore.readLine()).equals("END")) {
                System.out.println(risposta);
            }
        } catch (IOException e) {
            System.err.println("Errore durante la lettura della risposta: " + e.getMessage());
        }
    }

    // Metodo per la ricerca di tutti gli hotel in una città
    private void cercaTuttiGliHotel() {
        System.out.print("Inserisci città: ");
        String citta = scanner.nextLine();
        scrittore.println("SEARCH_ALL_HOTELS&" + citta);
        try {
            String risposta;
            while (!(risposta = lettore.readLine()).equals("fine")) {
                System.out.println(risposta);
            }
        } catch (IOException e) {
            System.err.println("Errore durante la lettura della risposta: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        HOTELIERCustomerClient client = new HOTELIERCustomerClient();
        client.avviaCLI();
    }
}
