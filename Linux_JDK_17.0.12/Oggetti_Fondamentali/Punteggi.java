package Oggetti_Fondamentali;

public class Punteggi {

    private int posizione;
    private int pulizia;
    private int servizio;
    private int prezzo;

    // Costruttore di default
    public Punteggi() {
        this(0, 0, 0, 0);  // Chiama il costruttore parametrizzato con valori di default
    }

    // Costruttore parametrizzato
    public Punteggi(int posizione, int pulizia, int servizio, int prezzo) {
        this.posizione = validaPunteggio(posizione);
        this.pulizia = validaPunteggio(pulizia);
        this.servizio = validaPunteggio(servizio);
        this.prezzo = validaPunteggio(prezzo);
    }

    // Metodo di validazione per i punteggi
    private int validaPunteggio(int punteggio) {
        if (punteggio < 0) {
            return 0;  // Imposta a 0 se il punteggio è negativo
        }
        return Math.min(punteggio, 5);  // Limita il punteggio a un massimo di 5
    }

    // Getter e Setter
    public int getPosizione() {
        return posizione;
    }

    public void setPosizione(int posizione) {
        this.posizione = validaPunteggio(posizione);
    }

    public int getPulizia() {
        return pulizia;
    }

    public void setPulizia(int pulizia) {
        this.pulizia = validaPunteggio(pulizia);
    }

    public int getServizio() {
        return servizio;
    }

    public void setServizio(int servizio) {
        this.servizio = validaPunteggio(servizio);
    }

    public int getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(int prezzo) {
        this.prezzo = validaPunteggio(prezzo);
    }

    // Metodo per calcolare la media dei punteggi
    public double calcolaMedia() {
        return (posizione + pulizia + servizio + prezzo) / 4.0;
    }

    // Metodo toString per visualizzare i punteggi
    @Override
    public String toString() {
        return "Posizione: " + posizione + ", Pulizia: " + pulizia +
               ", Servizio: " + servizio + ", Prezzo: " + prezzo;
    }
}
