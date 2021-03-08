import communication.Address;
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
                new Thread(MainActivityThread(serverSocket, clientSocket)).start();
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
     * @param serverSocket Socket-ul pe care este mapat nodul curent.
     * @param clientSocket Socket-ul nodului adiacent, de la care primeste date.
     * @return Runnable-ul necesar pornirii unui thread separat pentru aceasta comunicare.
     */
    private Runnable MainActivityThread(ServerSocket serverSocket, Socket clientSocket){
        return new Runnable() {
            @Override
            public void run(){
                try {
                    DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                    DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                    HashMap<String,String> parsedMessage = null;
                    String chain = null;
                    byte[] buffer = new byte[bufferSize];
                    int read;
                    while((read = dataInputStream.read(buffer, 0, bufferSize)) > 0){
                        String clientMessage = new String(buffer, StandardCharsets.UTF_8).substring(0,read);
                        parsedMessage = clientCommunicationManager.parseMessageFromClient(clientMessage);
                        ClientCommunicationManager.ClientRequest clientRequest = clientCommunicationManager.getOperationType(parsedMessage);
                        if(clientRequest == ClientCommunicationManager.ClientRequest.NEW_FILE){
                            ClientCommunicationManager.ClientRequestStatus fileStatus = clientCommunicationManager.checkFileStatus(parsedMessage);
                            if(fileStatus == ClientCommunicationManager.ClientRequestStatus.FILE_ALREADY_EXISTS){
                                dataOutputStream.write("FILE ALREADY EXISTS1".getBytes());
                            }
                            else if(fileStatus == ClientCommunicationManager.ClientRequestStatus.OK) {
                                chain = clientCommunicationManager.generateChain(connectionTable, parsedMessage);
                                if (chain != null) {
                                    System.out.println("Token-ul a fost trimis catre client : " + chain);
                                    dataOutputStream.write(chain.getBytes());
                                    clientCommunicationManager.registerUserNewFileRequest(parsedMessage, chain);
                                } else {
                                    String errormsg = "eroare";
                                    dataOutputStream.write(errormsg.getBytes());
                                }
                            }

                        }
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