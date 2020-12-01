import model.Address;
import model.ConnectionTable;
import sun.applet.Main;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.lang.*;

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
    private final static double hearthBeatFrequency = 3;

    /**
     * Timeout-ul asteptarii primirii heartbeat-urilor
     * Exprimat in secunde.
     */
    private final static double hearthBeatReadTimeout = .5;

    /**
     * Obiectul care se ocupa de mecanismul de hearbeats
     */
    private HearthBeatManager hearthBeatManager;

    /**
     * Constructorul clasei
     * @param address Adresa nodului curent
     * @param hearthBeatManager Managerul de heartbeat-uri
     */
    public GeneralManager(Address address, HearthBeatManager hearthBeatManager){
        this.nodeAddress = address;
        this.hearthBeatManager = hearthBeatManager;
    }

    /**
     * Functie care porneste thread-ul pe care va fi rulat mecanismul de heartbeats
     */
    public void HearthBeatActivity(){
        new Thread(hearthBeatManager).start();
    }

    /**
     * Functie care prezinta toata activitatea nodului curent.
     */
    public void NodeActivity(){
        this.HearthBeatActivity();
        while (true) {
            connectionTable = this.hearthBeatManager.getConnectionTable();
            try {
                System.err.println("++++++++++++++++++++\n");
                System.err.println(connectionTable.toString());
                System.err.println("++++++++++++++++++++\n\n\n\n");
                Thread.sleep(2000);
            } catch (InterruptedException exception) {
                System.out.println("Timeout exception");
                break;
            }
        }
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
            address = generateAddress(new String[]{args[0], "8246"});
            HearthBeatManager hearthBeatManager = new HearthBeatManager(address, hearthBeatFrequency, hearthBeatReadTimeout);
            GeneralManager generalManager = new GeneralManager(address, hearthBeatManager);
            generalManager.NodeActivity();
        }
        catch (Exception exception){
            System.out.println(exception.getMessage());
        }
    }
}