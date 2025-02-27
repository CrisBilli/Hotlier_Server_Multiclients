package Client;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;

public class Condivisore implements Runnable {

    private MulticastSocket socket;
    private InetAddress indirizzoGruppo;
    private int porta;
    private Queue<String> codaNotifiche = new LinkedList<>();

    // Costruttore: inizializza il socket multicast e si unisce al gruppo
    public Condivisore(int porta, String indirizzoGruppoIp) throws IOException {
        this.porta = porta;
        this.socket = new MulticastSocket(porta);
        this.indirizzoGruppo = InetAddress.getByName(indirizzoGruppoIp);

        // Usa il metodo non  per unirsi al gruppo multicast
        NetworkInterface interfacciaDiRete = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
        this.socket.joinGroup(new InetSocketAddress(indirizzoGruppo, porta), interfacciaDiRete);
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte[] buffer = new byte[2048];  // Buffer per il pacchetto in arrivo
                DatagramPacket pacchetto = new DatagramPacket(buffer, buffer.length);
                socket.receive(pacchetto);

                // Aggiunge ogni notifica ricevuta alla coda
                String notifica = new String(pacchetto.getData(), 0, pacchetto.getLength(), StandardCharsets.UTF_8).trim();
                synchronized (codaNotifiche) {
                    codaNotifiche.add(notifica);  // Aggiungi la notifica alla coda
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    
    // Metodo per ottenere e rimuovere tutte le notifiche
    public Queue<String> ottieniNotifiche() {
        Queue<String> copiaNotifiche = new LinkedList<>(codaNotifiche);  // Crea una copia della coda
        codaNotifiche.clear();  // Svuota la coda originale in modo sicuro
        return copiaNotifiche;
    }

}
