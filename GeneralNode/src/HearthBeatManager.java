import communication.Address;
import communication.HearthBeatSocket;

import java.io.IOException;
import java.net.InetAddress;

import communication.Serializer;
import config.AppConfig;
import node_manager.Beat.NodeBeat;
import data.Time;

/**
 * Clasa care se va ocupa de tot mecanismul de heartbeats.
 * Va trimite mesaje frecvent catre nodul general;
 * Mesajele vor contine adresa nodului si statusul stocarii
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
    private static double frequency;


    /** -------- Constructor & Configurare -------- **/
    /**
     * Functie care citeste si initializeaza parametrii de configurare
     */
    public void readConfigParams(){
        multicastPort = Integer.parseInt(AppConfig.getParam("multicastPort"));
        multicastIPAddress = AppConfig.getParam("multicastIPAddress");
        frequency = Double.parseDouble(AppConfig.getParam("hearthBeatFrequency"));
    }

    /**
     * Constructorul managerului de heartbeat-uri pentru nodul curent.
     * @param address Adresa nodului curent
     */
    public HearthBeatManager(String address) throws Exception{
        readConfigParams();
        this.nodeAddress = new Address(address, multicastPort);
    }


    /** -------- Main -------- **/
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
                    try {
                        NodeBeat clientStorageStatus = GeneralNode.getStorageStatus();
                        if(clientStorageStatus == null)
                            continue;
                        clientStorageStatus.setNodeAddress(nodeAddress.toString());
                        socket.sendBinaryMessage(group, Serializer.serialize(clientStorageStatus));
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
                    //message = socket.receiveMessage();
                    //receivedAddress = Address.parseAddress(message);
                    //if(!connectionTable.containsAddress(receivedAddress)){
                    //  System.out.println(" >>> [New address] : " + receivedAddress);
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
            Thread sendingThread = new Thread(sendingLoop(group, socket));
            // Thread receivingThread = new Thread(receivingLoop(socket));
            sendingThread.start();
            //receivingThread.start();
        }
        catch (IOException exception){
            System.out.println(exception.getMessage());
        }
    }
}
