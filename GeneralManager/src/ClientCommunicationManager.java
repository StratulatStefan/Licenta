import client_manager.ManagerResponse;
import client_manager.data.ClientManagerRequest;
import client_manager.data.DeleteFileRequest;
import client_manager.data.NewFileRequest;
import client_manager.data.RenameFileRequest;
import communication.Address;
import communication.Serializer;
import config.AppConfig;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ClientCommunicationManager {
    /**
     * Enum care va cuprinde toate tipurile de interactiune cu clientul
     */
    public enum ClientRequest{
        NEW_FILE,
        DELETE_FILE,
        RENAME_FILE
    }

    public enum ClientRequestStatus{
        FILE_EXISTS,
        FILE_NOT_FOUND
    }

    /**
     * Dimensiunea bufferului in care vor fi citite datele de la un nod adiacent
     */
    private static int bufferSize;

    private static int dataTransmissionPort;

    public ClientCommunicationManager(){
        readConfigParams();
    }

    public void readConfigParams(){
        bufferSize = Integer.parseInt(AppConfig.getParam("buffersize"));
        dataTransmissionPort = Integer.parseInt(AppConfig.getParam("dataTransmissionPort"));
    }

    /**
     * Functie care identifica tipul operatiei solicitate de utilizator
     * @param operation String-ul ce identifica operatia
     * @return Tipul operatiei sau null daca nu s-a identificat nicio operatie valida
     */
    public ClientRequest getOperationType(Class<? extends ClientManagerRequest> operation){
        if(operation == NewFileRequest.class){
            return ClientRequest.NEW_FILE;
        }
        if(operation == DeleteFileRequest.class){
            return ClientRequest.DELETE_FILE;
        }
        if(operation == RenameFileRequest.class){
            return ClientRequest.RENAME_FILE;
        }
        return null;
    }

    /**
     * Functie care va genera lantul de noduri la care se va stoca un fisier nou aparut in sistem
     * @param filesize Dimensiunea fisierului ce va fi stocat
     * @param replication_factor Factorul de replicare al fisierului
     * @return Lantul de noduri la care se va stoca fisierului
     */
    public String generateChain(int filesize, int replication_factor){
        System.out.println("User uploaded a new file with size : " + filesize + " and replication factor : " + replication_factor);
        List<String> connectionAddresses = GeneralManager.connectionTable.getConnectionTable();
        if(connectionAddresses.size() <  replication_factor){
            System.out.println("Nu sunt suficiente noduri disponibile");
        }
        else{
            System.out.println("Generam token-ul..");
            StringBuilder token = new StringBuilder();
            /* !!!!!! criteriu de selectare a nodurilor !!!!!! */
            for(String address : connectionAddresses){
                if(replication_factor == 0){
                    break;
                }
                token.append(address).append("-");
                replication_factor -= 1;
            }
            token = new StringBuilder(token.substring(0, token.length() - 1));
            return token.toString();
        }
        return null;
    }

    public void registerUserNewFileRequest(String user, String filename, int replication_factor){
        synchronized (GeneralManager.contentTable){
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep((int) (5 * 1e3));
                            GeneralManager.contentTable.addRegister(user, filename, replication_factor);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                }).start();
            }
            catch (Exception exception){
                System.out.println("registerUserNewFileRequest exception : " + exception.getMessage());
            }
        }
    }

    public ClientRequestStatus checkFileStatus(String user, String filename){
        boolean fileStatus = GeneralManager.contentTable.checkForUserFile(user, filename);
        if(fileStatus){
            return ClientRequestStatus.FILE_EXISTS;
        }
        return ClientRequestStatus.FILE_NOT_FOUND;
    }

    /** Functie care inglobeaza activitatea principala a fiecarui nod, aceea de a asigura comunicarea cu celelalte noduri
     * in vederea trimiterii si primirii de mesaje.
     */
    public void clientCommunicationLoop(String generalManagerIpAddress) throws Exception{
        Address address = new Address(generalManagerIpAddress, dataTransmissionPort);
        ServerSocket serverSocket = new ServerSocket();
        try{
            serverSocket.bind(new InetSocketAddress(address.getIpAddress(), address.getPort()));
            while(true){
                Socket clientSocket = serverSocket.accept();
                System.out.println(String.format("Client nou conectat : [%s : %d]\n", clientSocket.getLocalAddress(), clientSocket.getLocalPort()));
                new Thread(clientCommunicationThread(clientSocket)).start();
            }
        }
        catch (Exception exception){
            serverSocket.close();
            System.out.println("Client communication loop exception : " + exception.getMessage());
        }
    }

    /**
     * Functie care inglobeaza comunicarea de date cu un nod adicant, avandu-se in vedere primirea de date de la un
     * nod adiacent si, eventual, trimiterea informatiilor mai departe, in cazul in care nu este nod terminal.
     * @param clientSocket Socket-ul nodului adiacent, de la care primeste date.
     * @return Runnable-ul necesar pornirii unui thread separat pentru aceasta comunicare.
     */
    public Runnable clientCommunicationThread(Socket clientSocket){
        return new Runnable() {
            @Override
            public void run(){
                try {
                    ManagerResponse response = new ManagerResponse();
                    DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                    DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                    String chain;
                    byte[] buffer = new byte[bufferSize];
                    while(dataInputStream.read(buffer, 0, bufferSize) > 0){
                        ClientManagerRequest clientManagerRequest = (ClientManagerRequest) Serializer.deserialize(buffer);
                        ClientRequest clientRequest = getOperationType(clientManagerRequest.getClass());

                        ClientRequestStatus fileStatus = checkFileStatus(clientManagerRequest.getUserId(), clientManagerRequest.getFilename());
                        switch (clientRequest){
                            case NEW_FILE:{
                                switch (fileStatus){
                                    case FILE_EXISTS: {
                                        response.setException("FILE ALREADY EXISTS!");
                                        break;
                                    }
                                    case FILE_NOT_FOUND: {
                                        int filesize = ((NewFileRequest)clientManagerRequest).getFilesize();
                                        int replication_factor = ((NewFileRequest)clientManagerRequest).getReplication_factor();
                                        chain = generateChain(filesize, replication_factor);
                                        if (chain != null) {
                                            response.setResponse(chain);
                                            System.out.println("Token-ul a fost trimis catre client : " + chain);
                                            registerUserNewFileRequest(clientManagerRequest.getUserId(), clientManagerRequest.getFilename(), replication_factor);
                                        } else {
                                            response.setException("eroare");
                                        }
                                        break;
                                    }
                                }
                                break;
                            }
                            case RENAME_FILE:{
                                switch (fileStatus){
                                    case FILE_NOT_FOUND:{
                                        response.setException("FILE NOT FOUND");
                                        break;
                                    }
                                    case FILE_EXISTS:{
                                        response.setResponse("Se rezolva boss!");
                                        List<String> candidateNodes = GeneralManager.statusTable.getAvailableNodesForFile(clientManagerRequest.getUserId(), clientManagerRequest.getFilename());
                                        GeneralManager.fileSystemManager.renameFile(clientManagerRequest.getUserId(), clientManagerRequest.getFilename(),
                                                ((RenameFileRequest)clientManagerRequest).getNewName(), candidateNodes);
                                        GeneralManager.contentTable.updateFileName(clientManagerRequest.getUserId(), clientManagerRequest.getFilename(), ((RenameFileRequest) clientManagerRequest).getNewName());
                                        break;
                                    }
                                }
                                break;
                            }
                            case DELETE_FILE:{
                                switch (fileStatus){
                                    case FILE_NOT_FOUND: {
                                        response.setException("FILE NOT FOUND");
                                        break;
                                    }
                                    case FILE_EXISTS:{
                                        GeneralManager.contentTable.updateReplicationFactor(clientManagerRequest.getUserId(), clientManagerRequest.getFilename(), 0);
                                        response.setResponse("OK");
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                        dataOutputStream.write(Serializer.serialize(response));
                    }
                    System.out.println("Cerinta clientului a fost realizata..");
                    dataInputStream.close();
                    dataOutputStream.close();
                    clientSocket.close();
                }
                catch (Exception exception){
                    System.out.println(exception.getMessage());
                    System.out.println(String.format("Could not properly close connection with my friend : [%s : %d]",
                            clientSocket.getLocalAddress(),
                            clientSocket.getLocalPort()));
                }
            }
        };
    }
}
