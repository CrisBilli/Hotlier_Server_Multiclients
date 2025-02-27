# HOTELIER - Multi-Client Server in Java

## Descrizione
**HOTELIER** è un'applicazione client-server che permette agli utenti di registrarsi, effettuare il login, cercare hotel, inserire recensioni e visualizzare ranking in tempo reale.  
L'architettura prevede un **server** che gestisce le richieste e un **client** che interagisce con gli utenti.  
Il sistema supporta **multithreading** e **notifiche multicast UDP** per garantire aggiornamenti in tempo reale.

---

## Struttura del Progetto
Il progetto è organizzato nelle seguenti directory:

```
Root/
│── Client/
│   ├── HOTELIERClient.java
│   ├── Condivisore.java
│   ├── Config.properties
│
│── Server/
│   ├── HOTELIERServer.java
│   ├── ClientHandler.java
│   ├── Database_utenti.java
│   ├── Database_hotels.java
│   ├── Config.properties
│
│── Oggetti_Fondamentali/
│   ├── Hotel.java
│   ├── Punteggi.java
│   ├── Recensione.java
│   ├── Utente.java
│
│── Libreria/
│   ├── gson-2.10.jar
```

---

## Funzionalità Principali

### **Client**
- **Registrazione e Login**: L'utente può creare un account e autenticarsi.
- **Ricerca Hotel**: Possibilità di cercare un hotel per nome e città.
- **Inserimento Recensioni**: Gli utenti possono valutare gli hotel e assegnare punteggi su diversi criteri.
- **Visualizzazione Distintivi**: Mostra i riconoscimenti ottenuti in base alle recensioni scritte.
- **Sistema di Notifiche**: Ricezione di aggiornamenti in tempo reale tramite multicast UDP.

### **Server**
- **Gestione Utenti e Hotel**: Mantiene un database di utenti e hotel in JSON.
- **Algoritmo di Ranking**: Ordina gli hotel in base alle recensioni degli utenti.
- **Notifiche in Tempo Reale**: Invio di aggiornamenti agli utenti connessi.
- **Multi-Threading**: Gestisce più utenti contemporaneamente con thread separati.
- **Persistenza Dati**: Salvataggio automatico delle recensioni e delle informazioni sugli hotel.

---

## Installazione e Avvio

### **1. Compilazione del Progetto**
Per compilare il progetto, esegui:

**Windows**
```sh
./Builder.bat
```

**Linux**
```sh
./Builder.sh
```

Questo comando compila i file Java e crea i file `.jar` per il server e il client.

---

### **2. Avvio del Server**
Dopo la compilazione, avvia il server con:
```sh
java -jar HOTELIERServer.jar
```
Se necessario, specifica il percorso della libreria Gson:
```sh
java -cp "Libreria/gson-2.10.jar;HOTELIERServer.jar" Server.HOTELIERServer
```

---

### **3. Avvio del Client**
Per avviare il client:
```sh
java -jar HOTELIERClient.jar
```
Oppure, con Gson:
```sh
java -cp "Libreria/gson-2.10.jar;HOTELIERClient.jar" Client.HOTELIERCustomerClient
```

---

## Configurazione
Assicurati di modificare i file **Config.properties** nel client e nel server con i parametri appropriati, come:
- **Indirizzo IP del server**
- **Porta di comunicazione**
- **Impostazioni database**

---

## **Testing e Debugging**
Sono disponibili script per avviare il server e simulare più client contemporaneamente:

**Linux**
```sh
./avvia_hotelier_linux.sh
```

**Windows**
```sh
./avvia_hotelier_windows.sh
```

Questi script consentono di testare facilmente il sistema senza dover avviare manualmente ogni componente.

---

## **Autore**
**Cristian Billi**  


---

