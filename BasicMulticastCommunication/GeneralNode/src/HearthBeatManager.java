import communication.Address;
import communication.HearthBeatSocket;

import java.io.IOException;
import java.net.InetAddress;

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
     * Timeout-ul asteptarii primirii heartbeat-urilor
     * Exprimat in secunde.
     */
    private double timeout;


    /**
     * Constructorul managerului de heartbeat-uri pentru nodul curent.
     * @param address Adresa nodului curent
     * @param frequency Frecventa buclei de trimitere/receptie heartbeat-uri
     * @param timeout Timeout pentru primirea hearthbeat-urilor.
     */
    public HearthBeatManager(Address address, double frequency, double timeout){
        this.nodeAddress = address;
        this.frequency = frequency;
        this.timeout = timeout;
    }

    /**
     * Functie care se ocupa de secventa de trimitere a heart-beat-urilor, odata la frequency secunde.
     * Fiecare trimitere a beat-urilor este urmata de verificarea tabelei de conexiuni.
     * @param group Adresa de multicast pe care se va trimite mesajul
     * @param socket Socket-ul nodului curent
     * @return Runnable pe baza caruia se va porni thread-ul de trimitere
     */
    public Runnable sendingLoop(InetAddress group, HearthBeatSocket socket){
        return new Runnable(){
            @Override
            public void run(){
                while(true) {
                    System.out.println(Time.getCurrentTimeWithFormat() + " Se trimite un hearthbeat ...");
                    //System.out.println("[My address] " + nodeAddress.toString());
                    //System.out.println(" >>> Sending my address...");
                    try {
                        socket.sendMessage(group, nodeAddress.toString());
                        Thread.sleep((int) (frequency * 1e3));
                    } catch (IOException exception) {
                        socket.close();
                        System.out.println("IOException occured. : " + exception.getMessage());
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
                String message;
                Address receivedAddress;
                //while(true){
                    try{
                        message = socket.receiveMessage();
                        receivedAddress = Address.parseAddress(message);
                        //if(!connectionTable.containsAddress(receivedAddress)){
                            System.out.println(" >>> [New address] : " + receivedAddress);
                            //connectionTable.addAddress(receivedAddress);
                       // }
                        //else {
                       //    connectionTable.confirmAvailability(receivedAddress);
                       // }
                    }
                    catch (Exception exception){
                        System.out.println(exception.getMessage());
                    }
               // }
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
        Thread sendingThread = new Thread(sendingLoop(group, socket));
       // Thread receivingThread = new Thread(receivingLoop(socket));
        sendingThread.start();
        //receivingThread.start();
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
