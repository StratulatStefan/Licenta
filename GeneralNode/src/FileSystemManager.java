import client_node.FileHeader;
import communication.Address;
import communication.Serializer;
import node_manager.DeleteRequest;
import node_manager.EditRequest;
import node_manager.RenameRequest;
import node_manager.ReplicationRequest;
import os.FileSystem;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileSystemManager implements Runnable{
    private Address address;
    private static int bufferSize = 1024;
    private static String mainFilepath = "D:/Facultate/Licenta/Storage/";

    public FileSystemManager(Address address){
        this.address = address;
    }

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
        catch (Exception exception){
            try {
                serverSocket.close();
            }
            catch (Exception exception1){

            }
            System.out.println("File System manager exception : " + exception.getMessage());
        }
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
                            if(fileSystemRequest.getClass() == ReplicationRequest.class){
                                String filepath = mainFilepath + address.getIpAddress() + "/" + fileSystemRequest.getUserId() + "/" + fileSystemRequest.getFilename();
                                System.out.println("Am primit comanda de replicare si trimit fisierul mai departe.");
                                for(String ipAddress : ((ReplicationRequest)fileSystemRequest).getDestionationAddress()) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Socket socket = new Socket(ipAddress, address.getPort());
                                                DataOutputStream replicationOutputStream = new DataOutputStream(socket.getOutputStream());

                                                File file = new File(filepath);
                                                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));

                                                FileHeader fileHeader = new FileHeader();
                                                fileHeader.setFilename(fileSystemRequest.getFilename());
                                                fileHeader.setFilesize(file.length());
                                                fileHeader.setUserId(fileSystemRequest.getUserId());

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
                                                System.out.println("Exceptie la sursa de replicare : " + exception.getMessage());
                                            }
                                        }
                                    }).start();
                                    System.out.println("Sending replication to " + ipAddress +" done!");
                                }
                            }
                            else if(fileSystemRequest.getClass() == DeleteRequest.class){
                                String filepath = mainFilepath + address.getIpAddress() + "/" + fileSystemRequest.getUserId() + "/" + fileSystemRequest.getFilename();
                                System.out.println("Am primit comanda de eliminare a fisierului.");
                                FileSystem.deleteFile(filepath);
                            }
                            else if(fileSystemRequest.getClass() == RenameRequest.class){
                                System.out.println("Am primit comanda de redenumire. Dam drumu la treaba imediat");
                                String originalPath = mainFilepath + address.getIpAddress() + "/" + fileSystemRequest.getUserId() + "/" + fileSystemRequest.getFilename();
                                String newPath = mainFilepath + address.getIpAddress() + "/" + fileSystemRequest.getUserId() + "/" + ((RenameRequest) fileSystemRequest).getNewname();
                                FileSystem.renameFile(originalPath, newPath);
                            }
                        }
                        catch (StreamCorruptedException exception){
                            fileOutputStream.write(buffer, 0, read);
                        }
                        catch (ClassCastException exception){
                            // daca s-a generat aceasta exceptie, suntem nodul la care se va face replicarea
                            // asteptam sa primim un Fileheader
                            FileHeader fileHeader = (FileHeader)Serializer.deserialize(buffer);
                            String path = mainFilepath + address.getIpAddress() + "/" + fileHeader.getUserId();
                            if(!FileSystem.checkFileExistance(path))
                                FileSystem.createDir(path);
                            path += "/" + fileHeader.getFilename();
                            fileOutputStream = new FileOutputStream(path);
                            file_header_found = true;
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

}
