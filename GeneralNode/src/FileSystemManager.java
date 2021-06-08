import client_node.FileHeader;
import communication.Address;
import communication.Serializer;
import config.AppConfig;
import logger.LoggerService;
import model.VersionData;
import node_manager.*;
import os.FileSystem;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

/**
 * Clasa are ca obiectiv prelucrarile fisierelor;
 * Se bazeaza pe comunicarea cu nodul general si tratarea cererilor de prelucrare a fisierelor.
 * Replicare, eliminare, redenumire
 */
public class FileSystemManager implements Runnable{
    /** -------- Atribute -------- **/
    /**
     * Adresa socket-ului pe care este mapata comunicarea cu nodul general.
     */
    private final Address address;
    /**
     * Dimensiunea bufferului de receptie a datelor.
     */
    private static int bufferSize;
    /**
     * Portul pe care este mapata comunicarea cu nodul general.
     */
    private static int fileSystemMngrPort;
    /**
     * Calea de baza la care se vor stoca fisierele
     */
    private static String mainFilepath;


    /** -------- Constructor & Configurare -------- **/
    /**
     * Functie care citeste si initializeaza parametrii de configurare
     */
    public void readConfigParams(){
        fileSystemMngrPort = Integer.parseInt(AppConfig.getParam("replicationPort"));
        bufferSize = Integer.parseInt(AppConfig.getParam("buffersize"));
        mainFilepath = AppConfig.getParam("storagePath");

    }

    /**
     * Constructorul clasei;
     * Citeste si instantiaza parametrii de configurare
     */
    public FileSystemManager(String address) throws Exception{
        readConfigParams();
        this.address = new Address(address, fileSystemMngrPort);
    }


    /** -------- Main -------- **/
    /**
     * Functia de trimitere a replicii fisierului solicitat, catre nodul corespunzator.
     * @param userId Id-ul utilizatorului care detine fisierul.
     * @param filename Numele fisierului.
     * @param destionationAddress Adresa nodului.
     */
    public void sendReplication(String userId, String filename, String destionationAddress){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(destionationAddress, address.getPort());
                    DataOutputStream replicationOutputStream = new DataOutputStream(socket.getOutputStream());

                    File file = new File(mainFilepath + address.getIpAddress() + "/" + userId + "/" + filename);
                    BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));

                    FileHeader fileHeader = new FileHeader();
                    fileHeader.setFilename(filename);
                    fileHeader.setFilesize(file.length());
                    fileHeader.setUserId(userId);

                    replicationOutputStream.write(Serializer.serialize(fileHeader));
                    byte[] binaryFile = new byte[bufferSize];
                    int count;
                    while ((count = inputStream.read(binaryFile)) > 0) {
                        replicationOutputStream.write(binaryFile, 0, count);
                    }

                    inputStream.close();
                    replicationOutputStream.close();
                    socket.close();
                    LoggerService.registerSuccess(GeneralNode.ipAddress,"Replica fisierului " + filename + " a fost trimisa cu succes catre " + destionationAddress);
                }
                catch (IOException exception){
                    LoggerService.registerWarning(GeneralNode.ipAddress,"Exceptie la sursa de replicare [sendReplication] : " + exception.getMessage());
                }
            }
        }).start();
    }

    /**
     * Functia care inglobeaza bucla de comunicare cu clientul.
     * Se interpreteaza cererea, se determina tipul operatiei solicitate si se executa operatiunea
     * ceruta asupra fisierului.
     * @param clientSocket Socket-ul pe care se conecteaza nodul general.
     */
    public Runnable fileSystemManagerLoop(Socket clientSocket){
        return new Runnable() {
            @Override
            public void run() {
                try{
                    DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                    DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                    byte[] buffer = new byte[bufferSize];
                    boolean file_header_found = false;
                    FileOutputStream fileOutputStream = null;
                    int read;
                    String userId = "";
                    String filename = "";
                    while((read = dataInputStream.read(buffer, 0, bufferSize)) > 0){
                        try {
                            EditRequest fileSystemRequest = (EditRequest) Serializer.deserialize(buffer);
                            userId = fileSystemRequest.getUserId();
                            filename = fileSystemRequest.getFilename();

                            if(fileSystemRequest.getClass() == ReplicationRequest.class){
                                LoggerService.registerSuccess(GeneralNode.ipAddress,"Am primit comanda de replicare si trimit fisierul mai departe.");
                                String metadataFilename = FileSystem.changeFileExtension(filename, ".metadata");

                                for(String destionationAddress : ((ReplicationRequest)fileSystemRequest).getDestionationAddress()) {
                                    sendReplication(userId, filename, destionationAddress);
                                    sendReplication(userId, metadataFilename, destionationAddress);
                                }
                            }
                            else if(fileSystemRequest.getClass() == DeleteRequest.class){
                                LoggerService.registerSuccess(GeneralNode.ipAddress,"Am primit comanda de eliminare a fisierului.");
                                String filepath = mainFilepath + address.getIpAddress() + "/" + userId + "/" + filename;
                                int operation_status = FileSystem.deleteFile(filepath);
                                FeedbackResponse feedbackResponse = new FeedbackTextResponse();
                                switch (operation_status){
                                    case 0 : {
                                        LoggerService.registerError(GeneralNode.ipAddress,"Fisierul " + filename + " nu poate fi eliminat!");
                                        ((FeedbackTextResponse)feedbackResponse).setStatus("Fisierul nu poate fi eliminat");
                                        feedbackResponse.setSuccess(false);
                                        break;
                                    }
                                    case 1 : {
                                        LoggerService.registerSuccess(GeneralNode.ipAddress,"Fisierul " + filename + " a fost eliminat cu succes!");
                                        ((FeedbackTextResponse)feedbackResponse).setStatus("Fisierul a fost eliminat cu succes!");
                                        feedbackResponse.setSuccess(true);

                                        String metadataPath = FileSystem.changeFileExtension(filepath, ".metadata");
                                        FileSystem.deleteFile(metadataPath);
                                        break;
                                    }
                                }
                                sendFeedbackToGeneraManage(dataOutputStream, feedbackResponse);
                            }
                            else if(fileSystemRequest.getClass() == RenameRequest.class){
                                LoggerService.registerSuccess(GeneralNode.ipAddress,"Am primit comanda de redenumire.");
                                String originalPath = mainFilepath + address.getIpAddress() + "/" + userId + "/" + filename;
                                String newFilename = ((RenameRequest) fileSystemRequest).getNewName();
                                String newPath = mainFilepath + address.getIpAddress() + "/" + userId + "/" + newFilename;
                                int operation_status = FileSystem.renameFile(originalPath, newPath);
                                FeedbackResponse feedbackResponse = new FeedbackTextResponse();
                                switch (operation_status){
                                    case 0 : {
                                        LoggerService.registerError(GeneralNode.ipAddress,"Fisierul " + filename + " nu poate fi redenumit!");
                                        ((FeedbackTextResponse)feedbackResponse).setStatus("Eroare la redenumirea fisierului!");
                                        feedbackResponse.setSuccess(false);
                                        break;
                                    }
                                    case 1 : {
                                        LoggerService.registerSuccess(GeneralNode.ipAddress,"Fisierul " + filename + " a fost redenumit cu succes!");
                                        ((FeedbackTextResponse)feedbackResponse).setStatus("Fisierul a fost redenumit cu succes!");
                                        feedbackResponse.setSuccess(true);

                                        String metadataPath = FileSystem.changeFileExtension(originalPath, ".metadata");
                                        String newMetadataPath = FileSystem.changeFileExtension(newPath, ".metadata");
                                        FileSystem.renameFile(metadataPath, newMetadataPath);

                                        GeneralNode.versionControlManager.registerFileVersion(userId, newFilename, -1, fileSystemRequest.getDescription());
                                        break;
                                    }
                                    case 2 : {
                                        LoggerService.registerError(GeneralNode.ipAddress,"Nu se poate redenumi fisierul " + filename + "! Exista deja un fisier cu noul nume!");
                                        ((FeedbackTextResponse)feedbackResponse).setStatus("Nu se poate redenumi fisierul! Exista deja un fisier cu noul nume!");
                                        feedbackResponse.setSuccess(false);
                                        break;
                                    }
                                }
                                sendFeedbackToGeneraManage(dataOutputStream, feedbackResponse);
                            }
                            else if(fileSystemRequest.getClass() == VersionsRequest.class){
                                LoggerService.registerSuccess(GeneralNode.ipAddress,"Am primit cerere pentru istoricul unui fisier al unui user.");
                                List<VersionData> versionDataList = GeneralNode.versionControlManager.getVersionsForFile(userId, filename);
                                FeedbackComplexeResponse feedbackComplexeResponse = new FeedbackComplexeResponse();
                                feedbackComplexeResponse.setResponse(Collections.singletonList(versionDataList));
                                sendFeedbackToGeneraManage(dataOutputStream, feedbackComplexeResponse);
                            }
                        }
                        catch (ClassCastException exception){
                            // daca s-a generat aceasta exceptie, suntem nodul la care se va face replicarea
                            // asteptam sa primim un Fileheader
                            FileHeader fileHeader = (FileHeader)Serializer.deserialize(buffer);
                            String path = mainFilepath + address.getIpAddress() + "/" + fileHeader.getUserId();
                            if(!FileSystem.checkFileExistance(path))
                                FileSystem.createDir(path);
                            fileOutputStream = new FileOutputStream(path + "/" + fileHeader.getFilename());
                            file_header_found = true;
                        }
                        catch (StreamCorruptedException exception){
                            fileOutputStream.write(buffer, 0, read);
                        }
                    }
                    dataInputStream.close();
                    dataOutputStream.close();
                    if(file_header_found) {
                        LoggerService.registerSuccess(GeneralNode.ipAddress,"Receiving replication done!");
                        fileOutputStream.close();
                    }
                    clientSocket.close();

                }
                catch (IOException exception){
                    LoggerService.registerError(GeneralNode.ipAddress,"Filesystem manager loop exception IO : " + exception.getMessage() + " " + exception.getStackTrace().toString());
                }
                catch (ClassNotFoundException exception){
                    LoggerService.registerError(GeneralNode.ipAddress,"Filesystem manager loop exception ClassNotFound : " + exception.getMessage());
                }
            }
        };
    }



    public void sendFeedbackToGeneraManage(DataOutputStream dataOutputStream, FeedbackResponse feedbackResponse){
        try{
            dataOutputStream.write(Serializer.serialize(feedbackResponse));
        }
        catch (IOException exception){
            LoggerService.registerError(GeneralNode.ipAddress,"Exceptie IO la sendFeedbackToGeneraManage : " + exception.getMessage());
        }
    }

    public static void downloadFile(Socket socket, String userId, String filename) throws IOException {
        String filepath = "D:/Facultate/Licenta/Storage/" + GeneralNode.ipAddress + "/" + userId + "/" + filename;

        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filepath));

        byte[] binaryFile = new byte[bufferSize];
        int count;
        while ((count = inputStream.read(binaryFile)) > 0) {
            outputStream.write(binaryFile, 0, count);
        }

        inputStream.close();
        outputStream.close();
        LoggerService.registerSuccess(GeneralNode.ipAddress,"Fisierul " + filename + " a fost trimis cu succes catre download");
    }

    /**
     * Asteptarea conectarii nodului general si instantierea threadului de tratare a cererii
     */
    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(address.getIpAddress(), address.getPort()));
            while(true){
                Socket clientSocket = serverSocket.accept();
                new Thread(fileSystemManagerLoop(clientSocket)).start();
            }
        }
        catch (IOException exception){
            LoggerService.registerWarning(GeneralNode.ipAddress,"File System manager exception : " + exception.getMessage());
        }
        finally {
            try {
                serverSocket.close();
            }
            catch (IOException exception1){
                LoggerService.registerWarning(GeneralNode.ipAddress,"Exceptie la FileSystemManager : Nu putem inchide in siguranta ServerSocket-ul");
            }
        }
    }
}
