import communication.Address;
import communication.HearthBeatSocket;
import communication.Serializer;
import config.AppConfig;
import log.ProfiPrinter;
import logger.LoggerService;
import model.PendingQueueRegister;
import node_manager.Beat.NodeBeat;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import data.Time;
import node_manager.Beat.RequestCRC;

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

    private static int checkStorageHealthFrequency;


    /** -------- Constructor & Configurare -------- **/
    /**
     * Functie care citeste si initializeaza parametrii de configurare
     */
    public void readConfigParams(){
        multicastIPAddress = AppConfig.getParam("multicastIPAddress");
        multicastPort = Integer.parseInt(AppConfig.getParam("multicastPort"));
        frequency = Integer.parseInt(AppConfig.getParam("hearthBeatFrequency"));
        cleanupFrequency = Integer.parseInt(AppConfig.getParam("cleanupFrequency"));
        checkStorageHealthFrequency = Integer.parseInt(AppConfig.getParam("checkStorageHealthFrequency"));
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
                        ProfiPrinter.PrintException("Heartbeat Manager cleanup");
                        if(cleanUpIndex == cleanupFrequency){
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
                        socket.close();
                        LoggerService.registerError(GeneralManager.generalManagerIpAddress,
                                "InterruptedException occured. : " + exception.getMessage());
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
                        System.out.println("Am primit un hearthbeat de la " + receivedAddress + " ...");
                        if(!GeneralManager.connectionTable.containsAddress(receivedAddress)){
                            LoggerService.registerSuccess(GeneralManager.generalManagerIpAddress,
                                    " >>> [Adresa noua] : " + receivedAddress);
                            GeneralManager.connectionTable.addAddress(receivedAddress);
                        }
                        else {
                            GeneralManager.connectionTable.confirmAvailability(receivedAddress);
                        }
                        GeneralManager.statusTable.updateTable(message, GeneralManager.contentTable);
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

    public void checkForFileStatusChange(){
        long currentTimeStamp;
        for(int i = 0; i < 3; i++){
            try {
                PendingQueueRegister updateRequest = GeneralManager.pendingQueue.popFromQueue();
                currentTimeStamp = Time.getCurrentTimestamp();
                //if(Math.abs(updateRequest.getTimestamp() - currentTimeStamp) < frequency){
                    // asta apare mai ales atunci cand facem cerere de replicare;
                    // la replicare, nu asteptam feedback de la user, ci adaugam instant in lista
                    // ne asiguram ca modificarea statusului fisierului nu se inainte de a primi macar cu beat de la noduri
                    // bagam inregistrarea inapoi in coada si mai asteptam inca o perioada de cleanup
                    //GeneralManager.pendingQueue.addToQueue(updateRequest);
                    //continue;
                //}
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
            Thread cleanUpThread = new Thread(cleanUp(group, socket));
            Thread receivingThread = new Thread(receivingLoop(socket));
            Thread requestCRCThread = new Thread(requestCRC(group, socket));
            cleanUpThread.start();
            receivingThread.start();
            requestCRCThread.start();
        }
        catch (IOException exception){
            ProfiPrinter.PrintException(exception.getMessage());
        }
    }
}
