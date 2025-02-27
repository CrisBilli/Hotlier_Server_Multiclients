package Server;

import Oggetti_Fondamentali.Utente;  // Assicurati che il pacchetto sia corretto
import Oggetti_Fondamentali.Recensione; 
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.GsonBuilder;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Database_utenti {
    private String percorsoFile;  // Percorso del file JSON
    private List<Utente> utenti;  // Lista di utenti
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();  // Lock per gestire la concorrenza

    // Costruttore che accetta il percorso del file JSON
    public Database_utenti(String percorsoFile) {
        this.percorsoFile = percorsoFile;
        this.utenti = caricaUtenti();  // Carica gli utenti all'inizializzazione
    }

    // Metodo per aggiornare le recensioni di un utente con attenzione a prendere il lock dopo aver trovato client , ridurre tempi di locking
    public void aggiornaRecensioniUtente(String username, Recensione recensione) {
    Utente utente;
    lock.readLock().lock();  // Blocco in lettura per la ricerca
    try {
        utente = trovaUtente(username);  // Cerca l'utente
    } finally {
        lock.readLock().unlock();  // Rilascia il lock in lettura
    }

    if (utente != null) {
        lock.writeLock().lock();  // Acquisisce il lock in scrittura solo se l'utente esiste
        try {
            utente.setNumeroRecensioni(utente.getNumeroRecensioni() + 1);
            utente.aggiornaLivelloDistintivo();
            utente.aggiungiRecensione(recensione);
            salvaUtenti();
        } finally {
            lock.writeLock().unlock();  // Rilascia il lock
        }
    }
}


    // Metodo per caricare gli utenti dal file JSON
    public List<Utente> caricaUtenti() {
        lock.readLock().lock();  // Blocco in lettura
        try (BufferedReader reader = new BufferedReader(new FileReader(percorsoFile))) {
            Gson gson = new Gson();
            List<Utente> utentiCaricati = gson.fromJson(reader, new TypeToken<List<Utente>>() {}.getType());

            if (utentiCaricati == null) {
                utentiCaricati = new ArrayList<>();
                System.out.println("Nessun utente trovato, inizializzo lista vuota.");
            } else {
                System.out.println("Utenti caricati correttamente.");
            }
            return utentiCaricati;
        } catch (IOException e) {
            System.err.println("Errore durante il caricamento del database utenti: " + e.getMessage());
            return new ArrayList<>();  // Restituisci una lista vuota in caso di errore
        } finally {
            lock.readLock().unlock();  // Rilascia il lock
        }
    }

    // Metodo per salvare gli utenti nel file JSON
    public void salvaUtenti() {
        lock.writeLock().lock();  // Blocco in scrittura
        try (Writer writer = new FileWriter(percorsoFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();  // Per salvare in formato leggibile
            gson.toJson(utenti, writer);
        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio del database utenti: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();  // Rilascia il lock
        }
    }

    // Metodo per aggiungere un utente
    public void aggiungiUtente(Utente utente) {
        lock.writeLock().lock();  // Blocco in scrittura
        try {
            if (utenti == null) {
                utenti = new ArrayList<>();
            }
            utenti.add(utente);
            salvaUtenti();
        } finally {
            lock.writeLock().unlock();  // Rilascia il lock
        }
    }

    // Metodo per verificare le credenziali di un utente
    public boolean verificaCredenziali(String nome, String password) {
        lock.readLock().lock();  // Blocco in lettura
        try {
            if (utenti == null || utenti.isEmpty()) {
                return false;  // Nessun utente registrato
            }
            for (Utente utente : utenti) {
                if (utente.getNome().equals(nome) && utente.controllaPassword(password)) {
                    return true;
                }
            }
            return false;
        } finally {
            lock.readLock().unlock();  // Rilascia il lock
        }
    }

    // Metodo per trovare un utente
    public Utente trovaUtente(String nome) {
        lock.readLock().lock();  // Blocco in lettura
        try {
            for (Utente utente : utenti) {
                if (utente.getNome().equals(nome)) {
                    return utente;
                }
            }
            return null;  // Utente non trovato
        } finally {
            lock.readLock().unlock();  // Rilascia il lock
        }
    }
}
