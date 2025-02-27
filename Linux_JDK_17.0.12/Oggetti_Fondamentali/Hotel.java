package Oggetti_Fondamentali;
import java.util.List;

public class Hotel {

    private int id;
    private String name;  // Nome dell'hotel
    private String description;  // Descrizione dell'hotel
    private String city;  // Città dell'hotel
    private String phone;  // Numero di telefono
    private List<String> services;  // Servizi offerti dall'hotel
    private double rate;  // Valutazione complessiva
    private Ratings ratings;  // Oggetto per i punteggi specifici

    private int contaRecensioni = 0;  // Conta delle recensioni

    // Classe interna per i punteggi
    public static class Ratings {
        private double cleaning;
        private double position;
        private double services;
        private double quality;

        // Costruttore di default
        public Ratings() {
            this.cleaning = 0;
            this.position = 0;
            this.services = 0;
            this.quality = 0;
        }

        // Getter per i punteggi
        public double getCleaning() { return cleaning; }
        public double getPosition() { return position; }
        public double getServices() { return services; }
        public double getQuality() { return quality; }
    }

    // Costruttore di base per la classe Hotel
    public Hotel(int id, String name, String description, String city, String phone, List<String> services) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.city = city;
        this.phone = phone;
        this.services = services;
        this.rate = 0;
        this.ratings = new Ratings();  // Inizializza i punteggi
    }

    // Getter per i campi principali
    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getDescription() {
        return description;
    }

    public String getPhone() {
        return phone;
    }

    public List<String> getServices() {
        return services;
    }

    public double getRate() {
        return rate;
    }

    public Ratings getRatings() {
        return ratings;
    }

    // Metodo per aggiungere una recensione all'hotel (sincronizzato per sicurezza)
    public synchronized void aggiungiRecensione(double globalScore, double cleaning, double position, double services, double quality) {
        // Aggiorna il punteggio e le recensioni
        this.rate = (this.rate * this.contaRecensioni + globalScore) / (this.contaRecensioni + 1);
        this.ratings.cleaning = (this.ratings.cleaning * this.contaRecensioni + cleaning) / (this.contaRecensioni + 1);
        this.ratings.position = (this.ratings.position * this.contaRecensioni + position) / (this.contaRecensioni + 1);
        this.ratings.services = (this.ratings.services * this.contaRecensioni + services) / (this.contaRecensioni + 1);
        this.ratings.quality = (this.ratings.quality * this.contaRecensioni + quality) / (this.contaRecensioni + 1);
    
        this.contaRecensioni++;  // Incrementa il numero di recensioni

        // Stampa i dati aggiornati
        System.out.println("Recensione aggiunta per " + this.name + ". Punteggio medio: " + this.rate + ", Recensioni: " + this.contaRecensioni);
    }

    // Metodo per ottenere il numero totale di recensioni
    public int getRecensioniTotali() {
        return contaRecensioni;
    }

    // Metodo per ottenere il punteggio medio
    public double getPunteggioMedio() {
        return this.rate;  // Considera il rate come il punteggio medio
    }

    // Metodo toString per visualizzare i dati dell'hotel
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("+--------------------------------------------------+\n");
        sb.append("|                 Dettagli Hotel                   |\n");
        sb.append("+--------------------------------------------------+\n");
        sb.append("| Nome:           ").append(name).append("\n");
        sb.append("| Città:          ").append(city).append("\n");
        sb.append("| Descrizione:    ").append(description).append("\n");
        sb.append("| Telefono:       ").append(phone).append("\n");
        sb.append("| Servizi:        ").append(services == null || services.isEmpty() ? "Nessuno" : String.join(", ", services)).append("\n");
        sb.append("| Punteggio Medio:").append(String.format(" %.1f", rate)).append("\n");
        sb.append("+--------------------------------------------------+\n");
        sb.append("|                   Recensioni                     |\n");
        sb.append("+--------------------------------------------------+\n");
        sb.append("| Pulizia:        ").append(String.format("%.1f", ratings.getCleaning())).append("\n");
        sb.append("| Posizione:      ").append(String.format("%.1f", ratings.getPosition())).append("\n");
        sb.append("| Servizi:        ").append(String.format("%.1f", ratings.getServices())).append("\n");
        sb.append("| Qualità:        ").append(String.format("%.1f", ratings.getQuality())).append("\n");
        sb.append("+--------------------------------------------------+");
        return sb.toString();
    }
}
