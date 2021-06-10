import config.AppConfig;
import generalstructures.PendingQueue;
import logger.LoggerService;
import tables.NodeStorageQuantityTable;
import tables.ConnectionTable;
import tables.ContentTable;
import tables.StorageStatusTable;

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
    public static String generalManagerIpAddress = AppConfig.getParam("generalManagerIpAddress");
    /**
     * Calea de baza la care se vor stoca fisierele
     */
    public static String storagePath = AppConfig.getParam("storagePath");


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
     * Constructorul clasei
     */
    public GeneralManager() throws Exception {
        feedbackManager = new FeedbackManager();
        this.hearthBeatManager = new HearthBeatManager(generalManagerIpAddress);
        this.clientCommunicationManager = new ClientCommunicationManager();
        this.replicationManager = new ReplicationManager();
    }

    public static void attachRuntimeExitHook(){
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                LoggerService.registerError(generalManagerIpAddress, "General manager closed.");
            }
        });

    }
    /** -------- Main -------- **/
    /**
     * Functia care porneste toate activitatile managerilor;
     * Apeluri de functii sau pornire de thread-uri
     */
    public void startActivity() throws Exception {
        GeneralManager.pendingQueue = new PendingQueue();
        new Thread(hearthBeatManager).start();

        new Thread(replicationManager).start();

        new Thread(feedbackManager).start();

        clientCommunicationManager.clientCommunicationLoop(generalManagerIpAddress);
    }

    /**
     * @param args Argumentele furnizate la linia de comanda
     */
    public static void main(String[] args){
        attachRuntimeExitHook();
        AppConfig.readConfig();

        try {
            LoggerService.registerSuccess(generalManagerIpAddress, "General manager successfully started");
            nodeStorageQuantityTable = new NodeStorageQuantityTable();

            GeneralManager generalManager = new GeneralManager();
            generalManager.startActivity();
        }
        catch (Exception exception){
            LoggerService.registerError(generalManagerIpAddress, "GeneralManagerMain exception : " + exception.getMessage());
        }
    }
}