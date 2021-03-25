import communication.Address;
import model.ConnectionTable;
import model.ContentTable;
import model.StorageStatusTable;

import java.lang.*;

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

    public final static ContentTable contentTable = new ContentTable();

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
     * Portul pentru replicare
     */
    private final static int replicationPort = 8082;

    /**
     * Frecventa heartbeat-urilor
     * Exprimat in secunde.
     */
    private final static double hearthBeatFrequency = 5;

    /**
     * Frecventa pentru mecanismul de replicare
     */
    private final static int replicationFrequency = 5;

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

    private ReplicationManager replicationManager;

    public static FileSystemManager fileSystemManager;

    /**
     * Constructorul clasei
     * @param hearthBeatManager Managerul de heartbeat-uri
     */
    public GeneralManager(HearthBeatManager hearthBeatManager, ClientCommunicationManager clientCommunicationManager,
                          ReplicationManager replicationManager, FileSystemManager fileSystemManager){
        this.hearthBeatManager = hearthBeatManager;
        this.clientCommunicationManager = clientCommunicationManager;
        this.replicationManager = replicationManager;
        this.fileSystemManager = fileSystemManager;
    }

    public void startActivity() throws Exception {
        // Pornim thread-ul pe care va fi rulat mecanismul de heartbeats
        new Thread(hearthBeatManager).start();

        new Thread(replicationManager).start();

        clientCommunicationManager.clientCommunicationLoop(generalManagerIpAddress, dataTransmissionPort);
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

            ReplicationManager replicationManager = new ReplicationManager(replicationPort, replicationFrequency);

            FileSystemManager editRequestManager = new FileSystemManager();
            GeneralManager generalManager = new GeneralManager(hearthBeatManager, clientCommunicationManager, replicationManager, editRequestManager);

            generalManager.startActivity();
        }
        catch (Exception exception){
            System.out.println(exception.getMessage());
        }
    }
}