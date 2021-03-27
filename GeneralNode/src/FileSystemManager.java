import client_node.FileHeader;
import communication.Address;
import communication.Serializer;
import config.AppConfig;
import node_manager.DeleteRequest;
import node_manager.EditRequest;
import node_manager.RenameRequest;
import node_manager.ReplicationRequest;
import os.FileSystem;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class FileSystemManager implements Runnable{
    private final Address address;
    private static int bufferSize;
    private static int fileSystemMngrPort;
    private static String mainFilepath;


    /* ------------------------------------------------------------------------------ */


    public void readConfigParams(){
        fileSystemMngrPort = Integer.parseInt(AppConfig.getParam("replicationPort"));
        bufferSize = Integer.parseInt(AppConfig.getParam("buffersize"));
        mainFilepath = AppConfig.getParam("storagePath");

    }

    public FileSystemManager(String address) throws Exception{
        readConfigParams();
        this.address = new Address(address, fileSystemMngrPort);
    }

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
                                FileSystem.deleteFile(filepath);
                            }
                            else if(fileSystemRequest.getClass() == RenameRequest.class){
                                System.out.println("Am primit comanda de redenumire. Dam drumu la treaba imediat");
                                String originalPath = mainFilepath + address.getIpAddress() + "/" + userId + "/" + filename;
                                String newPath = mainFilepath + address.getIpAddress() + "/" + userId + "/" + ((RenameRequest) fileSystemRequest).getNewName();
                                FileSystem.renameFile(originalPath, newPath);
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


    /* ------------------------------------------------------------------------------ */


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
