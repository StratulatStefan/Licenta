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
    /**
     * Adresa IP la care va fi mapat managerul general
     */
    public static String generalManagerIpAddress = AppConfig.getParam("generalManagerIpAddress");

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
     * Lista de asteptare folosita in cadrul procesului de stocare si inregistrare a unui nou fisier.
     */
    public static PendingQueue pendingQueue;

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

    /**
     * Obiectul care se va ocupa de mecanismul de feedback
     */
    public static FeedbackManager feedbackManager;

    /**
     * Obiectul care se va ocupa de prelucrarea fisierelor
     */
    public static FileSystemManager fileSystemManager = new FileSystemManager();

    /**
     * <ul>
     * 	<li>Constructorul clasei.</li>
     * 	<li> Va instantia obiectele de tip manager de activitati.</li>
     * </ul>
     */
    public GeneralManager() throws Exception {
        feedbackManager = new FeedbackManager();
        this.hearthBeatManager = new HearthBeatManager(generalManagerIpAddress);
        this.clientCommunicationManager = new ClientCommunicationManager();
        this.replicationManager = new ReplicationManager();
    }

    /**
     * <ul>
     * 	<li>Functia care porneste toate activitatile managerilor.</li>
     * 	<li>Apeluri de functii sau pornire de thread-uri.</li>
     * </ul>
     */
    public void startActivity() throws Exception {
        GeneralManager.pendingQueue = new PendingQueue();
        new Thread(hearthBeatManager).start();
        new Thread(replicationManager).start();
        new Thread(feedbackManager).start();
        clientCommunicationManager.clientCommunicationLoop();
    }

    /**
     * <ul>
     * 	<li>Functia main care va instantia si rula toti managerii de activitati.</li>
     * </ul>
     */
    public static void main(String[] args){
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