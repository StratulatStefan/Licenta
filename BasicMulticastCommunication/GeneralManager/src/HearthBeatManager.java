import model.Address;
import model.ConnectionTable;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

public class HearthBeatManager implements Runnable{
    /**
     * Tabela (o lista) nodurilor conectate in retea, care comunica cu nodul curent.
     */
    private static ConnectionTable connectionTable = new ConnectionTable();

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
     * Principala bucla care se ocupa de manevrarea heartbeat-urilor (trimitere/receptie).
     * @throws IOException generata de o eroare aparuta la folosirea socket-ului de multicast.
     */
    public void hearthBeatLoop() throws IOException{
        System.out.println(String.format("Node with address [%s] started...", nodeAddress));
        InetAddress group = InetAddress.getByName(HearthBeatManager.multicastIPAddress);
        HearthBeatSocket socket = new HearthBeatSocket(nodeAddress, multicastPort);
        socket.setNetworkInterface(HearthBeatSocket.NetworkInterfacesTypes.LOCALHOST);
        socket.joinGroup(group);
        socket.setTimeOut((int) (timeout * 1e3));
        String message;
        Address receivedAddress;
        while(true){
            System.out.println("[My address] " + nodeAddress.toString());
            System.out.println(" >>> Sending my address...");
            connectionTable.resetAddressList();
            try{
                socket.sendMessage(group, nodeAddress.toString());
                try{
                    System.out.println(" >>> Waiting for data from friend...");
                    while(true){
                        message = socket.receiveMessage();
                        receivedAddress = Address.parseAddress(message);
                        if(!connectionTable.containsAddress(receivedAddress)){
                            System.out.println(" >>> [New address] : " + receivedAddress);
                            connectionTable.addAddress(receivedAddress);
                        }
                        else{
                            try {
                                connectionTable.confirmAvailability(receivedAddress);
                            }
                            catch (Exception exception){
                                System.out.println(exception.getMessage());
                            }
                        }
                    }
                }
                catch (Exception exception){
                    List<Address> disconnected = connectionTable.checkDisconnection(2);
                    if(disconnected.size() == 0){
                        if(connectionTable.size() == 0){
                            System.out.println(" >>> Nobody connected!");
                        }
                        else {
                            System.out.println(connectionTable);
                        }
                    }
                    else{
                        for(Address disconnectedAddres : disconnected){
                            System.out.println(" >>> Address " + disconnectedAddres + " disconnected");
                            connectionTable.removeAddress(disconnectedAddres);
                        }
                    }
                }
                Thread.sleep((int)(frequency * 1e3));
            }
            catch (IOException exception){
                socket.close();
                System.out.println("IOException occured. : " + exception.getMessage());
            }
            catch (InterruptedException exception){
                socket.close();
                System.out.println("InterruptedException occured. : " + exception.getMessage());
            }

            System.out.println("\n");
        }
    }

    /**
     * Getter pentru connectionTable.
     * Va fi necesar astfel incat managerul general sa poata deschide conexiuni unicast cu nodurile disponibile
     * @return tabela adreselor nodurilor conectate
     */
    public ConnectionTable getConnectionTable(){
        return HearthBeatManager.connectionTable;
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
