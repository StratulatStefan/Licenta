import client_manager.ClientManagerRequest;
import client_manager.Token;
import communication.Address;
import communication.Serializer;
import model.ConnectionTable;
import model.StorageStatusTable;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.lang.*;
import java.util.HashMap;

/* https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/MulticastSocket.html */
/* https://tldp.org/HOWTO/Multicast-HOWTO.html#toc1 */
public class GeneralManager{
    /**
     * Tabela (o lista) nodurilor conectate in retea, care comunica cu nodul curent.
     */
    public final static ConnectionTable connectionTable = new ConnectionTable();

    /**
     * Tabela ce contine statusul stocarii nodurilor
     */
    public final static StorageStatusTable statusTable = new StorageStatusTable();

    /**
     * Adresa IP la care va fi mapat managerul general
     */
    private static String generalManagerIpAddress = "127.0.0.1";

    /**
     * Portul de multicast.
     */
    private static int multicastPort = 8246;

    /**
     * Portul pentru comunicarea cu clientul
     */
    private final static int dataTransmissionPort = 8081;

    /**
     * Frecventa heartbeat-urilor
     * Exprimat in secunde.
     */
    private final static double hearthBeatFrequency = 5;

    /**
     * Dimensiunea bufferului in care vor fi citite datele de la un nod adiacent
     */
    private final static int bufferSize = 1024;

    /**
     * Obiectul care se ocupa de mecanismul de hearthbeats
     */
    private HearthBeatManager hearthBeatManager;

    /**
     * Obiectul care se va ocupa de comunicatia cu clientul
     */
    private ClientCommunicationManager clientCommunicationManager;

    /**
     * Constructorul clasei
     * @param hearthBeatManager Managerul de heartbeat-uri
     */
    public GeneralManager(HearthBeatManager hearthBeatManager, ClientCommunicationManager clientCommunicationManager){
        this.hearthBeatManager = hearthBeatManager;
        this.clientCommunicationManager = clientCommunicationManager;
    }


    public void StartActivity() throws Exception {
        // Pornim thread-ul pe care va fi rulat mecanismul de heartbeats
        new Thread(hearthBeatManager).start();

        clientCommunicationManager.ClientCommunicationLoop(generalManagerIpAddress, dataTransmissionPort, connectionTable);
    }


    /**
     * @param args Argumentele furnizate la linia de comanda
     */
    public static void main(String[] args){
        Address multicastAddress;
        try {
            multicastAddress = new Address(generalManagerIpAddress, multicastPort);
            HearthBeatManager hearthBeatManager = new HearthBeatManager(multicastAddress, hearthBeatFrequency);

            ClientCommunicationManager clientCommunicationManager = new ClientCommunicationManager();
            GeneralManager generalManager = new GeneralManager(hearthBeatManager, clientCommunicationManager);

            generalManager.StartActivity();
        }
        catch (Exception exception){
            System.out.println(exception.getMessage());
        }
    }
}