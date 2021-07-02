import communication.Address;
import communication.HearthBeatSocket;

import java.io.IOException;
import java.net.InetAddress;

import communication.Serializer;
import config.AppConfig;
import logger.LoggerService;
import node_manager.Beat.NodeBeat;
import data.Time;
import node_manager.Beat.RequestCRC;

/**
 * <ul>
 * 	<li> Clasa care se va ocupa de tot mecanismul de heartbeats.</li>
 * 	<li> Va trimite mesaje frecvent catre nodul general.</li>
 * 	<li> Mesajele vor contine adresa nodului si statusul stocarii.</li>
 * </ul>
 */
public class HearthBeatManager implements Runnable{
    /**
     * Adresa de multicast
     */
    private static String multicastIPAddress = AppConfig.getParam("multicastIPAddress");
    /**
     * Portul de multicast
     */
    private static int multicastPort = Integer.parseInt(AppConfig.getParam("multicastPort"));
    /**
     * Adresa pe care o va avea nodul curent.
     */
    private Address nodeAddress;
    /**
     * <ul>
     * 	<li>Frecventa heartbeat-urilor Exprimat in secunde.</li>
     * </ul>
     */
    private static double frequency = Double.parseDouble(AppConfig.getParam("hearthBeatFrequency"));

    /**
     * Constructorul managerului de heartbeat-uri pentru nodul curent.
     * @param address Adresa nodului curent
     */
    public HearthBeatManager(String address) throws Exception{
        this.nodeAddress = new Address(address, multicastPort);
    }

    /**
     * <ul>
     * 	<li>Functie care se ocupa de secventa de trimitere a heart-beat-urilor, odata la frequency secunde.</li>
     * 	<li> Fiecare trimitere a beat-urilor este urmata de verificarea tabelei de conexiuni.</li>
     * </ul>
     * @param group Adresa de multicast pe care se va trimite mesajul
     * @param socket Socket-ul nodului curent
     * @return Runnable pe baza caruia se va porni thread-ul de trimitere
     */
    public Runnable sendingHeartBeatLoop(InetAddress group, HearthBeatSocket socket){
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
                        LoggerService.registerError(GeneralNode.ipAddress,"IOException occured. : " + exception.getMessage());
                    } catch (InterruptedException exception) {
                        socket.close();
                        LoggerService.registerError(GeneralNode.ipAddress,"InterruptedException occured. : " + exception.getMessage());
                    }
                    System.out.println("\n");
                }
            }
        };
    }

    /**
     * <ul>
     * 	<li>Functie care se ocupa de primirea mesajelor de la celelalte noduri.</li>
     * 	<li> La fiecare primire a unui nou heartbeat, se actualizeaza tabela de conexiuni.</li>
     * 	<li> Primirea se face incontinuu, fara timeout pe recv.</li>
     * </ul>
     * @param socket Socket-ul nodului curent
     * @return Runnable-ul pe baza caruia se va crea thread-ul de receptie a hearth-beat-urilor.
     */
    public Runnable receivingCRCRequestLoop(HearthBeatSocket socket){
        return new Runnable() {
            @Override
            public void run(){
                RequestCRC message;
                Address receivedAddress;
                while(true){
                    try{
                        message = (RequestCRC)socket.receiveMessage();
                        System.out.println("Am primit cerere de includere a crc-ului in pachet!");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                GeneralNode.calculateFileSystemCRC();
                            }
                        }).start();
                    }
                    catch (ClassCastException castException){
                        // nu facem nimic; aici primit beat de la celelalte noduri interne;
                        // nu avem ce face cu el; il ignoram!
                    }
                    catch (Exception exception){
                        System.out.println(exception.getMessage());
                    }
                }
            }
        };
    }

    /**
     * <ul>
     * 	<li>Acest manager de hearbeat-uri va trebui sa fie executat pe un thread separat,
     *      astfel incat sa nu blocheze comunicarea managerului general cu nodurile conectate.</li>
     * 	<li> Asadar, trebuie implementata functia run, care se va executa la apelul start.</li>
     * 	<li> Principala bucla care se ocupa de manevrarea heartbeat-urilor <strong>trimitere/receptie</strong>.</li>
     * </ul>
     */
    public void run(){
        try {
            LoggerService.registerSuccess(GeneralNode.ipAddress,String.format("Node with address [%s] started...", nodeAddress));
            InetAddress group = InetAddress.getByName(HearthBeatManager.multicastIPAddress);
            HearthBeatSocket socket = new HearthBeatSocket(nodeAddress, multicastPort);
            socket.setNetworkInterface(HearthBeatSocket.NetworkInterfacesTypes.LOCALHOST);
            socket.joinGroup(group);
            Thread sendingThread = new Thread(sendingHeartBeatLoop(group, socket));
            Thread receivingThread = new Thread(receivingCRCRequestLoop(socket));
            sendingThread.start();
            receivingThread.start();
        }
        catch (IOException exception){
            System.out.println(exception.getMessage());
        }
    }
}
