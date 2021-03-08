import communication.Address;
import communication.FileHeader;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.lang.*;

/* https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/MulticastSocket.html */
/* https://tldp.org/HOWTO/Multicast-HOWTO.html#toc1 */
public class GeneralNode{
    /**
     * Portul pe care fi mapata ServerSocket-ul
     */
    private final static int serverSocketPort = 8081;

    /**
     * Portul pe care va fi mapata comunicarea multicast
     */
    private final static int heartBeatPort = 8246;

    /**
     * Obiectul care se ocupa de mecanismul de hearbeats
     */
    private HearthBeatManager hearthBeatManager;

    /**
     * Obiectul care se va ocupa de comunicarea cu clientul
     */
    private ClientCommunicationManager clientCommunicationManager;

    /**
     * Constructorul clasei
     * @param hearthBeatManager Managerul de heartbeat-uri
     */
    public GeneralNode(HearthBeatManager hearthBeatManager, ClientCommunicationManager clientCommunicationManager){
        this.hearthBeatManager = hearthBeatManager;
        this.clientCommunicationManager = clientCommunicationManager;
    }

    public void StartActivity() throws Exception {
        // Pornim thread-ul pe care va fi rulat mecanismul de heartbeats
        new Thread(hearthBeatManager).start();

        // Pornim comunicarea cu clientul
        clientCommunicationManager.ClientCommunicationLoop();
    }

    /**
     * @param args Argumentele furnizate la linia de comanda
     */
    public static void main(String[] args) throws IOException {
        try {
            Address hearthBeatAddress = new Address(args[0], heartBeatPort);
            HearthBeatManager hearthBeatManager = new HearthBeatManager(hearthBeatAddress);

            Address clientCommunicationAddress = new Address(args[0], serverSocketPort);
            InternalNodeCommunicationManager internalCommunicationManager = new InternalNodeCommunicationManager();
            ClientCommunicationManager clientCommunicationManager = new ClientCommunicationManager(clientCommunicationAddress, internalCommunicationManager);

            GeneralNode generalManager = new GeneralNode(hearthBeatManager, clientCommunicationManager);
            generalManager.StartActivity();
        }
        catch (Exception exception){
            System.out.println(exception.getMessage());
        }
    }
}