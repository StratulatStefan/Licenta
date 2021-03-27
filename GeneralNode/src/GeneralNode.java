import communication.Address;
import config.AppConfig;
import node_manager.NodeBeat;
import os.FileSystem;

import java.io.*;
import java.lang.*;

/* https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/MulticastSocket.html */
/* https://tldp.org/HOWTO/Multicast-HOWTO.html#toc1 */
public class GeneralNode{
    private static String ipAddress;

    private  static String storagePath;

    private final static NodeBeat storageStatus = new NodeBeat();


    /* ------------------------------------------------------------------------------ */


    /**
     * Obiectul care se ocupa de mecanismul de hearbeats
     */
    private HearthBeatManager hearthBeatManager;

    /**
     * Obiectul care se va ocupa de comunicarea cu clientul
     */
    private ClientCommunicationManager clientCommunicationManager;

    private FileSystemManager fileSystemManager;


    /* ------------------------------------------------------------------------------ */


    /**
     * Constructorul clasei
     */
    public GeneralNode(String ipAddress) throws Exception {
        this.hearthBeatManager = new HearthBeatManager(ipAddress);
        this.clientCommunicationManager = new ClientCommunicationManager(ipAddress);
        this.fileSystemManager = new FileSystemManager(ipAddress);
    }

    public void StartActivity() throws Exception {
        getStorageStatus();

        new Thread(hearthBeatManager).start();

        new Thread(fileSystemManager).start();

        clientCommunicationManager.clientCommunicationLoop();
    }

    public static NodeBeat getStorageStatus() throws IOException {
        storageStatus.cleanUp();
        String path = storagePath + ipAddress;
        if(!FileSystem.checkFileExistance(path)){
            FileSystem.createDir(path);
            System.out.println("No status defined yet!");
            return null;
        }
        String [] usersDirectories = FileSystem.getDirContent(path);

        for (String userDir : usersDirectories) {
            String[] userFiles = FileSystem.getDirContent(path + "\\" + userDir);
            storageStatus.addUserFiles(userDir, userFiles);
        }
        return storageStatus;
    }


    /* ------------------------------------------------------------------------------ */

    public static void readConfigParams(){
        storagePath = AppConfig.getParam("storagePath");
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