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
import logger.LoggerService;
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
        return String.join("-", candidates);
    }

    /**
     * Functie care va genera lantul de noduri la care se va stoca un fisier nou aparut in sistem.
     * Totadata, inregistreaza consumul de memorie in tabela de stocare a nodurilor.
     * @param filesize Dimensiunea fisierului ce va fi stocat
     * @param replication_factor Factorul de replicare al fisierului
     * @return Lantul de noduri la care se va stoca fisierului
     */
    public String generateNewChain(long filesize, int replication_factor) throws Exception{
        LoggerService.registerSuccess(GeneralManager.generalManagerIpAddress, "User uploaded a new file with size : " + filesize + " and replication factor : " + replication_factor);
        List<String> connectionAddresses = GeneralManager.connectionTable.getConnectionTable();
        if(connectionAddresses.size() <  replication_factor){
            LoggerService.registerWarning(GeneralManager.generalManagerIpAddress, "Nu sunt suficiente noduri disponibile");
            return null;
        }
        System.out.println("Generam token-ul..");
        List<String> candidates = GeneralManager.nodeStorageQuantityTable.getMostSuitableNodes(filesize);
        if(candidates.size() == 0){
            LoggerService.registerWarning(GeneralManager.generalManagerIpAddress, "Niciun nod nu are suficienta memorie pentru a stoca noul fisier.");
            return null;
        }
        String token = String.join("-", candidates.subList(0, replication_factor));
        System.out.println("====================================");
        System.out.println(token);
        System.out.println("====================================");

        return token;
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
    public void registerFileRequest(String user, String filename, long crc, long filesize, String userType, String status) throws Exception{
        synchronized (GeneralManager.contentTable){
            try {
                int replication_factor = getReplicationFactor(userType);
                String newFileStatus = "[PENDING]";
                if(!GeneralManager.contentTable.checkForUserFile(user, filename, -1)){
                    // aici putem ajunge doar la adaugarea unui nou fisier
                    GeneralManager.contentTable.addRegister(user, filename, replication_factor, crc, newFileStatus, "v1");
                }
                else{
                    // aici putem ajunge si rename si alte operatii asupra fisierului
                    GeneralManager.contentTable.updateFileStatus(user, filename, newFileStatus);
                    GeneralManager.contentTable.updateReplicationFactor(user, filename, replication_factor);
                    GeneralManager.contentTable.updateFileCRC(user, filename, crc);
                    GeneralManager.contentTable.updateFileVersionNo(user, filename, status.contains("DELETED")? 1 : -1);
                }
            }
            catch (Exception exception){
                LoggerService.registerError(GeneralManager.generalManagerIpAddress, "registerUserNewFileRequest exception : " + exception.getMessage());
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
                LoggerService.registerError(GeneralManager.generalManagerIpAddress,"confirmNewFileStorage exception : " + exception.getMessage());
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
                LoggerService.registerSuccess(GeneralManager.generalManagerIpAddress,
                        String.format("Client nou conectat : [%s : %d]\n", clientSocket.getLocalAddress(), clientSocket.getLocalPort()));
                new Thread(clientCommunicationThread(clientSocket)).start();
            }
        }
        catch (Exception exception){
            serverSocket.close();
            LoggerService.registerError(GeneralManager.generalManagerIpAddress,
                    "Client communication loop exception : " + exception.getMessage());
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
                                boolean crcMatch = false;
                                long crc = ((NewFileRequest)clientManagerRequest).getCrc();
                                switch (fileStatus){
                                    case FILE_EXISTS: {
                                        fileStatus = checkFileStatus(userId, filename, crc);
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
                                        String usertype = ((NewFileRequest) clientManagerRequest).getUserType();
                                        int replication_factor = getReplicationFactor(usertype);
                                        if (chain != null) {
                                            if(chain.equals("")){
                                                chain = generateNewChain(filesize, replication_factor);
                                            }
                                            response.setResponse(chain);
                                            LoggerService.registerSuccess(GeneralManager.generalManagerIpAddress, "Token-ul a fost trimis catre client : " + chain);
                                            System.out.println("Inregistram noul fisier.");
                                            String status = GeneralManager.contentTable.getFileStatusForUser(userId, filename);
                                            registerFileRequest(userId, filename, crc, filesize, usertype, status);
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
                                        String currentVersionNo = GeneralManager.statusTable.getLastVersionOfFile(userId, filename);
                                        long currentCRc = GeneralManager.statusTable.getCRCsForFile(userId, filename);
                                        String newName = ((RenameFileRequest)clientManagerRequest).getNewName();
                                        List<String> candidateNodes = GeneralManager.statusTable.getAvailableNodesAddressesForFile(userId, filename);
                                        String feedbackResponseStatus = GeneralManager.fileSystemManager.renameFile(userId, filename, newName, candidateNodes, clientManagerRequest.getDescription());
                                        GeneralManager.contentTable.updateFileName(userId, filename, newName);
                                        response.setResponse(feedbackResponseStatus);
                                        confirmUserRequest(userId, newName);
                                        GeneralManager.contentTable.addRegister(userId, filename, 0, currentCRc, "[DELETED]", currentVersionNo);
                                        GeneralManager.contentTable.updateFileVersionNo(userId, newName, -1);
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
                                       // GeneralManager.userStorageQuantityTable.registerMemoryRelease(clientManagerRequest.getUserId(), FileSystem.getFileSize(filepath));
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
                    LoggerService.registerError(GeneralManager.generalManagerIpAddress,
                        String.format("Could not properly close connection with my friend : [%s : %d]", clientSocket.getLocalAddress(), clientSocket.getLocalPort())
                    );
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
                    LoggerService.registerSuccess(GeneralManager.generalManagerIpAddress,
                            "Feedback valid de la frontend! Confirmam stocarea noului fisier.");
                    confirmUserRequest(userId, filename);
                }
                else{
                    LoggerService.registerError(GeneralManager.generalManagerIpAddress,
                            "Nu putem inregistra fisierul " + fileName + "!");
                }
            }
        }).start();
    }
}
