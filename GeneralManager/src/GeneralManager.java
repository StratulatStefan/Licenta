import communication.Address;
import config.AppConfig;
import model.ConnectionTable;
import model.ContentTable;
import model.StorageStatusTable;

import java.lang.*;

/* https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/MulticastSocket.html */
/* https://tldp.org/HOWTO/Multicast-HOWTO.html#toc1 */
public class GeneralManager{
    /** -------- Atribute generale -------- **/
    /**
     * Adresa IP la care va fi mapat managerul general
     */
    private static String generalManagerIpAddress;


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

    /**
     * Obiectul care se va ocupa de prelucrarea fisierelor
     */
    public static FileSystemManager fileSystemManager;


    /** -------- Functii de initializare -------- **/
    /**
     * Functie care citeste si initializeaza parametrii de configurare
     */
    public static void readConfigParams(){
        generalManagerIpAddress = AppConfig.getParam("generalManagerIpAddress");
    }

    /**
     * Constructorul clasei
     */
    public GeneralManager() throws Exception {
        this.hearthBeatManager = new HearthBeatManager(generalManagerIpAddress);
        this.clientCommunicationManager = new ClientCommunicationManager();
        this.replicationManager = new ReplicationManager();
        this.fileSystemManager = new FileSystemManager();
    }


    /** -------- Main -------- **/
    /**
     * Functia care porneste toate activitatile managerilor;
     * Apeluri de functii sau pornire de thread-uri
     */
    public void startActivity() throws Exception {
        // Pornim thread-ul pe care va fi rulat mecanismul de heartbeats
        new Thread(hearthBeatManager).start();

        new Thread(replicationManager).start();

        clientCommunicationManager.clientCommunicationLoop(generalManagerIpAddress);
    }

    /**
     * @param args Argumentele furnizate la linia de comanda
     */
    public static void main(String[] args){
        AppConfig.readConfig();
        readConfigParams();

        try {
            GeneralManager generalManager = new GeneralManager();
            generalManager.startActivity();
        }
        catch (Exception exception){
            System.out.println("Exceptie la GeneralManagerMain : " + exception.getMessage());
        }
    }
}