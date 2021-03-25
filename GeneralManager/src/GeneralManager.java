import communication.Address;
import config.AppConfig;
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
    private static String generalManagerIpAddress;

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

        clientCommunicationManager.clientCommunicationLoop(generalManagerIpAddress);
    }

    public static void readConfigParams(){
        generalManagerIpAddress = AppConfig.getParam("generalManagerIpAddress");
    }

    /**
     * @param args Argumentele furnizate la linia de comanda
     */
    public static void main(String[] args){
        AppConfig.readConfig();
        readConfigParams();

        try {
            HearthBeatManager hearthBeatManager = new HearthBeatManager(generalManagerIpAddress);
            ClientCommunicationManager clientCommunicationManager = new ClientCommunicationManager();
            ReplicationManager replicationManager = new ReplicationManager();
            FileSystemManager editRequestManager = new FileSystemManager();

            GeneralManager generalManager = new GeneralManager(hearthBeatManager, clientCommunicationManager, replicationManager, editRequestManager);
            generalManager.startActivity();
        }
        catch (Exception exception){
            System.out.println("Exceptie la main : " + exception.getMessage());
        }
    }
}