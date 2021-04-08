import communication.Address;
import config.AppConfig;
import node_manager.Beat.FileAttribute;
import node_manager.Beat.NodeBeat;
import os.FileSystem;

import java.io.*;
import java.lang.*;
import java.util.ArrayList;
import java.util.List;

/* https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/MulticastSocket.html */
/* https://tldp.org/HOWTO/Multicast-HOWTO.html#toc1 */

/**
 * Clasa generala a nodului intern.
 */
public class GeneralNode{
    /** -------- Atribute -------- **/
    /**
     * Adresa IP la care va fi mapat nodul intern
     */
    private static String ipAddress;
    /**
     * Calea de baza la care se vor stoca fisierele
     */
    private  static String storagePath;
    /**
     * Obiectul care va fi trimis la nodul general sub forma de heartbeat
     * **/
    private final static NodeBeat storageStatus = new NodeBeat();


    public final static PendingList pendingList = new PendingList();

    /** -------- Managerii activitatilor -------- **/
    /**
     * Obiectul care se ocupa de mecanismul de hearbeats
     */
    private HearthBeatManager hearthBeatManager;

    /**
     * Obiectul care se va ocupa de comunicarea cu clientul
     */
    private ClientCommunicationManager clientCommunicationManager;

    /**
     * Obiectul care se va ocupa de comunicarea cu nodul general pentru prelucrarea fisiere.
     */
    private FileSystemManager fileSystemManager;


    /** -------- Constructor & Configurare -------- **/
    /**
     * Functie care citeste si initializeaza parametrii de configurare
     */
    public static void readConfigParams(){
        storagePath = AppConfig.getParam("storagePath");
    }

    /**
     * Constructorul clasei
     */
    public GeneralNode(String ipAddress) throws Exception {
        this.hearthBeatManager = new HearthBeatManager(ipAddress);
        this.clientCommunicationManager = new ClientCommunicationManager(ipAddress);
        this.fileSystemManager = new FileSystemManager(ipAddress);
    }


    /** -------- Getter -------- **/
    /**
     * Functie care acceseaza filesystem-ul si determina statusul stocarii nodului curent;
     * Extrage toti utilizatorii si fisierele din stocarea nodului curent, si compune heartBeat-ul
     * ce va fi trimis catre nodul general.
     * Pentru a se evita instantierea unui nou beat la perioade regulate de timp, heartbeat-ul are o singura instanta,
     * care se trimite mereu catre nodul general, doar ca se modifica valorile acestuia
     * @return Heartbeat-ul (acelasi, dar cu alte valori)
     */
    public static NodeBeat getStorageStatus() throws IOException {
        storageStatus.cleanUp();
        String path = storagePath + ipAddress;
        if(!FileSystem.checkFileExistance(path)){
            FileSystem.createDir(path);
            System.out.println("No status defined yet!");
            return null;
        }

        try {
            storageStatus.setMemoryQuantity(FileSystem.getFileSize(path));
        }
        catch (IOException exception){
            // fuck off!
        }
        String [] usersDirectories = FileSystem.getDirContent(path);

        for (String userDir : usersDirectories) {
            String[] userFiles = FileSystem.getDirContent(path + "\\" + userDir);
            List<FileAttribute> fileAttributes = new ArrayList<>();
            for(String file : userFiles){
                if(!pendingList.containsRegister(userDir, file)) {
                    FileAttribute f = new FileAttribute();
                    f.setFilename(file);
                    long startTime = System.currentTimeMillis();
                    f.setCrc(FileSystem.calculateCRC(path + "\\" + userDir + "\\" + file));
                    long estimatedTime = System.currentTimeMillis() - startTime;
                    System.out.println("CRC for " + file + " : " + estimatedTime + " ms");
                    fileAttributes.add(f);

                }
                else{
                    System.out.println("File ignored because it is in pending : " + file);
                    //f.setCrc(0);
                }
            }
            storageStatus.addUserFiles(userDir, fileAttributes);
        }
        return storageStatus;
    }


    /** -------- Main -------- **/
    /**
     * Functia care porneste toate activitatile managerilor;
     * Apeluri de functii sau pornire de thread-uri
     */
    public void StartActivity() throws Exception {
        getStorageStatus();

        new Thread(hearthBeatManager).start();

        new Thread(fileSystemManager).start();

        clientCommunicationManager.clientCommunicationLoop();
    }

    /**
     * @param args Argumentele furnizate la linia de comanda
     */
    public static void main(String[] args) {
        AppConfig.readConfig();
        readConfigParams();
        try {
            ipAddress = args[0];

            GeneralNode generalManager = new GeneralNode(ipAddress);
            generalManager.StartActivity();
        }

        catch (Exception exception){
            System.out.println("Exceptie la GeneralNode main : " + exception.getMessage());
        }
    }
}