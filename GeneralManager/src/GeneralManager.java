import communication.Address;
import config.AppConfig;
import log.ProfiPrinter;
import model.*;
import storage_quantity.NodeStorageQuantityTable;
import storage_quantity.StorageQuantityTable;
import storage_quantity.UserStorageQuantityTable;

import java.lang.*;

/* https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/MulticastSocket.html */
/* https://tldp.org/HOWTO/Multicast-HOWTO.html#toc1 */

/**
 * Clasa generala a managerului.
 */
public class GeneralManager{
    /** -------- Atribute generale -------- **/
    /**
     * Adresa IP la care va fi mapat managerul general
     */
    private static String generalManagerIpAddress;
    /**
     * Calea de baza la care se vor stoca fisierele
     */
    public static String storagePath;


    /** -------- Tabele -------- **/
    /**
     * Tabela (o lista) nodurilor conectate in retea, care comunica cu nodul curent.
     */
    public final static ConnectionTable connectionTable = new ConnectionTable();

    /**
     * Tabela ce contine statusul stocarii nodurilor
     */
    public final static StorageStatusTable statusTable = new StorageStatusTable();

    /**
     * Tabela ce contine fisierele care ar trebui sa existe in sistem
     */
    public final static ContentTable contentTable = new ContentTable();

    /**
     * Tabela ce contine informatii despre cantitatea de memorie disponibila pentru fiecare nod.
     */
    public static NodeStorageQuantityTable nodeStorageQuantityTable;

    /**
     * Tabela ce contine informatii despre cantitatea de memorie disponibila pentru fiecare utilizator.
     */
    public static UserStorageQuantityTable userStorageQuantityTable;

    /**
     * Tabela ce contine datele utilizatorilor (id si tip)
     */
    public static UserDataTable userDataTable;

    public static PendingQueue pendingQueue;


    /** -------- Managerii activitatilor -------- **/
    /**
     * Obiectul care se ocupa de mecanismul de hearthbeats
     */
    private HearthBeatManager hearthBeatManager;

    /**
     * Obiectul care se va ocupa de comunicatia cu clientul
     */
    private ClientCommunicationManager clientCommunicationManager;

    /**
     * Obiectul care se va ocupa de mecanismul de replicare
     */
    private ReplicationManager replicationManager;

    public static FeedbackManager feedbackManager;

    /**
     * Obiectul care se va ocupa de prelucrarea fisierelor
     */
    public static FileSystemManager fileSystemManager = new FileSystemManager();


    /** -------- Functii de initializare -------- **/
    /**
     * Functie care citeste si initializeaza parametrii de configurare
     */
    public static void readConfigParams(){
        generalManagerIpAddress = AppConfig.getParam("generalManagerIpAddress");
        storagePath = AppConfig.getParam("storagePath");
    }

    /**
     * Constructorul clasei
     */
    public GeneralManager() throws Exception {
        this.feedbackManager = new FeedbackManager();
        this.hearthBeatManager = new HearthBeatManager(generalManagerIpAddress);
        this.clientCommunicationManager = new ClientCommunicationManager();
        this.replicationManager = new ReplicationManager();
        GeneralManager.fileSystemManager.readConfigParams();
    }


    /** -------- Main -------- **/
    /**
     * Functia care porneste toate activitatile managerilor;
     * Apeluri de functii sau pornire de thread-uri
     */
    public void startActivity() throws Exception {
        GeneralManager.pendingQueue = new PendingQueue();
        // Pornim thread-ul pe care va fi rulat mecanismul de heartbeats
        new Thread(hearthBeatManager).start();

        new Thread(replicationManager).start();

        new Thread(feedbackManager).start();

        clientCommunicationManager.clientCommunicationLoop(generalManagerIpAddress);
    }

    /**
     * @param args Argumentele furnizate la linia de comanda
     */
    public static void main(String[] args){
        AppConfig.readConfig();
        readConfigParams();

        try {
            nodeStorageQuantityTable = new NodeStorageQuantityTable();
            userStorageQuantityTable = new UserStorageQuantityTable();
            userDataTable = new UserDataTable();

            GeneralManager generalManager = new GeneralManager();
            generalManager.startActivity();
        }
        catch (Exception exception){
            ProfiPrinter.PrintException("Exceptie la GeneralManagerMain : " + exception.getMessage());
        }
    }
}