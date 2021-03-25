import communication.Address;
import node_manager.NodeBeat;
import os.FileSystem;

import java.io.*;
import java.lang.*;

/* https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/MulticastSocket.html */
/* https://tldp.org/HOWTO/Multicast-HOWTO.html#toc1 */
public class GeneralNode{
    private static String ipAddress;

    /**
     * Portul pe care fi mapata ServerSocket-ul
     */
    private final static int serverSocketPort = 8081;

    /**
     * Portul pe care va fi mapata comunicarea multicast
     */
    private final static int heartBeatPort = 8246;

    private final static int fileSystemMngrPort = 8082;

    /**
     * Obiectul care se ocupa de mecanismul de hearbeats
     */
    private HearthBeatManager hearthBeatManager;

    /**
     * Obiectul care se va ocupa de comunicarea cu clientul
     */
    private ClientCommunicationManager clientCommunicationManager;

    private FileSystemManager fileSystemManager;

    private final static String storagePath = "D:\\Facultate\\Licenta\\Storage\\";

    private final static NodeBeat storageStatus = new NodeBeat();

    /**
     * Constructorul clasei
     * @param hearthBeatManager Managerul de heartbeat-uri
     */
    public GeneralNode(HearthBeatManager hearthBeatManager, ClientCommunicationManager clientCommunicationManager, FileSystemManager fileSystemManager){
        this.hearthBeatManager = hearthBeatManager;
        this.clientCommunicationManager = clientCommunicationManager;
        this.fileSystemManager = fileSystemManager;
    }

    public void StartActivity() throws Exception {
        getStorageStatus();

        // Pornim thread-ul pe care va fi rulat mecanismul de heartbeats
        new Thread(hearthBeatManager).start();

        new Thread(fileSystemManager).start();

        // Pornim comunicarea cu clientul
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

    /**
     * @param args Argumentele furnizate la linia de comanda
     */
    public static void main(String[] args) {
        try {
            ipAddress = args[0];

            Address hearthBeatAddress = new Address(ipAddress, heartBeatPort);
            HearthBeatManager hearthBeatManager = new HearthBeatManager(hearthBeatAddress);

            Address clientCommunicationAddress = new Address(ipAddress, serverSocketPort);
            InternalNodeCommunicationManager internalCommunicationManager = new InternalNodeCommunicationManager();
            ClientCommunicationManager clientCommunicationManager = new ClientCommunicationManager(clientCommunicationAddress, internalCommunicationManager);

            Address filesystemManagerAddress = new Address(ipAddress, fileSystemMngrPort);
            FileSystemManager fileSystemManager = new FileSystemManager(filesystemManagerAddress);
            GeneralNode generalManager = new GeneralNode(hearthBeatManager, clientCommunicationManager, fileSystemManager);
            generalManager.StartActivity();
        }

        catch (Exception exception){
            System.out.println(exception.getMessage());
        }
    }
}