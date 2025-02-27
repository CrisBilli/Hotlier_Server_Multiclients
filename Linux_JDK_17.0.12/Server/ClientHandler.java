package Server;

import Oggetti_Fondamentali.Hotel;
import Oggetti_Fondamentali.Punteggi;
import Oggetti_Fondamentali.Recensione;
import Oggetti_Fondamentali.Utente;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Database_utenti databaseUtenti;
    private Map<Socket, String> loggedInUsers = new ConcurrentHashMap<>();  // Inizializzazione diretta
    private Database_hotels databaseHotels;  // Aggiungi il database degli hotel

    public ClientHandler(Socket socket, Database_utenti dbUtenti, Map<Socket, String> loggedInUsers, Database_hotels dbHotels) throws IOException {
        this.clientSocket = socket;
        this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
        this.databaseUtenti = dbUtenti;
        this.loggedInUsers = loggedInUsers;
        this.databaseHotels = dbHotels;  
    }

    @Override
    public void run() {
        try {
            String messaggioClient;
            while ((messaggioClient = reader.readLine()) != null) {
                // Gestisce i comandi ricevuti dal client
                String[] parti = messaggioClient.split("&");
                String comando = parti[0];

                switch (comando) {
                    case "LOGIN":
                        gestisciLogin(parti);
                        break;
                    case "REGISTER":
                        gestisciRegistrazione(parti);
                        break;
                    case "LOGOUT":
                        gestisciLogout(parti);
                        break;
                    case "SEARCH_HOTEL":
                        gestisciRicercaHotel(parti);
                        break;
                    case "SEARCH_ALL_HOTELS":
                        gestisciRicercaTuttiHotel(parti);
                        break;
                    case "SHOW_MY_BADGES":
                        mostraDistintivi();
                        break;
                    case "INSERT_REVIEW":
                        gestisciInserimentoRecensione(parti);  // Aggiungiamo questo per gestire l'inserimento delle recensioni
                        break;
                    default:
                        writer.println(coloraTesto("ERRORE: Comando non riconosciuto", "\033[0;31m"));
                }
            }
        } catch (IOException e) {
            System.out.println("Il client si è disconnesso: " + clientSocket.getRemoteSocketAddress());
        } finally {
            // Logout automatico quando il client si disconnette
            String utenteLoggato = loggedInUsers.get(clientSocket);
            if (utenteLoggato != null) {
                loggedInUsers.remove(clientSocket);  // Rimuove il client dalla lista degli utenti loggati
                System.out.println("Utente " + utenteLoggato + " sloggato a causa della disconnessione.");
            }

            try {
                clientSocket.close();  // Chiudi il socket del client
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Metodo per colorare il testo
    private String coloraTesto(String testo, String codiceColore) {
        return codiceColore + testo + "\033[0m";
    }

    // Metodo per gestire la ricerca di un hotel
    private void gestisciRicercaHotel(String[] parti) {
        if (parti.length == 3) {
            String nomeHotel = parti[1].trim();
            String citta = parti[2].trim();

            // Controlla che i parametri non siano vuoti
            if (nomeHotel.isEmpty() || citta.isEmpty()) {
                writer.println(coloraTesto("ERRORE: Nome dell'hotel o nome della città non possono essere vuoti", "\033[0;35m"));
                writer.println("END");
                return;
            }

            // Cerca l'hotel nel database
            Hotel hotel = databaseHotels.trovaHotel(nomeHotel, citta);
            if (hotel != null) {
                writer.println(hotel.toString());
            } else {
                writer.println(coloraTesto("ERRORE: Hotel non trovato", "\033[0;35m"));
            }
            writer.println("END");  // Segnale di fine della risposta
        } else {
            writer.println(coloraTesto("ERRORE: Parametri non validi per la ricerca dell'hotel", "\033[0;35m"));
            writer.println("END");  // Segnale di fine della risposta anche in caso di errore
        }
    }

    // Metodo per gestire il login
    private void gestisciLogin(String[] parti) {
        if (parti.length == 3) {
            String nomeUtente = parti[1];
            String password = parti[2];

            if (databaseUtenti.verificaCredenziali(nomeUtente, password)) {
                // Aggiunge l'utente alla lista dei loggati
                loggedInUsers.put(clientSocket, nomeUtente);
                writer.println("LOGIN_SUCCESS");
                System.out.println("Utente " + nomeUtente + " loggato con successo.");

                // Stampa la lista degli utenti loggati
                stampaUtentiLoggati();
            } else {
                writer.println("LOGIN_FAILED");
                System.out.println("Tentativo di login fallito per l'utente: " + nomeUtente);
            }
        } else {
            writer.println(coloraTesto("ERRORE: Parametri non validi per il login", "\033[0;35m"));
        }
    }

    // Metodo per gestire l'inserimento di una recensione
    private void gestisciInserimentoRecensione(String[] parts) {
        if (parts.length == 8) {
            String username = loggedInUsers.get(clientSocket);
            if (username != null) {
                String hotelName = parts[1];
                String cityName = parts[2];

                try {
                    double cleaningScore = Double.parseDouble(parts[4]);
                    double positionScore = Double.parseDouble(parts[5]);
                    double servicesScore = Double.parseDouble(parts[6]);
                    double qualityScore = Double.parseDouble(parts[7]);

                    if (!isValidScore(cleaningScore) || !isValidScore(positionScore)
                            || !isValidScore(servicesScore) || !isValidScore(qualityScore)) {
                        writer.println("\033[0;31mERRORE: I punteggi devono essere compresi tra 0 e 5.\033[0m");
                    } else {
                        Hotel hotel = databaseHotels.trovaHotel(hotelName, cityName);
                        if (hotel != null) {
                            // Calcola il punteggio globale come media dei punteggi individuali
                            double globalScore = (cleaningScore + positionScore + servicesScore + qualityScore) / 4.0;

                            // Aggiungi la recensione all'hotel
                            hotel.aggiungiRecensione(globalScore, cleaningScore, positionScore, servicesScore, qualityScore);

                            try {
                                databaseHotels.salvaDatabase("./Server/hotels.json");
                                writer.println("REVIEW_INSERTED");

                                // Crea una nuova recensione da aggiungere all'utente
                                Punteggi punteggi = new Punteggi((int) positionScore, (int) cleaningScore, (int) servicesScore, (int) qualityScore);
                                Recensione nuovaRecensione = new Recensione(hotelName, cityName, punteggi);

                                // Aggiungi la recensione all'utente
                                databaseUtenti.aggiornaRecensioniUtente(username, nuovaRecensione);
                                databaseUtenti.salvaUtenti();
                            } catch (IOException e) {
                                writer.println("ERRORE: Non è stato possibile salvare il database.");
                                e.printStackTrace();
                            }
                        } else {
                            writer.println("\033[0;35mERRORE: Hotel non trovato\033[0m");
                        }
                    }
                } catch (NumberFormatException e) {
                    writer.println("\033[0;31mERRORE: I punteggi devono essere numeri validi.\033[0m");
                }
            } else {
                writer.println("\033[0;31mERRORE: Devi essere loggato per inserire una recensione.\033[0m");
            }
        } else {
            writer.println("\033[0;31mERRORE: Parametri non validi per l'inserimento della recensione.\033[0m");
        }
    }

    // Metodo per verificare se il punteggio è valido
    private boolean isValidScore(double score) {
        return score >= 0 && score <= 5;
    }

    // Metodo per gestire il logout
    private void gestisciLogout(String[] parti) {
        if (parti.length == 2) {
            String nomeUtente = parti[1];

            // Verifica se il socket corrente è associato all'utente che sta richiedendo il logout
            String utenteLoggato = loggedInUsers.get(clientSocket);
            if (utenteLoggato != null && utenteLoggato.equals(nomeUtente)) {
                // L'utente è loggato con questo socket e può effettuare il logout
                loggedInUsers.remove(clientSocket);  // Rimuove il client dalla mappa
                writer.println("LOGOUT_SUCCESS");
                System.out.println("Utente " + nomeUtente + " disconnesso.");

                // Stampa la lista aggiornata degli utenti loggati
                stampaUtentiLoggati();
            } else {
                writer.println("LOGOUT_FAILED: Non sei loggato come " + nomeUtente);
                System.out.println("Tentativo di logout fallito per l'utente: " + nomeUtente);
            }
        } else {
            writer.println(coloraTesto("ERRORE: Parametri non validi per il logout", "\033[0;31m"));
        }
    }

    // Metodo per la registrazione
    private void gestisciRegistrazione(String[] parti) {
        if (parti.length == 3) {
            String nomeUtente = parti[1];
            String password = parti[2];

            if (databaseUtenti.trovaUtente(nomeUtente) == null) {
                // Crea un nuovo utente e lo aggiunge al database
                Utente nuovoUtente = new Utente(nomeUtente, password);
                databaseUtenti.aggiungiUtente(nuovoUtente);
                writer.println("REGISTER_SUCCESS");
                System.out.println("Nuovo utente registrato: " + nomeUtente);
            } else {
                writer.println("USERNAME_TAKEN");
                System.out.println("Tentativo di registrazione fallito, username già esistente: " + nomeUtente);
            }
        } else {
            writer.println(coloraTesto("ERRORE: Parametri non validi per la registrazione", "\033[0;31m"));
        }
    }

    private void mostraDistintivi() {
        String nomeUtente = loggedInUsers.get(clientSocket);
        if (nomeUtente != null) {
            Utente utente = databaseUtenti.trovaUtente(nomeUtente);
            System.out.println("Distintivo per " + nomeUtente + ": " + utente.getLivelloDistintivo()); // Stampa di debug
            writer.println("BADGES: " + utente.getLivelloDistintivo());
        } else {
            writer.println(coloraTesto("ERRORE: Devi essere loggato per visualizzare i tuoi distintivi", "\033[0;31m"));
        }
    }

    // Metodo per gestire la ricerca di tutti gli hotel in una città
    private void gestisciRicercaTuttiHotel(String[] parti) {
        if (parti.length == 2) {
            String citta = parti[1].trim();

            // Se la città è vuota, gestisci l'errore
            if (citta.isEmpty()) {
                writer.println(coloraTesto("ERRORE: Il nome della città non può essere vuoto.", "\033[0;31m"));
                writer.println("fine");
                return;
            }

            // Cerca tutti gli hotel nella città specificata
            List<Hotel> hotelsInCity = databaseHotels.trovaHotelsPerCittaOrdinatiPerRanking(citta);

            if (hotelsInCity.isEmpty()) {
                writer.println(coloraTesto("NESSUN_HOTEL_TROVATO", "\033[0;31m"));
                writer.println("fine");
                return;
            }

            // Ordina gli hotel in base al punteggio medio (ranking)
            hotelsInCity.sort((h1, h2) -> Double.compare(h2.getPunteggioMedio(), h1.getPunteggioMedio()));

            // Invia gli hotel ordinati al client
            writer.println("+--------------------------------------------------+");
            writer.println("|               HOTELIER - Risultati               |");
            writer.println("+--------------------------------------------------+");
            for (Hotel hotel : hotelsInCity) {
                writer.println(hotel);
            }
            writer.println("+--------------------------------------------------+");
            writer.println("fine");  // Indica al client che la trasmissione è finita
        } else {
            writer.println(coloraTesto("ERRORE: Parametri non validi per la ricerca di tutti gli hotel", "\033[0;31m"));
            writer.println("fine");
        }
    }

    // Metodo per stampare la lista degli utenti loggati
    private void stampaUtentiLoggati() {
        System.out.println("Utenti attualmente loggati:");
        for (String user : loggedInUsers.values()) {
            System.out.println("- " + user);
        }
    }
}
