package Oggetti_Fondamentali;
import java.util.ArrayList;
import java.util.List;

public class Utente {

    private String nome;
    private String password;
    private int numeroRecensioni;
    private String livelloDistintivo;
    private List<Recensione> recensioni;
    
    // Costruttore
    public Utente(String nome, String password) {
        this.nome = nome;
        this.password = password;
        this.numeroRecensioni = 0;
        this.livelloDistintivo = "Recensore";
        this.recensioni = new ArrayList<>();
    }

    // Metodo per controllare la password
    public boolean controllaPassword(String pass) {
        return pass.equals(this.password);
    }

    // Getter per il nome
    public String getNome() {
        return this.nome;
    }

    // Getter per il numero di recensioni
    public int getNumeroRecensioni() {
        return this.numeroRecensioni;
    }

    // Getter per il livello del distintivo
    public String getLivelloDistintivo() {
        return this.livelloDistintivo;
    }

    // Setter per il numero di recensioni
    public void setNumeroRecensioni(int numeroRecensioni) {
        this.numeroRecensioni = numeroRecensioni;
    }

    // Setter per il livello del distintivo
    public void setLivelloDistintivo(String livelloDistintivo) {
        this.livelloDistintivo = livelloDistintivo;
    }

    // Metodo per aggiornare il livello distintivo in base al numero di recensioni
    public void aggiornaLivelloDistintivo() {
        if (numeroRecensioni >= 50) {
            this.livelloDistintivo = "Contributore Super"; 
        } else if (numeroRecensioni >= 30) {
            this.livelloDistintivo = "Contributore Esperto"; 
        } else if (numeroRecensioni >= 15) {
            this.livelloDistintivo = "Contributore"; 
        } else if (numeroRecensioni >= 5) {
            this.livelloDistintivo ="Recensore Esperto"; 
        } else {
            this.livelloDistintivo ="Recensore"; 
        }
    }

    // Metodo per aggiungere una recensione all'utente e aggiornare il distintivo
    public void aggiungiRecensione(Recensione recensione) {
        this.recensioni.add(recensione);
        aggiornaLivelloDistintivo(); // Aggiorna il livello del distintivo
    }

    // Metodo per ottenere la lista delle recensioni
    public List<Recensione> getRecensioni() {
        return this.recensioni;
    }

    // Metodo toString per visualizzare i dati dell'utente
    @Override
    public String toString() {
        return "Nome: " + nome + "\n" +
               "Numero di recensioni: " + numeroRecensioni + "\n" +
               "Livello distintivo: " + livelloDistintivo;
    }
}
