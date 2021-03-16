import client_node.FileHeader;
import communication.Address;
import communication.Serializer;
import node_manager.ReplicationRequest;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ReplicationManager implements Runnable{
    private Address address;
    private static int bufferSize = 1024;
    private static String mainFilepath = "D:/Facultate/Licenta/Storage/";

    public ReplicationManager(Address address){
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
                new Thread(ReplicationLoop(clientSocket)).start();
            }
        }
        catch (Exception exception){
            try {
                serverSocket.close();
            }
            catch (Exception exception1){

            }
            System.out.println("Replication manager exception : " + exception.getMessage());
        }
    }

    public Runnable ReplicationLoop(Socket clientSocket){
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
                            ReplicationRequest replicationRequest = (ReplicationRequest) Serializer.Deserialize(buffer);
                            // daca nu s-a generat StreamCorruptedException, suntem nodul sursa, de la care incepe replicarea
                            String filepath = mainFilepath + address.getIpAddress() + "/" + replicationRequest.getUserId() + "/" + replicationRequest.getFilename();
                            System.out.println("Sunt nodul sursa si trimit comanda!!!");
                            Socket socket = new Socket(replicationRequest.getDestionationAddress(), address.getPort());
                            DataOutputStream replicationOutputStream = new DataOutputStream(socket.getOutputStream());

                            File file = new File(filepath);
                            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));

                            FileHeader fileHeader = new FileHeader();
                            fileHeader.setFilename(replicationRequest.getFilename());
                            fileHeader.setFilesize(file.length());
                            fileHeader.setUserId(replicationRequest.getUserId());

                            replicationOutputStream.write(Serializer.Serialize(fileHeader));
                            byte[] binaryFile = new byte[bufferSize];
                            int count;
                            while ((count = inputStream.read(binaryFile)) > 0){
                                replicationOutputStream.write(binaryFile, 0, count);
                            }

                            inputStream.close();
                            replicationOutputStream.close();
                            socket.close();
                            System.out.println("Sending replication done!");
                        }
                        catch (Exception exception){
                            // daca s-a generat aceasta exceptie, suntem nodul la care se va face replicarea
                            // asteptam sa primim un Fileheader
                            if(!file_header_found){
                                FileHeader fileHeader = (FileHeader)Serializer.Deserialize(buffer);
                                String path = mainFilepath + address.getIpAddress() + "/" + fileHeader.getUserId();
                                if(!Files.exists(Paths.get(path)))
                                    Files.createDirectories(Paths.get(path));
                                path += "/" + fileHeader.getFilename();
                                fileOutputStream = new FileOutputStream(path);
                                file_header_found = true;
                            }
                            else{
                                fileOutputStream.write(buffer, 0, read);
                            }
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
                   System.out.println("Replication manager loop exception IO : " + exception.getMessage());
                }
                catch (ClassNotFoundException exception){
                    System.out.println("Replication manager loop exception ClassNotFound : " + exception.getMessage());
                }
            }
        };
    }
}
