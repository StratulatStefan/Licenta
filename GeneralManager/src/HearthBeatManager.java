import communication.Address;
import communication.HearthBeatSocket;
import config.AppConfig;
import node_manager.NodeBeat;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import data.Time;

/**
 * Clasa care se va ocupa de tot mecanismul de heartbeats.
 * Va primi mesaje frecvent de la fiecare nod intern si va stoca statusul acestora.
 */
public class HearthBeatManager implements Runnable{
    /** -------- Atribute -------- **/
    /**
     * Adresa de multicast
     */
    private static String multicastIPAddress;
    /**
     * Portul de multicast
     */
    private static int multicastPort;
    /**
     * Adresa pe care o va avea nodul curent.
     */
    private Address nodeAddress;
    /**
     * Frecventa heartbeat-urilor
     * Exprimat in secunde.
     */
    private double frequency;
    /**
     * Numarul de heart-beat-uri la care se face clean-up-ul tabelei de conexiuni
     */
    private static int cleanupFrequency;


    /** -------- Constructor & Configurare -------- **/
    /**
     * Functie care citeste si initializeaza parametrii de configurare
     */
    public void readConfigParams(){
        multicastIPAddress = AppConfig.getParam("multicastIPAddress");
        multicastPort = Integer.parseInt(AppConfig.getParam("multicastPort"));
        frequency = Integer.parseInt(AppConfig.getParam("hearthBeatFrequency"));
        cleanupFrequency = Integer.parseInt(AppConfig.getParam("cleanupFrequency"));
    }

    /**
     * Constructorul managerului de heartbeat-uri pentru nodul curent.
     * @param address Adresa nodului curent
     */
    public HearthBeatManager(String address) throws Exception{
        readConfigParams();
        this.nodeAddress = new Address(address, multicastPort);
    }


    /** -------- Principalele actiuni -------- **/
    /**
     * Functie care se ocupa de secventa de trimitere a heart-beat-urilor, odata la frequency secunde.
     * Fiecare trimitere a beat-urilor este urmata de verificarea tabelei de conexiuni.
     * @param group Adresa de multicast pe care se va trimite mesajul
     * @param socket Socket-ul nodului curent
     * @return Runnable pe baza caruia se va porni thread-ul de trimitere
     */
    public Runnable cleanUp(InetAddress group, HearthBeatSocket socket){
        return new Runnable(){
            @Override
            public void run(){
                List<Address> disconnected;
                int cleanUpIndex = 1;
                while(true) {
                    System.out.println(Time.getCurrentTimeWithFormat() + " ");
                    try {
                        if(cleanUpIndex == cleanupFrequency){
                            disconnected = GeneralManager.connectionTable.checkDisconnection(frequency);
                            if(disconnected.size() != 0){
                                for (Address disconnectedAddres : disconnected) {
                                    System.out.println(" >>> Address " + disconnectedAddres + " disconnected");
                                    GeneralManager.connectionTable.removeAddress(disconnectedAddres);
                                    GeneralManager.statusTable.cleanUpAtNodeDisconnection(disconnectedAddres.getIpAddress());
                                }
                            }
                            cleanUpIndex = 0;
                        }
                        if(GeneralManager.connectionTable.size() == 0){
                            System.out.println(" >>> Niciun nod conectat!");
                        }
                        else {
                            System.out.println(GeneralManager.connectionTable);
                        }
                        cleanUpIndex += 1;
                        Thread.sleep((int) (frequency * 1e3));

                        if(GeneralManager.contentTable.needInit){
                            GeneralManager.contentTable.initialize(GeneralManager.statusTable);
                        }
                    } catch (InterruptedException exception) {
                        socket.close();
                        System.out.println("InterruptedException occured. : " + exception.getMessage());
                    }
                    System.out.println("\n");
                }
            }
        };
    }

    /**
     * Functie care se ocupa de primirea mesajelor de la celelalte noduri. La fiecare primire a unui nou
     * heartbeat, se actualizeaza tabela de conexiuni. Primirea se face incontinuu, fara timeout pe recv.
     * @param socket Socket-ul nodului curent
     * @return Runnable-ul pe baza caruia se va crea thread-ul de receptie a hearth-beat-urilor.
     */
    public Runnable receivingLoop(HearthBeatSocket socket){
        return new Runnable() {
            @Override
            public void run(){
                NodeBeat message;
                Address receivedAddress;
                while(true){
                    try{
                        message = (NodeBeat) socket.receiveMessage();
                        receivedAddress = Address.parseAddress(message.getNodeAddress());
                        GeneralManager.statusTable.updateTable(message);
                        System.out.println("Am primit un hearthbeat de la " + receivedAddress + " ...");
                        if(!GeneralManager.connectionTable.containsAddress(receivedAddress)){
                            System.out.println(" >>> [Adresa noua] : " + receivedAddress);
                            GeneralManager.connectionTable.addAddress(receivedAddress);
                        }
                        else {
                            GeneralManager.connectionTable.confirmAvailability(receivedAddress);
                        }
                    }
                    catch (Exception exception){
                        System.out.println("Hearthbeatmanager receiveloop : " + exception.getMessage());
                    }
                }
            }
        };
    }


    /** -------- Main -------- **/
    /**
     * Acest manager de hearbeat-uri va trebui sa fie executat pe un thread separat, astfel incat sa nu blocheze comunicarea
     * managerului general cu nodurile conectate. Asadar, trebuie implementata functia run, care se va executa la apelul start.
     *
     * Principala bucla care se ocupa de manevrarea heartbeat-urilor (trimitere/receptie).
     */
    public void run(){
        try {
            System.out.println(String.format("Node with address [%s] started...", nodeAddress));
            InetAddress group = InetAddress.getByName(HearthBeatManager.multicastIPAddress);
            HearthBeatSocket socket = new HearthBeatSocket(nodeAddress, multicastPort);
            socket.setNetworkInterface(HearthBeatSocket.NetworkInterfacesTypes.LOCALHOST);
            socket.joinGroup(group);
            Thread sendingThread = new Thread(cleanUp(group, socket));
            Thread receivingThread = new Thread(receivingLoop(socket));
            sendingThread.start();
            receivingThread.start();
        }
        catch (IOException exception){
            System.out.println(exception.getMessage());
        }
    }
}
