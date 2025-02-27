@echo off
echo Compilazione dei file .java...
javac -cp "Libreria/gson-2.10.jar" Client/*.java Server/*.java Oggetti_Fondamentali/*.java

echo Creazione del JAR del server...
jar cfm HOTELIERServer.jar META-INF/MANIFEST_Server.MF -C . Server -C . Oggetti_Fondamentali

echo Creazione del JAR del client...
jar cfm HOTELIERClient.jar META-INF/MANIFEST_Client.MF -C . Client -C . Oggetti_Fondamentali

echo Processo completato.
