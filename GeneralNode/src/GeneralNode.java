import config.AppConfig;
import generalstructures.PendingList;
import logger.LoggerService;
import model.VersionData;
import node_manager.VersionsRequest;
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
 * <ul>
 * 	<li>Clasa generala a nodului intern.</li>
 * 	<li>Va realiza instantierea si comunicarea tuturor obiectelor care expun mecanismele specifice nodului intern.</li>
 * </ul>
 */
public class GeneralNode{
    /**
     * Adresa IP la care va fi mapat nodul intern
     */
    public static String ipAddress;
    /**
     * Calea de baza la care se vor stoca fisierele
     */
    private  static String storagePath = AppConfig.getParam("storagePath");
    /**
     * <ul>
     * 	<li>Flag care conditioneaza calcularea sumei de control a tuturor fisierelor din sistem.</li>
     * 	<li> Va fi initializat cu valoarea <strong>true</strong> astfel incat, la pornirea nodului sa se calculeze sumele de control.</li>
     * 	<li> Va fi resetat pe <strong>false</strong> ulterior pentru a evita calcularea sumelor de control.</li>
     * </ul>
     */
    private static boolean initFlag = true;

    /**
     * Obiectul care va fi trimis la nodul general sub forma de heartbeat
     **/
    private final static NodeBeat storageStatus = new NodeBeat();
    /**
     * <ul>
     * 	<li>Tabela sumelor de control, care va fi folosita pentru salvarea rezultatelor calcularii sumelor de control
     *       si pentru extragerea sumei de control ce va fi trimisa catre nodul general in cadrul unui <strong>heartbeat</strong>.</li>
     * </ul>
     */
    public final static CRCTable crcTable = new CRCTable();
    /**
     * <ul>
     * 	<li>Tabela fisierelor noi.</li>
     * 	<li> Vor fi stocate doar fisierelor noi, pentru care suma de control trebuie calculata imediat.</li>
     * </ul>
     */
    private final static NewFiles newFiles = new NewFiles();
    /**
     * <ul>
     * 	<li>Lista de asteptare, in care vor fi adaugate fisierele,
     *      in momentul in care sunt prelucrate in cadrul unei operatii solicitate de client.</li>
     * 	<li> Se evita astfel, calcularea sumei de control a fisierului.</li>
     * 	<li> Nu se doreste obtinerea unor valori eronate care sa genereze replicari.</li>
     * </ul>
     */
    public final static PendingList pendingList = new PendingList();

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
    /**
     * Obiectul care va gestiona toate prelucrarile versiunilor fisierelor.
     */
    public static VersionControlManager versionControlManager;

    /**
     * <ul>
     * 	<li>Constructorul clasei.</li>
     * 	<li> Realizeaza instantierea tuturor obiectelor care asigura mecanismele specifice nodului.</li>
     * </ul>
     */
    public GeneralNode(String ipAddress) throws Exception {
        this.hearthBeatManager = new HearthBeatManager(ipAddress);
        this.clientCommunicationManager = new ClientCommunicationManager(ipAddress);
        this.fileSystemManager = new FileSystemManager(ipAddress);
        versionControlManager = new VersionControlManager(ipAddress);
    }

    /**
     * <ul>
     * 	<li>Calculeaza sumele de control ale tuturor fisierelor stocate in memoria nodului.</li>
     * 	<li> Se tine cont de faptul ca, dupa finalizarea calculului, o suma de control se adauga in lista <strong>CRCTable</strong> urmand sa fie preluata imediat si trimisa catre nodul general.</li>
     * 	<li> Pentru a eficientiza procesul trimiterii sumelor de control ale fisierelor de dimensiuni mici si pentru a reduce timpul global de calculare a sumelor, se paralelizeaza intregul mecanism.</li>
     * 	<li> Fiecare fisier va fi analizat in mod separat.</li>
     * 	<li> Se ignora fisierele de matedate.</li>
     * </ul>
     */
    public static void calculateFileSystemCRC(){
        String path = storagePath + ipAddress;

        for (String userDir : FileSystem.getDirContent(path)) {
            String[] userFiles = FileSystem.getDirContent(path + "\\" + userDir);
            for(String file : userFiles){
                if(file.contains(VersionControlManager.extension))
                    continue;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long crc = FileSystem.calculateCRC(path + "\\" + userDir + "\\" + file);
                        crcTable.updateRegister(userDir, file, crc);
                        System.out.println("Am modificat crc-ul pentru [" +userDir + ":" + file +"]");
                    }
                }).start();
            }
        }
    }

    /**
     * <ul>
     * 	<li>Daca in tabela <strong>CRCTable</strong> fisierele au suma de control <strong>-1</strong>, nu se va recalcula suma de control.</li>
     * 	<li>In schimb, suma de control se va calcula doar la cererea nodului general.</li>
     * 	<li>Daca fisierul curent se afla intr-o operatie de prelucrare, fapt evidentiat prin prezenta in lista <strong>PendingList</strong>,
     *      se va transmiterea datelor acestora.</li>
     * 	<li>Pe langa datele de identificare ale unui fisier, se vor include si date despre ultima versiune a fisierului.</li>
     * </ul>
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
                    VersionData versionData = versionControlManager.getLastVersionOfFile(userDir, file);
                    String versionName = versionData.getVersionName();
                    String versionDescription = versionData.getDescription();
                    f.setVersionNo(versionName);
                    f.setDescription(versionDescription);
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

    /**
     * <ul>
     * 	<li>Functia care porneste toate activitatile managerilor.</li>
     * 	<li>Apeluri de functii sau pornire de thread-uri.</li>
     * </ul>
     */
    public void StartActivity() throws Exception {
        getStorageStatus();

        new Thread(hearthBeatManager).start();

        new Thread(fileSystemManager).start();

        clientCommunicationManager.clientCommunicationLoop();
    }

    /**
     * <ul>
     * 	<li>Functia main.</li>
     * 	<li> Va instantia si porni toate mecanismele specifice nodului intern.</li>
     * </ul>
     */
    public static void main(String[] args) {
        try {
            ipAddress = args[0];
            GeneralNode generalNode = new GeneralNode(ipAddress);
            generalNode.StartActivity();
        }

        catch (Exception exception){
            LoggerService.registerError(GeneralNode.ipAddress,"Exceptie la GeneralNode main : " + exception.getMessage());
        }
    }
}