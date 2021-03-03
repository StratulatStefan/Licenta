import communication.Address;
import model.ClientCommunicationManager;
import model.ConnectionTable;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.lang.*;
import java.util.List;

/* https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/MulticastSocket.html */
/* https://tldp.org/HOWTO/Multicast-HOWTO.html#toc1 */
public class GeneralManager{
    /**
     * Tabela (o lista) nodurilor conectate in retea, care comunica cu nodul curent.
     */
    private static ConnectionTable connectionTable = new ConnectionTable();

    /**
     * Adresa pe care o va avea nodul curent.
     */
    private Address nodeAddress;

    /**
     * Frecventa heartbeat-urilor
     * Exprimat in secunde.
     */
    private final static double hearthBeatFrequency = 2;

    /**
     * Portul pe care fi mapata ServerSocket-ul
     */
    private final static int serverSocketPort = 8081;

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
     * @param address Adresa nodului curent
     * @param hearthBeatManager Managerul de heartbeat-uri
     */
    public GeneralManager(Address address, HearthBeatManager hearthBeatManager, ClientCommunicationManager clientCommunicationManager){
        this.nodeAddress = address;
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
    public void MainActivity(String ipAddress) throws Exception{
        Address address = generateAddress(new String[]{ipAddress, String.format("%d", serverSocketPort)});
        ServerSocket serverSocket = new ServerSocket();
        try{
            serverSocket.bind(new InetSocketAddress(address.getIpAddress(), serverSocketPort));
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
                    byte[] buffer = new byte[bufferSize];
                    int read = 0;
                    boolean header_found = false;
                    int timeout = 2000;
                    while((read = dataInputStream.read(buffer, 0, bufferSize)) > 0){
                        String clientMessage = new String(buffer, StandardCharsets.UTF_8).substring(0,read);
                        ClientCommunicationManager.ClientRequest clientRequest = clientCommunicationManager.parseMessageFromClient(clientMessage);
                        if(clientRequest == ClientCommunicationManager.ClientRequest.NEW_FILE){
                            String chain = clientCommunicationManager.generateChain(connectionTable, clientMessage);
                            if(chain != null) {
                                System.out.println("Token-ul a fost trimis catre client : " + chain);
                                dataOutputStream.write(chain.getBytes());
                            }
                            else{
                                String errormsg = "eroare";
                                dataOutputStream.write(errormsg.getBytes());
                            }

                        }
                    }
                    System.out.println("File write done");
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
     * Functie care afiseaza interfetele de retea disponibile
     * Nu este folosita
     */
    public static void displayNetworkInterfaces() {
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            int contor = 0;
            while (enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = enumeration.nextElement();
                if (networkInterface.isUp()) {
                    System.out.println(networkInterface.getName());
                    System.out.println(contor + " : " + networkInterface.getInterfaceAddresses());
                }
                contor += 1;
            }
        }
        catch (SocketException exception){
            System.out.println("Error at fetching or accessing the network interfaces");
        }
    }

    /**
     * Datele componente ale adresei nodului curent (ip si port) se vor trimite prin intermediul parametrilor la linia de comanda.
     * Aceasta functie realizeaza o verificare de baza a paraemetrilor furnizati la linia de comanda.
     * Se impune sa fie furnizati cei doi parametri (ip si port) si sa aiba formatul necesar. (string si int).
     * Daca sunt valide, se va returna obiectul de tip Address.
     * @param args argumentele furnizate la linia de coamnda
     * @return obiectul de tip Address ce va reprezenta adresa nodului curent.
     * @throws Exception generata in cazul in care nu se furnizeaza numarul specificat de argumente sau daca nu au tipul necesar.
     */
    public static Address generateAddress(String[] args) throws Exception{
        if(args.length < 2){
            throw new Exception("Please provide the ip address and the port number for this node.");
        }
        int port;
        try{
            port = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException exception){
            throw new Exception("Please provide a number for the port.");
        }
        return new Address(args[0], port);
    }

    /**
     * @param args Argumentele furnizate la linia de comanda
     */
    public static void main(String[] args) throws IOException {
        Address address;
        try {
            address = generateAddress(new String[]{"127.0.0.1", "8246"});
            HearthBeatManager hearthBeatManager = new HearthBeatManager(address, hearthBeatFrequency, connectionTable);
            ClientCommunicationManager clientCommunicationManager = new ClientCommunicationManager();
            GeneralManager generalManager = new GeneralManager(address, hearthBeatManager, clientCommunicationManager);
            generalManager.HearthBeatActivity();
            generalManager.MainActivity("127.0.0.1");
        }
        catch (Exception exception){
            System.out.println(exception.getMessage());
        }
    }
}