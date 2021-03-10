import client_manager.ClientManagerRequest;
import client_manager.Token;
import communication.Address;
import communication.Serializer;
import model.ConnectionTable;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.lang.*;
import java.util.HashMap;

/* https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/MulticastSocket.html */
/* https://tldp.org/HOWTO/Multicast-HOWTO.html#toc1 */
public class GeneralManager{
    /**
     * Tabela (o lista) nodurilor conectate in retea, care comunica cu nodul curent.
     */
    private static ConnectionTable connectionTable = new ConnectionTable();

    /**
     * Adresa IP la care va fi mapat managerul general
     */
    private static String generalManagerIpAddress = "127.0.0.1";

    /**
     * Portul de multicast.
     */
    private static int multicastPort = 8246;

    /**
     * Portul pentru comunicarea cu clientul
     */
    private final static int dataTransmissionPort = 8081;

    /**
     * Frecventa heartbeat-urilor
     * Exprimat in secunde.
     */
    private final static double hearthBeatFrequency = 5;

    /**
     * Dimensiunea bufferului in care vor fi citite datele de la un nod adiacent
     */
    private final static int bufferSize = 1024;

    /**
     * Obiectul care se ocupa de mecanismul de hearthbeats
     */
    private HearthBeatManager hearthBeatManager;

    /**
     * Obiectul care se va ocupa de comunicatia cu clientul
     */
    private ClientCommunicationManager clientCommunicationManager;

    /**
     * Constructorul clasei
     * @param hearthBeatManager Managerul de heartbeat-uri
     */
    public GeneralManager(HearthBeatManager hearthBeatManager, ClientCommunicationManager clientCommunicationManager){
        this.hearthBeatManager = hearthBeatManager;
        this.clientCommunicationManager = clientCommunicationManager;
    }

    /**
     * Functie care porneste thread-ul pe care va fi rulat mecanismul de heartbeats
     */
    public void HearthBeatActivity(){
        new Thread(hearthBeatManager).start();
    }

    /** Functie care inglobeaza activitatea principala a fiecarui nod, aceea de a asigura comunicarea cu celelalte noduri
     * in vederea trimiterii si primirii de mesaje.
     */
    public void MainActivity() throws Exception{
        Address address = new Address(generalManagerIpAddress, dataTransmissionPort);
        ServerSocket serverSocket = new ServerSocket();
        try{
            serverSocket.bind(new InetSocketAddress(address.getIpAddress(), dataTransmissionPort));
            while(true){
                Socket clientSocket = serverSocket.accept();
                System.out.println(String.format("Client nou conectat : [%s : %d]\n", clientSocket.getLocalAddress(), clientSocket.getLocalPort()));
                new Thread(MainActivityThread(clientSocket)).start();
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
    private Runnable MainActivityThread(Socket clientSocket){
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
                        ClientCommunicationManager.ClientRequest clientRequest = clientCommunicationManager.getOperationType(clientManagerRequest.getOperation());
                        if(clientRequest == ClientCommunicationManager.ClientRequest.NEW_FILE){
                            ClientCommunicationManager.ClientRequestStatus fileStatus = clientCommunicationManager.checkFileStatus(clientManagerRequest.getUserId(),
                                    clientManagerRequest.getFilename());
                            if(fileStatus == ClientCommunicationManager.ClientRequestStatus.FILE_ALREADY_EXISTS){
                                token.setException("FILE ALREADY EXISTS!");
                            }
                            else if(fileStatus == ClientCommunicationManager.ClientRequestStatus.OK) {
                                chain = clientCommunicationManager.generateChain(connectionTable,
                                        clientManagerRequest.getFilesize(), clientManagerRequest.getReplication_factor());
                                if (chain != null) {
                                    token.setToken(chain);
                                    System.out.println("Token-ul a fost trimis catre client : " + chain);
                                    clientCommunicationManager.registerUserNewFileRequest(chain,
                                            clientManagerRequest.getUserId(), clientManagerRequest.getFilename());
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

    /**
     * @param args Argumentele furnizate la linia de comanda
     */
    public static void main(String[] args){
        Address multicastAddress;
        try {
            multicastAddress = new Address(generalManagerIpAddress, multicastPort);
            HearthBeatManager hearthBeatManager = new HearthBeatManager(multicastAddress, hearthBeatFrequency, connectionTable);

            ClientCommunicationManager clientCommunicationManager = new ClientCommunicationManager();
            GeneralManager generalManager = new GeneralManager(hearthBeatManager, clientCommunicationManager);

            generalManager.HearthBeatActivity();
            generalManager.MainActivity();
        }
        catch (Exception exception){
            System.out.println(exception.getMessage());
        }
    }
}