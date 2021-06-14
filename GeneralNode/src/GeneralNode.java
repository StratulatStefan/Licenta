import config.AppConfig;
import generalstructures.PendingList;
import logger.LoggerService;
import tables.CRCTable;
import model.NewFiles;
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
    public static String ipAddress;
    /**
     * Calea de baza la care se vor stoca fisierele
     */
    private  static String storagePath = AppConfig.getParam("storagePath");
    private static boolean initFlag = true;

    /**
     * Obiectul care va fi trimis la nodul general sub forma de heartbeat
     * **/
    private final static NodeBeat storageStatus = new NodeBeat();
    public final static CRCTable crcTable = new CRCTable();
    private final static NewFiles newFiles = new NewFiles();
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
    public static VersionControlManager versionControlManager;


    /** -------- Constructor & Configurare -------- **/

    /**
     * Constructorul clasei
     */
    public GeneralNode(String ipAddress) throws Exception {
        this.hearthBeatManager = new HearthBeatManager(ipAddress);
        this.clientCommunicationManager = new ClientCommunicationManager(ipAddress);
        this.fileSystemManager = new FileSystemManager(ipAddress);
        versionControlManager = new VersionControlManager(ipAddress);
    }


    /** -------- Getter -------- **/

    public static void calculateFileSystemCRC(){
        String path = storagePath + ipAddress;

        for (String userDir : FileSystem.getDirContent(path)) {
            String[] userFiles = FileSystem.getDirContent(path + "\\" + userDir);
            for(String file : userFiles){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long crc = FileSystem.calculateCRC(path + "\\" + userDir + "\\" + file);
                        crcTable.updateRegister(userDir, file, crc);
                        LoggerService.registerSuccess(GeneralNode.ipAddress,"Am modificat crc-ul pentru [" +userDir + ":" + file +"]");
                    }
                }).start();
            }
        }
    }

    /**
     * TODO rework description
     * Functie care acceseaza filesystem-ul si determina statusul stocarii nodului curent;
     * Extrage toti utilizatorii si fisierele din stocarea nodului curent, si compune heartBeat-ul
     * ce va fi trimis catre nodul general.
     * Pentru a se evita instantierea unui nou beat la perioade regulate de timp, heartbeat-ul are o singura instanta,
     * care se trimite mereu catre nodul general, doar ca se modifica valorile acestuia
     * @return Heartbeat-ul (acelasi, dar cu alte valori)
     */
    public static NodeBeat getStorageStatus() throws IOException {
        // la primul beat, se trimite neaparat tot statusul
        if(initFlag){
            try {
                calculateFileSystemCRC();
            }
            catch (Exception exception){
                // se genereaza daca nu gaseste niciun fisier
            }
            initFlag = false;
        }

        storageStatus.cleanUp();
        String path = storagePath + ipAddress;
        if(!FileSystem.checkFileExistance(path)){
            FileSystem.createDir(path);
            System.out.println("No status defined yet!");
            newFiles.clean();
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
                if(file.contains("metadata")){
                    continue;
                }
                if(!pendingList.containsRegister(userDir, file)) {
                    FileAttribute f = new FileAttribute();
                    f.setFilename(file);
                    if(!newFiles.containsRegister(userDir, file)){
                        f.setCrc(FileSystem.calculateCRC(path + "\\" + userDir + "\\" + file));
                        newFiles.addNewFile(userDir, file);
                    }
                    else{
                        f.setCrc(crcTable.getCrcForFile(userDir, file));
                        crcTable.resetRegister(userDir, file);
                    }
                    String versionName = versionControlManager.getLastVersionOfFile(userDir, file).getVersionName();
                    f.setVersionNo(versionName);
                    f.setFilesize(FileSystem.getFileSize(path + "\\" + userDir + "\\" + file));
                    fileAttributes.add(f);
                }
                else{
                    LoggerService.registerWarning(GeneralNode.ipAddress,"File ignored because it is in pending : " + file);
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
        try {
            ipAddress = args[0];

            GeneralNode generalManager = new GeneralNode(ipAddress);
            generalManager.StartActivity();
        }

        catch (Exception exception){
            LoggerService.registerError(GeneralNode.ipAddress,"Exceptie la GeneralNode main : " + exception.getMessage());
        }
    }
}