import client_manager.ClientManagerRequest;
import client_manager.Token;
import communication.Address;
import communication.Serializer;
import model.ConnectionTable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ClientCommunicationManager {
    /**
     * Enum care va cuprinde toate tipurile de interactiune cu clientul
     */
    public enum ClientRequest{
        NEW_FILE
    }

    public enum ClientRequestStatus{
        OK,
        FILE_ALREADY_EXISTS
    }

    /**
     * Dimensiunea bufferului in care vor fi citite datele de la un nod adiacent
     */
    private final static int bufferSize = 1024;

    /**
     * Colectie care va contine
     */
    public static final ConcurrentHashMap<String, HashMap<String, String[]>> contentTable = new ConcurrentHashMap();

    /**
     * Functie care identifica tipul operatiei solicitate de utilizator
     * @param operation String-ul ce identifica operatia
     * @return Tipul operatiei sau null daca nu s-a identificat nicio operatie valida
     */
    public ClientRequest getOperationType(String operation){
        if(operation.equals("newfile")){
            return ClientRequest.NEW_FILE;
        }
        return null;
    }

    /**
     * Functie care va genera lantul de noduri la care se va stoca un fisier nou aparut in sistem
     * @param connectionTable Tabela conexiunilor (noduri disponibile)
     * @param filesize Dimensiunea fisierului ce va fi stocat
     * @param replication_factor Factorul de replicare al fisierului
     * @return Lantul de noduri la care se va stoca fisierului
     */
    public String generateChain(ConnectionTable connectionTable, int filesize, int replication_factor){
        System.out.println("User uploaded a new file with size : " + filesize + " and replication factor : " + replication_factor);
        List<String> connectionAddresses = connectionTable.getConnectionTable();
        if(connectionAddresses.size() <  replication_factor){
            System.out.println("Nu sunt suficiente noduri disponibile");
        }
        else{
            System.out.println("Generam token-ul..");
            String token = "";
            /* !!!!!! criteriu de selectare a nodurilor !!!!!! */
            for(String address : connectionAddresses){
                if(replication_factor == 0){
                    break;
                }
                token = token + address + "-";
                replication_factor -= 1;
            }
            token = token.substring(0, token.length() - 1);
            return token;
        }
        return null;
    }

    public void registerUserNewFileRequest(String user, String filename, int replication_factor){
        synchronized (GeneralManager.contentTable){
            try {
                GeneralManager.contentTable.AddRegister(user, filename, replication_factor);
            }
            catch (Exception exception){
                System.out.println(exception.getMessage());
            }
        }
    }

    public ClientRequestStatus checkFileStatus(String user, String filename){
        boolean fileStatus = GeneralManager.statusTable.CheckFileForUser(user, filename);
        if(fileStatus){
            return ClientRequestStatus.FILE_ALREADY_EXISTS;
        }
        return ClientRequestStatus.OK;
    }


    /** Functie care inglobeaza activitatea principala a fiecarui nod, aceea de a asigura comunicarea cu celelalte noduri
     * in vederea trimiterii si primirii de mesaje.
     */
    public void ClientCommunicationLoop(String generalManagerIpAddress, int dataTransmissionPort, ConnectionTable connectionTable) throws Exception{
        Address address = new Address(generalManagerIpAddress, dataTransmissionPort);
        ServerSocket serverSocket = new ServerSocket();
        try{
            serverSocket.bind(new InetSocketAddress(address.getIpAddress(), dataTransmissionPort));
            while(true){
                Socket clientSocket = serverSocket.accept();
                System.out.println(String.format("Client nou conectat : [%s : %d]\n", clientSocket.getLocalAddress(), clientSocket.getLocalPort()));
                new Thread(ClientCommunicationThread(clientSocket, connectionTable)).start();
            }
        }
        catch (Exception exception){
            serverSocket.close();
            System.out.println(exception.getMessage());
        }
    }

    /**
     * Functie care inglobeaza comunicarea de date cu un nod adicant, avandu-se in vedere primirea de date de la un
     * nod adiacent si, eventual, trimiterea informatiilor mai departe, in cazul in care nu este nod terminal.
     * @param clientSocket Socket-ul nodului adiacent, de la care primeste date.
     * @return Runnable-ul necesar pornirii unui thread separat pentru aceasta comunicare.
     */
    public Runnable
    ClientCommunicationThread(Socket clientSocket, ConnectionTable connectionTable){
        return new Runnable() {
            @Override
            public void run(){
                try {
                    Token token = new Token();
                    DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                    DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                    String chain;
                    byte[] buffer = new byte[bufferSize];
                    while(dataInputStream.read(buffer, 0, bufferSize) > 0){
                        ClientManagerRequest clientManagerRequest = (ClientManagerRequest) Serializer.Deserialize(buffer);
                        ClientCommunicationManager.ClientRequest clientRequest = getOperationType(clientManagerRequest.getOperation());
                        if(clientRequest == ClientCommunicationManager.ClientRequest.NEW_FILE){
                            ClientCommunicationManager.ClientRequestStatus fileStatus = checkFileStatus(clientManagerRequest.getUserId(),
                                    clientManagerRequest.getFilename());
                            if(fileStatus == ClientCommunicationManager.ClientRequestStatus.FILE_ALREADY_EXISTS){
                                token.setException("FILE ALREADY EXISTS!");
                            }
                            else if(fileStatus == ClientCommunicationManager.ClientRequestStatus.OK) {
                                chain = generateChain(connectionTable,
                                        clientManagerRequest.getFilesize(), clientManagerRequest.getReplication_factor());
                                if (chain != null) {
                                    token.setToken(chain);
                                    System.out.println("Token-ul a fost trimis catre client : " + chain);
                                    registerUserNewFileRequest(clientManagerRequest.getUserId(), clientManagerRequest.getFilename(), clientManagerRequest.getReplication_factor());
                                } else {
                                    token.setException("eroare");
                                }
                            }
                        }
                        dataOutputStream.write(Serializer.Serialize(token));
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
