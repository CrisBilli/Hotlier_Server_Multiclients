package Oggetti_Fondamentali;

public class Recensione {

    private String nomeHotel;
    private String citta;
    private Punteggi punteggi;

    // Costruttore completo
    public Recensione(String nomeHotel, String citta, Punteggi punteggi) {
        this.nomeHotel = nomeHotel;
        this.citta = citta;
        this.punteggi = punteggi;
    }

    // Getter e Setter per nomeHotel
    public String getNomeHotel() {
        return nomeHotel;
    }

    public void setNomeHotel(String nomeHotel) {
        this.nomeHotel = nomeHotel;
    }

    // Getter e Setter per citta
    public String getCitta() {
        return citta;
    }

    public void setCitta(String citta) {
        this.citta = citta;
    }

    // Getter e Setter per punteggi
    public Punteggi getPunteggi() {
        return punteggi;
    }

    public void setPunteggi(Punteggi punteggi) {
        this.punteggi = punteggi;
    }

    // Metodo per calcolare il punteggio globale come media dei punteggi
    public double calcolaPunteggioGlobale() {
        return punteggi.calcolaMedia();
    }

    // Metodo toString per stampare i dettagli della recensione
    @Override
    public String toString() {
        return "Hotel: " + nomeHotel + ", Città: " + citta + 
               ", Punteggio Globale: " + calcolaPunteggioGlobale() + 
               "\nDettagli Punteggi: " + punteggi.toString();
    }
}
