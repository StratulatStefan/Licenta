import client_manager.ManagerResponse;
import client_manager.data.ClientManagerRequest;
import client_manager.data.DeleteFileRequest;
import client_manager.data.NewFileRequest;
import client_manager.data.RenameFileRequest;
import client_node.NewFileRequestFeedback;
import communication.Address;
import communication.Serializer;
import config.AppConfig;
import data.Pair;
import log.ProfiPrinter;
import node_manager.FeedbackResponse;
import os.FileSystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Clasa care va incapsula toata interactiunea dintre nodul general si client (frontend).
 * Principala actiune este de a asculta pentru cereri de prelucrare sau adaugare de fisiere si de a delega actiunea
 * inapoi sau nodurilor interne.
 */
public class ClientCommunicationManager {
    /** -------- Extra-descrieri -------- **/
    /**
     * Enum care va cuprinde toate tipurile de operatii solicitate de client
     */
    public enum ClientRequest{
        NEW_FILE,
        DELETE_FILE,
        RENAME_FILE
    }

    /**
     * Enum care va cuprinde statusul unui anumit fisier, raportat la tabela stocarii.
     */
    public enum ClientRequestStatus{
        FILE_EXISTS,
        FILE_NOT_FOUND
    }


    /** -------- Atribute -------- **/
    /**
     * Dimensiunea bufferului in care vor fi citite datele de la un nod adiacent
     */
    private static int bufferSize;
    /**
     * Port-ul de transmisie a datelor (client - nod general)
     */
    private static int dataTransmissionPort;

    /** -------- Constructor & Configurare -------- **/
    /**
     * Functie care citeste si initializeaza parametrii de configurare
     */
    public void readConfigParams(){
        bufferSize = Integer.parseInt(AppConfig.getParam("buffersize"));
        dataTransmissionPort = Integer.parseInt(AppConfig.getParam("dataTransmissionPort"));
    }

    /**
     * Constructorul clasei;
     * Citeste si instantiaza parametrii de configurare
     */
    public ClientCommunicationManager(){
        readConfigParams();
    }


    /** -------- Functii de validare -------- **/
    /**
     * Functie care verifica daca un anumit utilizator detine un anumit fisier;
     * Cautarea se face in tabela de content;
     * @param user Id-ul utilizatorului.
     * @param filename Numele fisierului cautat.
     */
    public ClientRequestStatus checkFileStatus(String user, String filename, long crc){
        try{
            boolean fileStatus = GeneralManager.contentTable.checkForUserFile(user, filename, crc);
            if(fileStatus){
                String status = GeneralManager.contentTable.getFileStatusForUser(user, filename);
                if(status.contains("DELETED"))
                    return ClientRequestStatus.FILE_NOT_FOUND;
                return ClientRequestStatus.FILE_EXISTS;
            }
            return ClientRequestStatus.FILE_NOT_FOUND;
        }
        catch (Exception exception){
            return ClientRequestStatus.FILE_NOT_FOUND;
        }
    }

    /**
     * Functie care identifica tipul operatiei solicitate de utilizator
     * @param operation String-ul ce identifica operatia
     * @return Tipul operatiei sau null daca nu s-a identificat nicio operatie valida
     */
    public ClientRequest getOperationType(Class<? extends ClientManagerRequest> operation){
        if(operation == NewFileRequest.class){
            return ClientRequest.NEW_FILE;
        }
        if(operation == DeleteFileRequest.class){
            return ClientRequest.DELETE_FILE;
        }
        if(operation == RenameFileRequest.class){
            return ClientRequest.RENAME_FILE;
        }
        return null;
    }


    /** -------- Functii de generare & inregistrare -------- **/
    /**
     * Functie care returneaza factorul de replicare specific tipului de utilizator;
     * Factorul de replicare va fi citit din fisierul de configurare
     */
    public int getReplicationFactor(String userType){
        switch (userType){
            case "STANDARD": return Integer.parseInt(AppConfig.getParam("basicUserReplicationFactor"));
            case "PREMIUM" : return Integer.parseInt(AppConfig.getParam("premiumUserReplicationFactor"));

        }
        return 0;
    }


    public String generateChainForUpdate(String userId, String filename){
        List<String> candidates = GeneralManager.statusTable.getAvailableNodesAddressesForFile(userId, filename);
        StringBuilder token = new StringBuilder();
        for(String address : candidates){
            token.append(address).append("-");
        }
        token = new StringBuilder(token.substring(0, token.length() - 1));
        return token.toString();
    }

    /**
     * Functie care va genera lantul de noduri la care se va stoca un fisier nou aparut in sistem.
     * Totadata, inregistreaza consumul de memorie in tabela de stocare a nodurilor.
     * @param filesize Dimensiunea fisierului ce va fi stocat
     * @param replication_factor Factorul de replicare al fisierului
     * @return Lantul de noduri la care se va stoca fisierului
     */
    public String generateNewChain(long filesize, int replication_factor) throws Exception{
        System.out.println("User uploaded a new file with size : " + filesize + " and replication factor : " + replication_factor);
        List<String> connectionAddresses = GeneralManager.connectionTable.getConnectionTable();
        if(connectionAddresses.size() <  replication_factor){
            System.out.println("Nu sunt suficiente noduri disponibile");
        }
        else{
            System.out.println("Generam token-ul..");
            StringBuilder token = new StringBuilder();
            List<String> candidates = GeneralManager.nodeStorageQuantityTable.getMostSuitableNodes(filesize);
            if(candidates.size() == 0){
                System.out.println("Niciun nod nu are suficienta memorie pentru a stoca noul fisier.");
                return null;
            }
            for(String address : candidates){
                if(replication_factor == 0){
                    break;
                }
                token.append(address).append("-");
                replication_factor -= 1;
            }
            token = new StringBuilder(token.substring(0, token.length() - 1));
            System.out.println("====================================");
            System.out.println(token.toString());
            System.out.println("====================================");

            return token.toString();
        }
        return null;
    }


    /**
     * TODO rework description
     * Functie apelata la adaugarea unui nou fisier;
     * Aadauga fisierul in tabela de content (cea care descrie toate fisierele care ar trebui sa fie existe in sistem);
     * Aceasta functie se apeleaza cand apare un nou fisier in sistem; se va pune inregistrarea in starea de PENDING;
     * Fisierul nu va fi considerat de catre mecanismul de replicare in aceasta stare (se asteapta pana se primeste confirmare,
     * adica se schimba starea in valid)
     * @param user Id-ul utilizatorului care a adaugat fisierul.
     * @param filename Numele fisierului.
     * @param userType Tipul utilizatorului, pe baza caruia se va determina si factorul de replicare, din fisierul de config.
     */
    public void registerFileRequest(String user, String filename, long crc, long filesize, String userType) throws Exception{
        synchronized (GeneralManager.contentTable){
            try {
                int replication_factor = getReplicationFactor(userType);
                String newFileStatus = "[PENDING]";
                if(!GeneralManager.contentTable.checkForUserFile(user, filename, -1)){
                    // aici putem ajunge doar la adaugarea unui nou fisier
                    GeneralManager.contentTable.addRegister(user, filename, replication_factor, crc, newFileStatus);
                }
                else{
                    // aici putem ajunge si rename si alte operatii asupra fisierului
                    GeneralManager.contentTable.updateFileStatus(user, filename, newFileStatus);
                    GeneralManager.contentTable.updateReplicationFactor(user, filename, replication_factor);
                    GeneralManager.contentTable.updateFileCRC(user, filename, crc);
                }
            }
            catch (Exception exception){
                ProfiPrinter.PrintException("registerUserNewFileRequest exception : " + exception.getMessage());
            }
        }
    }

    /**
     * TODO rework description
     * Functie apelata la confirmarea stocarii unui nou fisier
     * Schimba starea fisierului din pending in valid.
     * @param user Id-ul utilizatorului care a adaugat fisierul.
     * @param filename Numele fisierului.
     */
    public void confirmUserRequest(String user, String filename){
        synchronized (GeneralManager.contentTable){
            try {
                GeneralManager.pendingQueue.addToQueue(user, filename);
            }
            catch (Exception exception){
                ProfiPrinter.PrintException("confirmNewFileStorage exception : " + exception.getMessage());
            }
        }
    }


    /** -------- Main -------- **/
    /** Functie care inglobeaza activitatea principala a fiecarui nod, aceea de a asigura comunicarea cu celelalte noduri
     * in vederea trimiterii si primirii de mesaje.
     */
    public void clientCommunicationLoop(String generalManagerIpAddress) throws Exception{
        Address address = new Address(generalManagerIpAddress, dataTransmissionPort);
        ServerSocket serverSocket = new ServerSocket();
        try{
            serverSocket.bind(new InetSocketAddress(address.getIpAddress(), address.getPort()));
            while(true){
                Socket clientSocket = serverSocket.accept();
                System.out.println(String.format("Client nou conectat : [%s : %d]\n", clientSocket.getLocalAddress(), clientSocket.getLocalPort()));
                new Thread(clientCommunicationThread(clientSocket)).start();
            }
        }
        catch (Exception exception){
            serverSocket.close();
            ProfiPrinter.PrintException("Client communication loop exception : " + exception.getMessage());
        }
    }

    /**
     * Functie care inglobeaza comunicarea de date cu un nod adicant, avandu-se in vedere primirea de date de la un
     * nod adiacent si, eventual, trimiterea informatiilor mai departe, in cazul in care nu este nod terminal.
     * @param clientSocket Socket-ul nodului adiacent, de la care primeste date.
     * @return Runnable-ul necesar pornirii unui thread separat pentru aceasta comunicare.
     */
    public Runnable clientCommunicationThread(Socket clientSocket){
        return new Runnable() {
            @Override
            public void run(){
                try {
                    ManagerResponse response = new ManagerResponse();
                    DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                    DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                    String chain = "";
                    byte[] buffer = new byte[bufferSize];
                    while(dataInputStream.read(buffer, 0, bufferSize) > 0){
                        ClientManagerRequest clientManagerRequest = (ClientManagerRequest) Serializer.deserialize(buffer);
                        ClientRequest clientRequest = getOperationType(clientManagerRequest.getClass());

                        String userId = clientManagerRequest.getUserId();
                        String filename = clientManagerRequest.getFilename();
                        ClientRequestStatus fileStatus = checkFileStatus(userId, filename, -1);
                        switch (clientRequest){
                            case NEW_FILE:{
                                /* TODO trimitem inapoi la frontend ok la stocarea fisierului ? */
                                boolean crcMatch = false;
                                switch (fileStatus){
                                    case FILE_EXISTS: {
                                        fileStatus = checkFileStatus(userId, filename, ((NewFileRequest)clientManagerRequest).getCrc());
                                        switch (fileStatus){
                                            case FILE_EXISTS: {
                                                response.setException("FILE ALREADY EXISTS!");
                                                break;
                                            }
                                            case FILE_NOT_FOUND:{
                                                chain = generateChainForUpdate(userId, filename);
                                                crcMatch = true;
                                            }
                                        }
                                        if(!crcMatch){
                                            break;
                                        }
                                    }
                                    case FILE_NOT_FOUND: {
                                        long filesize = ((NewFileRequest)clientManagerRequest).getFilesize();
                                        long crc = ((NewFileRequest)clientManagerRequest).getCrc();
                                        String usertype = ((NewFileRequest) clientManagerRequest).getUserType();
                                        int replication_factor = getReplicationFactor(usertype);
                                        if(chain == ""){
                                            chain = generateNewChain(filesize, replication_factor);
                                        }
                                        if (chain != null) {
                                            response.setResponse(chain);
                                            System.out.println("Token-ul a fost trimis catre client : " + chain);
                                            System.out.println("Inregistram noul fisier.");
                                            registerFileRequest(userId, filename, crc, filesize, usertype);
                                            waitForFeedbackFromClient(userId, filename, filesize, usertype);
                                            /*try {
                                                GeneralManager.userDataTable.addUser(userId, usertype);
                                            }
                                            catch (Exception exception){
                                                ProfiPrinter.PrintException(exception.getMessage());
                                            }*/
                                            //GeneralManager.userStorageQuantityTable.registerMemoryConsumption(userId, usertype, filesize);
                                        }
                                        else {
                                            response.setException("eroare");
                                        }
                                        break;
                                    }
                                }
                                break;
                            }
                            case RENAME_FILE:{
                                switch (fileStatus){
                                    case FILE_NOT_FOUND:{
                                        response.setException("FILE NOT FOUND");
                                        break;
                                    }
                                    case FILE_EXISTS:{
                                        GeneralManager.contentTable.updateFileStatus(userId, filename, "[PENDING]");
                                        String newName = ((RenameFileRequest)clientManagerRequest).getNewName();
                                        List<String> candidateNodes = GeneralManager.statusTable.getAvailableNodesAddressesForFile(userId, filename);
                                        FeedbackResponse feedbackResponse = GeneralManager.fileSystemManager.renameFile(userId, filename, newName, candidateNodes, clientManagerRequest.getDescription());
                                        GeneralManager.contentTable.updateFileName(userId, filename, newName);
                                        response.setResponse(feedbackResponse.getStatus());
                                        GeneralManager.contentTable.updateFileStatus(userId, newName, "[VALID]");
                                        break;
                                    }
                                }
                                break;
                            }
                            case DELETE_FILE:{
                                switch (fileStatus){
                                    case FILE_NOT_FOUND: {
                                        response.setException("FILE NOT FOUND");
                                        break;
                                    }
                                    case FILE_EXISTS:{
                                        GeneralManager.contentTable.updateFileStatus(userId, filename, "[PENDING]");
                                        String candidateAddress = GeneralManager.statusTable.getAvailableNodesAddressesForFile(userId, filename).get(0);
                                        String filepath = GeneralManager.storagePath + candidateAddress + "/" + userId + "/" + filename;
                                        long filesize = FileSystem.getFileSize(filepath);
                                       // GeneralManager.userStorageQuantityTable.registerMemoryRelease(clientManagerRequest.getUserId(), filesize);
                                        GeneralManager.contentTable.updateReplicationFactor(userId, filename, 0);
                                        response.setResponse("OK");
                                        GeneralManager.contentTable.updateFileStatus(userId, filename, "[VALID]");
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                        dataOutputStream.write(Serializer.serialize(response));
                    }
                    System.out.println("Cerinta clientului a fost realizata..");
                    dataInputStream.close();
                    dataOutputStream.close();
                    clientSocket.close();
                }
                catch (Exception exception){
                    ProfiPrinter.PrintException(exception.getMessage());
                    ProfiPrinter.PrintException(String.format("Could not properly close connection with my friend : [%s : %d]",
                            clientSocket.getLocalAddress(),
                            clientSocket.getLocalPort()));
                }
            }
        };
    }

    public void waitForFeedbackFromClient(String userId, String filename, long filesize, String userType){
        new Thread(new Runnable() {
            @Override
            public void run() {
                /* TODO adaugare timeout */
                NewFileRequestFeedback feedback;
                while((feedback = GeneralManager.feedbackManager.getFeedback(userId, filename)) == null);
                String status = feedback.getStatus();
                String fileName = feedback.getFilename();
                String userID = feedback.getUserId();
                if(status.equals("OK") && fileName.equals(filename) && userID.equals(userId)) {
                    System.out.println("Feedback valid de la frontend!");
                    System.out.println("Confirmam stocarea noului fisier.");
                    confirmUserRequest(userId, filename);
                }
                else{
                    System.out.println("Nu putem inregistra fisierul!");
                }
            }
        }).start();
    }
}
