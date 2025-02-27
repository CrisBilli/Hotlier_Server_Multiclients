package Server;

import Oggetti_Fondamentali.Hotel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.util.Set;
import java.util.HashSet;
import java.io.IOException;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Database_hotels {

    private List<Hotel> hotels;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();  // Lock per gestire la concorrenza

    // Costruttore: Carica gli hotel dal file JSON
    public Database_hotels(String percorsoFile) throws IOException {
        // Prima carica i dati in una lista temporanea per evitare di prendere il lock per troppo tempo
        List<Hotel> tempHotels;
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(percorsoFile)) {
            Type hotelListType = new TypeToken<List<Hotel>>() {}.getType();
            tempHotels = gson.fromJson(reader, hotelListType);  // Carica la lista di hotel
        }

        // Acquisisce il lock solo per assegnare la lista a `hotels` per evitare di tenere il lock anche per il caricamento.
        lock.writeLock().lock();
        try {
            hotels = tempHotels;
        } finally {
            lock.writeLock().unlock();  // Rilascia il lock
        }
    }

    // Metodo per ottenere tutte le città presenti nel database degli hotel
    public Set<String> getAllCities() {
        lock.readLock().lock();  // Lock in lettura
        try {
            Set<String> cities = new HashSet<>();
            for (Hotel hotel : hotels) {
                cities.add(hotel.getCity());  // Aggiunge la città dell'hotel alla lista
            }
            return cities;
        } finally {
            lock.readLock().unlock();  // Rilascia il lock
        }
    }

    // Metodo per cercare un hotel per nome e città
    public Hotel trovaHotel(String nomeHotel, String citta) {
        lock.readLock().lock();  // Lock in lettura
        try {
            for (Hotel hotel : hotels) {
                if (hotel.getName().equalsIgnoreCase(nomeHotel) && hotel.getCity().equalsIgnoreCase(citta)) {
                    return hotel;  // Restituisce l'hotel trovato
                }
            }
            return null;  // Nessun hotel trovato
        } finally {
            lock.readLock().unlock();  // Rilascia il lock
        }
    }

    // Metodo per trovare tutti gli hotel in una città e ordinarli per ranking
    public List<Hotel> trovaHotelsPerCittaOrdinatiPerRanking(String city) {
        lock.readLock().lock();  // Lock in lettura
        try {
            List<Hotel> hotelsInCity = new ArrayList<>();

            // Filtra gli hotel per la città specifica
            for (Hotel hotel : hotels) {
                if (hotel.getCity().equalsIgnoreCase(city)) {
                    hotelsInCity.add(hotel);
                }
            }

            // Ordina prima per punteggio medio, poi per numero di recensioni e infine per numero di servizi
            hotelsInCity.sort(Comparator.comparingDouble(Hotel::getRate) // Punteggio medio
                    .thenComparingInt(Hotel::getRecensioniTotali) // Numero di recensioni
                    .thenComparingInt(hotel -> hotel.getServices().size()) // Numero di servizi
                    .reversed());  // Ordina in ordine decrescente per tutti i criteri

            return hotelsInCity;
        } finally {
            lock.readLock().unlock();  // Rilascia il lock
        }
    }

    // Metodo per aggiungere una recensione e aggiornare il punteggio dell'hotel
    public void aggiungiRecensione(String nomeHotel, String citta, double globalScore, double[] singleScores) {
        lock.writeLock().lock();  // Lock in scrittura
        try {
            Hotel hotel = trovaHotel(nomeHotel, citta);  // Trova l'hotel nel database
            if (hotel != null) {
                // Aggiungi la nuova recensione all'hotel
                hotel.aggiungiRecensione(globalScore, singleScores[0], singleScores[1], singleScores[2], singleScores[3]);

                // Salva l'intero database degli hotel aggiornato
                try {
                    salvaDatabase("./Server/hotels.json");  // Salva nel file JSON
                } catch (IOException e) {
                    e.printStackTrace();  // Gestione dell'errore in caso di problemi con il file
                }
            } else {
                System.out.println("Hotel non trovato: " + nomeHotel + ", " + citta);
            }
        } finally {
            lock.writeLock().unlock();  // Rilascia il lock
        }
    }

    // Metodo per salvare l'intero database di hotel nel file JSON (sovrascrive l'intero file)
    public void salvaDatabase(String percorsoFile) throws IOException {
        lock.writeLock().lock();  // Lock in scrittura
        try (FileWriter writer = new FileWriter(percorsoFile)) {
            Gson gson = new Gson();
            gson.toJson(hotels, writer);  // Scrive tutta la lista di hotel nel file, inclusi quelli già esistenti
        } finally {
            lock.writeLock().unlock();  // Rilascia il lock
        }
    }
}
