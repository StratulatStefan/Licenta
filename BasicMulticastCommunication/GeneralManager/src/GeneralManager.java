import model.Address;
import model.ConnectionTable;

import java.io.IOException;
import java.net.*;
import java.util.List;

/* https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/MulticastSocket.html */

public class GeneralManager {
    /**
     * Adresa de multicast
     */
    private static String ipAddress = "230.0.0.10";
    /**
     * Portul de multicast
     */
    private static int groupPort = 8246;
    /**
     * Tabela (o lista) nodurilor conectate in retea, care comunica cu nodul curent.
     */
    private static ConnectionTable connectionTable = new ConnectionTable();

    /**
     * Adresa pe care o va avea nodul curent.
     */
    private static Address nodeAddress;

    /**
     * Principala bucla care se ocupa de manevrarea heartbeat-urilor (trimitere/receptie)
     * @param address Adresa nodului curent
     * @param frequency frecventa la care se trimite heartbeat-ul (exprimat in secunde)
     * @param timeout timeout-ul pentru bucla de receptie a mesajelor de la celelalte noduri
     * @throws IOException
     */
    public static void hearthBeatLoop(Address address, double frequency, double timeout) throws IOException{
        System.out.println(String.format("Node with address [%s] started...", address));
        nodeAddress = address;

        InetAddress group = InetAddress.getByName(GeneralManager.ipAddress);
        HearthBeatSocket socket = new HearthBeatSocket(groupPort);
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
                        message = socket.receiveMessage(group);
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
                Thread.sleep(3000);
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
    public static void main(String[] args){
        Address address;
        try {
            address = generateAddress(args);
            hearthBeatLoop(address, 3, 0.5);
        }
        catch (Exception exception){
            System.out.println(exception.getMessage());
        }
    }
}