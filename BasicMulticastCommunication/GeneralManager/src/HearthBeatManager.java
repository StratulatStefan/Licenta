import communication.Address;
import communication.HearthBeatSocket;
import node_manager.NodeBeat;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import data.Time;


public class HearthBeatManager implements Runnable{
    /**
     * Adresa de multicast
     */
    private static String multicastIPAddress = "230.0.0.10";

    /**
     * Portul de multicast
     */
    private static int multicastPort = 8246;

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
    private final static int cleanupFrequency = 3;

    /**
     * Constructorul managerului de heartbeat-uri pentru nodul curent.
     * @param address Adresa nodului curent
     * @param frequency Frecventa buclei de trimitere/receptie heartbeat-uri
     */
    public HearthBeatManager(Address address, double frequency){
        this.nodeAddress = address;
        this.frequency = frequency;
    }

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
                                    GeneralManager.statusTable.CleanUpAtNodeDisconnection(disconnectedAddres.getIpAddress());
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
                        receivedAddress = Address.parseAddress(message.GetNodeAddress());
                        GeneralManager.statusTable.UpdateTable(message);
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

    /**
     * Principala bucla care se ocupa de manevrarea heartbeat-urilor (trimitere/receptie).
     * @throws IOException generata de o eroare aparuta la folosirea socket-ului de multicast.
     */
    public void hearthBeatLoop() throws IOException{
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

    /**
     * Acest manager de hearbeat-uri va trebui sa fie executat pe un thread separat, astfel incat sa nu blocheze comunicarea
     * managerului general cu nodurile conectate. Asadar, trebuie implementata functia run, care se va executa la apelul start.
     */
    public void run(){
        try {
            this.hearthBeatLoop();
        }
        catch (IOException exception){
            System.out.println(exception.getMessage());
        }
    }
}
