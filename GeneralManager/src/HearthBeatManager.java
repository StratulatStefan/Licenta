import communication.Address;
import communication.HearthBeatSocket;
import communication.Serializer;
import config.AppConfig;
import logger.LoggerService;
import model.PendingQueueRegister;
import node_manager.Beat.NodeBeat;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import data.Time;
import node_manager.Beat.RequestCRC;

/**
 * <ul>
 * 	<li>Clasa care se va ocupa de tot mecanismul de heartbeats.</li>
 * 	<li>Va primi mesaje frecvent de la fiecare nod intern si va stoca statusul acestora.</li>
 * 	<li>Totodata, periodic va realiza cereri de verificare a intergritatii fisierelor, prin solicitarea sumelor de control.</li>
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
     * Frecventa heartbeat-urilor
     * Exprimat in secunde.
     */
    private double frequency = Integer.parseInt(AppConfig.getParam("hearthBeatFrequency"));
    /**
     * Numarul de heart-beat-uri la care se face clean-up-ul tabelei de conexiuni
     */
    private static int cleanupFrequency = Integer.parseInt(AppConfig.getParam("cleanupFrequency"));
    /**
     * Frecventa cu care se realizeaza verificarea integritatii sistemului.
     */
    private static int checkStorageHealthFrequency = Integer.parseInt(AppConfig.getParam("checkStorageHealthFrequency"));


    /**
     * Constructorul managerului comunicatiei multicast.
     * @param address Adresa nodului curent
     */
    public HearthBeatManager(String address) throws Exception{
        this.nodeAddress = new Address(address, multicastPort);
    }


    /** -------- Principalele actiuni -------- **/
    /**
     * <ul>
     * 	<li>Functie care contine bucla de verificare a conexiunilor cu nodurile interne.</li>
     * 	<li>In cadrul unei iteratii se va verifica daca sunt noduri care nu au mai trimis <strong>heartbeat</strong>-uri.</li>
     * 	<li>Astfel de noduri vor fi eliminate, impreuna cu toate maparile din tabela de status, astfel incat sa se realizeze replicarea fisielor pe alte noduri.</li>
     * </ul>
     * @return Runnable pe baza caruia se va porni thread-ul de cleanup
     */
    public Runnable cleanUp(){
        return new Runnable(){
            @Override
            public void run(){
                List<Address> disconnected;
                int cleanUpIndex = 1;
                while(true) {
                    System.out.println(Time.getCurrentTimeWithFormat() + " ");
                    try {
                        if(cleanUpIndex == cleanupFrequency){
                            LoggerService.registerSuccess(GeneralManager.generalManagerIpAddress, "Heartbeat Manager cleanup");
                            checkForFileStatusChange();
                            disconnected = GeneralManager.connectionTable.checkDisconnection(frequency);
                            if(disconnected.size() != 0){
                                for (Address disconnectedAddres : disconnected) {
                                    LoggerService.registerWarning(GeneralManager.generalManagerIpAddress,
                                            " >>> Address " + disconnectedAddres + " disconnected");
                                    GeneralManager.connectionTable.removeAddress(disconnectedAddres);
                                    GeneralManager.statusTable.cleanUpAtNodeDisconnection(disconnectedAddres.getIpAddress());
                                }
                            }
                            cleanUpIndex = 0;
                        }
                        if(GeneralManager.connectionTable.size() == 0){
                            LoggerService.registerWarning(GeneralManager.generalManagerIpAddress,
                                    " >>> Niciun nod conectat!");
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
                        LoggerService.registerError(GeneralManager.generalManagerIpAddress,
                                "InterruptedException occured. : " + exception.getMessage());
                    }
                    System.out.println("\n");
                }
            }
        };
    }

    /**
     * <ul>
     * 	<li>Functie care se ocupa de primirea mesajelor de la celelalte noduri.</li>
     * 	<li> La fiecare primire a unui nouheartbeat, se actualizeaza <strong>tabela de conexiuni</strong> si <strong>tabela de status</strong>.</li>
     * </ul>
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
                        System.out.println("Am primit un hearthbeat de la " + receivedAddress + " ...");
                        if(!GeneralManager.connectionTable.containsAddress(receivedAddress)){
                            LoggerService.registerSuccess(GeneralManager.generalManagerIpAddress,
                                    " >>> [Adresa noua] : " + receivedAddress);
                            GeneralManager.connectionTable.addAddress(receivedAddress);
                        }
                        else {
                            GeneralManager.connectionTable.confirmAvailability(receivedAddress);
                        }
                        GeneralManager.statusTable.updateTable(message);
                        registerNodeStorageQuantity(receivedAddress.getIpAddress(), message.getMemoryQuantity());
                    }
                    catch (ClassCastException exception){
                        // mesajul trimis de acest nod a ajuns tot la el, ceea ce nu ne dorim sa se intample
                        // nu stim de ce se intampla, dar se intampla cateodata.
                        // primim RequestCRC
                    }
                    catch (Exception exception){
                        LoggerService.registerError(GeneralManager.generalManagerIpAddress,"Hearthbeatmanager receiveloop : " + exception.getMessage());
                    }
                }
            }
        };
    }

    /**
     * <ul>
     * 	<li>Functia de verificare a integritatii fisierelor.</li>
     * 	<li> Aceasta verificare se realizeaza cu ajutorul sumei de control.</li>
     * 	<li>Se va trimite catre fiecare nod intern o cerere <strong>RequestCRC</strong> de calculare a sumei de control si de includere a acesteia in hearbeat, imediat dupa finalizarea calcului.</li>
     * </ul>
     * @param group Grupul de multicast in care va fi trimisa cererea de verificare a integritatii.
     * @param socket Socket-ul pe care este mapata conexiunea la nodul curent.
     * @return
     */
    public Runnable requestCRC(InetAddress group, HearthBeatSocket socket){
        return new Runnable(){
            @Override
            public void run(){
                RequestCRC requestCRC = new RequestCRC();
                while(true) {
                    try {
                        Thread.sleep((int) (checkStorageHealthFrequency * 1e3));
                        LoggerService.registerSuccess(GeneralManager.generalManagerIpAddress,
                                Time.getCurrentTimeWithFormat() + " Se trimite cerere pentru CRC ...");
                        socket.sendBinaryMessage(group, Serializer.serialize(requestCRC));
                    } catch (IOException exception) {
                        socket.close();
                        LoggerService.registerError(GeneralManager.generalManagerIpAddress,
                                "IOException occured at requestCRC. : " + exception.getMessage());
                    } catch (InterruptedException exception) {
                        socket.close();
                        LoggerService.registerError(GeneralManager.generalManagerIpAddress,
                                "InterruptedException occured at requestCRC. : " + exception.getMessage());
                    }
                    System.out.println("\n");
                }
            }
        };
    }

    /**
     * <ul>
     * 	<li>Functie apelata in cadrul mecanismului de <strong>cleanup</strong> prin care se verifica
     *      daca exista in coada de asteptare, cereri de inregistrare de noi fisiere.</li>
     *  <li>Se vor extrage maxim 3 astfel de cereri in cadrul unei iteratii.</li>
     * </ul>
     */
    public void checkForFileStatusChange(){
        for(int i = 0; i < 3; i++){
            try {
                PendingQueueRegister updateRequest = GeneralManager.pendingQueue.popFromQueue();
                GeneralManager.contentTable.updateFileStatus(updateRequest.getUserId(), updateRequest.getFilename(), "[VALID]");
            }
            catch (NullPointerException exception) {
                break;
            }
            catch (Exception exception){
                LoggerService.registerError(GeneralManager.generalManagerIpAddress,"checkForFileStatusChange exception : " + exception.getMessage());
            }
        }
    }

    /**
     * <ul>
     * 	<li>Functie apelata la primirea unui heartbeat de la un nod intern,
     *      prin care se actualizeaza cantitatea de memorie disponibila pentru acesta.</li>
     * </ul>
     * @param nodeAddress Adresa nodului de la care s-a primit un <strong>heartbeat</strong>
     * @param quantity Cantitatea de memorie inregistrata.
     */
    public void registerNodeStorageQuantity(String nodeAddress, long quantity){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GeneralManager.nodeStorageQuantityTable.updateRegister(nodeAddress, quantity);
                } catch (Exception exception) {
                    LoggerService.registerError(GeneralManager.generalManagerIpAddress,"registerNodeStorageQuantity exception : " + exception.getMessage());
                }
            }
        }).start();
    }

    /**
     * <ul>
     * 	<li>Acest manager de hearbeat-uri va trebui sa fie executat pe un thread separat, astfel incat sa nu blocheze comunicarea managerului general cu nodurile conectate.</li>
     * 	<li> Asadar, trebuie implementata functia run, care se va executa la apelul start.</li>
     * 	<li>Principala bucla care se ocupa de manevrarea heartbeat-urilor <strong>trimitere/receptie</strong>.</li>
     * </ul>
     */
    public void run(){
        try {
            System.out.println(String.format("Node with address [%s] started...", nodeAddress));
            InetAddress group = InetAddress.getByName(HearthBeatManager.multicastIPAddress);
            HearthBeatSocket socket = new HearthBeatSocket(nodeAddress, multicastPort);
            socket.setNetworkInterface(HearthBeatSocket.NetworkInterfacesTypes.LOCALHOST);
            socket.joinGroup(group);
            Thread cleanUpThread = new Thread(cleanUp());
            Thread receivingThread = new Thread(receivingLoop(socket));
            Thread requestCRCThread = new Thread(requestCRC(group, socket));
            cleanUpThread.start();
            receivingThread.start();
            requestCRCThread.start();
        }
        catch (IOException exception){
            System.out.println(exception.getMessage());
        }
    }
}
