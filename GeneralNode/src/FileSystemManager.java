import client_node.FileHeader;
import client_node.NewFileRequestFeedback;
import communication.Address;
import communication.Serializer;
import config.AppConfig;
import node_manager.*;
import os.FileSystem;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

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
                }
                catch (IOException exception){
                    System.out.println("Exceptie la sursa de replicare [sendReplication] : " + exception.getMessage());
                }
            }
        }).start();
        System.out.println("Replica trimisa cu succes catre " + destionationAddress);
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
                    while((read = dataInputStream.read(buffer, 0, bufferSize)) > 0){
                        try {
                            EditRequest fileSystemRequest = (EditRequest) Serializer.deserialize(buffer);
                            String userId = fileSystemRequest.getUserId();
                            String filename = fileSystemRequest.getFilename();

                            if(fileSystemRequest.getClass() == ReplicationRequest.class){
                                System.out.println("Am primit comanda de replicare si trimit fisierul mai departe.");
                                for(String destionationAddress : ((ReplicationRequest)fileSystemRequest).getDestionationAddress()) {
                                    sendReplication(userId, filename, destionationAddress);
                                }
                            }
                            else if(fileSystemRequest.getClass() == DeleteRequest.class){
                                System.out.println("Am primit comanda de eliminare a fisierului.");
                                String filepath = mainFilepath + address.getIpAddress() + "/" + userId + "/" + filename;
                                int operation_status = FileSystem.deleteFile(filepath);
                                FeedbackResponse feedbackResponse = new FeedbackResponse();
                                switch (operation_status){
                                    case 0 : {
                                        feedbackResponse.setStatus("Fisierul nu poate fi eliminat");
                                        feedbackResponse.setSuccess(false);
                                        break;
                                    }
                                    case 1 : {
                                        feedbackResponse.setStatus("Fisierul a fost eliminat cu succes!");
                                        feedbackResponse.setSuccess(true);
                                        break;
                                    }
                                }
                                sendFeedbackToGeneraManage(dataOutputStream, feedbackResponse);
                            }
                            else if(fileSystemRequest.getClass() == RenameRequest.class){
                                System.out.println("Am primit comanda de redenumire. Dam drumu la treaba imediat");
                                String originalPath = mainFilepath + address.getIpAddress() + "/" + userId + "/" + filename;
                                String newPath = mainFilepath + address.getIpAddress() + "/" + userId + "/" + ((RenameRequest) fileSystemRequest).getNewName();
                                int operation_status = FileSystem.renameFile(originalPath, newPath);
                                FeedbackResponse feedbackResponse = new FeedbackResponse();
                                switch (operation_status){
                                    case 0 : {
                                        feedbackResponse.setStatus("Eroare la redenumirea fisierului!");
                                        feedbackResponse.setSuccess(false);
                                        break;
                                    }
                                    case 1 : {
                                        feedbackResponse.setStatus("Fisierul a fost redenumit cu succes!");
                                        feedbackResponse.setSuccess(true);
                                        break;
                                    }
                                    case 2 : {
                                        feedbackResponse.setStatus("Nu se poate redenumi fisierul! Exista deja un fisier cu noul nume!");
                                        feedbackResponse.setSuccess(false);
                                        break;
                                    }
                                }
                                sendFeedbackToGeneraManage(dataOutputStream, feedbackResponse);
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
                        System.out.println("Receiving replication done!");
                        fileOutputStream.close();
                    }
                    clientSocket.close();

                }
                catch (IOException exception){
                    System.out.println("Filesystem manager loop exception IO : " + exception.getMessage() + " " + exception.getStackTrace().toString());
                }
                catch (ClassNotFoundException exception){
                    System.out.println("Filesystem manager loop exception ClassNotFound : " + exception.getMessage());
                }
            }
        };
    }

    public void sendFeedbackToGeneraManage(DataOutputStream dataOutputStream, FeedbackResponse feedbackResponse){
        try{
            dataOutputStream.write(Serializer.serialize(feedbackResponse));
        }
        catch (IOException exception){
            System.out.println("Exceptie IO la sendFeedbackToGeneraManage : " + exception.getMessage());
        }
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
            System.out.println("File System manager exception : " + exception.getMessage());
            try {
                serverSocket.close();
            }
            catch (IOException exception1){
                System.out.println("Exceptie la FileSystemManager : Nu putem inchide in siguranta ServerSocket-ul");
            }
        }
    }


}
