import communication.Address;
import communication.HearthBeatSocket;
import model.ConnectionTable;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import data.Time;

public class HearthBeatManager implements Runnable{
    /**
     * O referinta la tabela de conexiuni din managerul general
     */
    private static ConnectionTable connectionTable;

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
    public HearthBeatManager(Address address, double frequency, ConnectionTable connectionTable){
        this.nodeAddress = address;
        this.frequency = frequency;
        HearthBeatManager.connectionTable = connectionTable;
    }

    /**
     * Functie care se ocupa de secventa de trimitere a heart-beat-urilor, odata la frequency secunde.
     * Fiecare trimitere a beat-urilor este urmata de verificarea tabelei de conexiuni.
     * @param group Adresa de multicast pe care se va trimite mesajul
     * @param socket Socket-ul nodului curent
     * @return Runnable pe baza caruia se va porni thread-ul de trimitere
     */
    public Runnable sendingLoop(InetAddress group, HearthBeatSocket socket, ConnectionTable connectionTable){
        return new Runnable(){
            @Override
            public void run(){
                while(true) {
                    System.out.println(Time.getCurrentTimeWithFormat() + " ");
                    // System.out.println("[My address] " + nodeAddress.toString());
                    // System.out.println(" >>> Sending my address...");
                    try {
                        //socket.sendMessage(group, nodeAddress.toString());
                        List<Address> disconnected = connectionTable.checkDisconnection((int)(frequency * cleanupFrequency));
                        if(disconnected.size() == 0){
                            if(connectionTable.size() == 0){
                                System.out.println(" >>> Niciun nod conectat!");
                            }
                            else {
                                System.out.println(connectionTable);
                            }
                        }
                        else {
                            for (Address disconnectedAddres : disconnected) {
                                System.out.println(" >>> Address " + disconnectedAddres + " disconnected");
                                connectionTable.removeAddress(disconnectedAddres);
                            }
                        }
                        Thread.sleep((int) (frequency * 1e3));
                   // } catch (IOException exception) {
                    //    socket.close();
                     //   System.out.println("IOException occured. : " + exception.getMessage());
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
    public Runnable receivingLoop(HearthBeatSocket socket, ConnectionTable connectionTable){
        return new Runnable() {
            @Override
            public void run(){
                String message;
                Address receivedAddress;
                while(true){
                    try{
                        message = socket.receiveMessage();
                        receivedAddress = Address.parseAddress(message);
                        System.out.println("Am primit un hearthbeat de la " + receivedAddress + " ...");
                        if(!connectionTable.containsAddress(receivedAddress)){
                            System.out.println(" >>> [Adresa noua] : " + receivedAddress);
                            connectionTable.addAddress(receivedAddress);
                        }
                        else {
                            connectionTable.confirmAvailability(receivedAddress);
                        }
                    }
                    catch (Exception exception){
                        System.out.println(exception.getMessage());
                    }
                }
            }
        };
    }

    /**
     * Principala bucla care se ocupa de manevrarea heartbeat-urilor (trimitere/receptie).
     * @throws IOException generata de o eroare aparuta la folosirea socket-ului de multicast.
     */
    public void hearthBeatLoop(ConnectionTable connectionTable) throws IOException{
        System.out.println(String.format("Node with address [%s] started...", nodeAddress));
        InetAddress group = InetAddress.getByName(HearthBeatManager.multicastIPAddress);
        HearthBeatSocket socket = new HearthBeatSocket(nodeAddress, multicastPort);
        socket.setNetworkInterface(HearthBeatSocket.NetworkInterfacesTypes.LOCALHOST);
        socket.joinGroup(group);
        Thread sendingThread = new Thread(sendingLoop(group, socket, connectionTable));
        Thread receivingThread = new Thread(receivingLoop(socket, connectionTable));
        sendingThread.start();
        receivingThread.start();
    }

    /**
     * Acest manager de hearbeat-uri va trebui sa fie executat pe un thread separat, astfel incat sa nu blocheze comunicarea
     * managerului general cu nodurile conectate. Asadar, trebuie implementata functia run, care se va executa la apelul start.
     */
    public void run(){
        try {
            this.hearthBeatLoop(HearthBeatManager.connectionTable);
        }
        catch (IOException exception){
            System.out.println(exception.getMessage());
        }
    }
}
