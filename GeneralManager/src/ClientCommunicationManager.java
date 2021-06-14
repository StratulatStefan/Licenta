import client_manager.ManagerComplexeResponse;
import client_manager.ManagerResponse;
import client_manager.ManagerTextResponse;
import client_manager.data.ClientManagerRequest;
import client_manager.data.*;
import client_node.NewFileRequestFeedback;
import communication.Address;
import communication.Serializer;
import config.AppConfig;
import http.HttpConnectionService;
import logger.LoggerService;
import os.FileSystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

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
        RENAME_FILE,
        GET_USER_FILES,
        GET_USER_FILE_HISTORY,
        GET_NODE_FOR_DOWNLOAD,
        GET_CONTENT_TABLE,
        GET_NODES_FOR_USER_FILE,
        GET_NODES_STORAGE_QUANTITY,
        GET_STORAGE_STATUS,
        GET_REPLICATION_STATUS,
        GET_CONNECTION_TABLE,
        DELETE_FILE_FROM_NODE
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
    private static int bufferSize = Integer.parseInt(AppConfig.getParam("buffersize"));
    /**
     * Port-ul de transmisie a datelor (client - nod general)
     */
    private static int dataTransmissionPort = Integer.parseInt(AppConfig.getParam("dataTransmissionPort"));
    private static String usersRestApi = AppConfig.getParam("usersRestApi");

    /** -------- Constructor & Configurare -------- **/
    /**
     * Constructorul clasei;
     * Citeste si instantiaza parametrii de configurare
     */
    public ClientCommunicationManager(){
    }

    /**
     * Functie care identifica tipul operatiei solicitate de utilizator
     * @param operation String-ul ce identifica operatia
     * @return Tipul operatiei sau null daca nu s-a identificat nicio operatie valida
     */
    private ClientRequest getOperationType(Class<? extends ClientManagerRequest> operation){
        if(operation == NewFileRequest.class){
            return ClientRequest.NEW_FILE;
        }
        if(operation == DeleteFileRequest.class){
            return ClientRequest.DELETE_FILE;
        }
        if(operation == RenameFileRequest.class){
            return ClientRequest.RENAME_FILE;
        }
        if(operation == GetUserFiles.class){
            return ClientRequest.GET_USER_FILES;
        }
        if(operation == GetUserFileHistory.class){
            return ClientRequest.GET_USER_FILE_HISTORY;
        }
        if(operation == GetNodeForDownload.class){
            return ClientRequest.GET_NODE_FOR_DOWNLOAD;
        }
        if(operation == GetContentTableRequest.class){
            return ClientRequest.GET_CONTENT_TABLE;
        }
        if(operation == GetNodesStorageQuantityRequest.class){
            return ClientRequest.GET_NODES_STORAGE_QUANTITY;
        }
        if(operation == GetNodesForFileRequest.class){
            return ClientRequest.GET_NODES_FOR_USER_FILE;
        }
        if(operation == GetStorageStatusRequest.class){
            return ClientRequest.GET_STORAGE_STATUS;
        }
        if(operation == GetReplicationStatusRequest.class){
            return ClientRequest.GET_REPLICATION_STATUS;
        }
        if(operation == GetConnectionTableRequest.class){
            return ClientRequest.GET_CONNECTION_TABLE;
        }
        if(operation == DeleteFileFromNodeRequest.class){
            return ClientRequest.DELETE_FILE_FROM_NODE;
        }
        return null;
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

    /** -------- Functii de generare & inregistrare -------- **/
    /**
     * Functie care returneaza factorul de replicare specific tipului de utilizator;
     * Factorul de replicare va fi citit din fisierul de configurare
     */
    private int getReplicationFactor(String userType){
        switch (userType){
            case "STANDARD": return Integer.parseInt(AppConfig.getParam("basicUserReplicationFactor"));
            case "PREMIUM" : return Integer.parseInt(AppConfig.getParam("premiumUserReplicationFactor"));
        }
        return 0;
    }

    private String generateChainForUpdate(String userId, String filename){
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
    private String generateNewChain(long filesize, int replication_factor) throws Exception{
        List<String> connectionAddresses = GeneralManager.connectionTable.getConnectionTable();
        if(connectionAddresses.size() <  replication_factor){
            LoggerService.registerError(GeneralManager.generalManagerIpAddress, "Nu sunt suficiente noduri disponibile");
            return null;
        }
        System.out.println("Generam token-ul..");
        List<String> candidates = GeneralManager.nodeStorageQuantityTable.getMostSuitableNodes(filesize);
        if(candidates.size() == 0){
            LoggerService.registerError(GeneralManager.generalManagerIpAddress, "Niciun nod nu are suficienta memorie pentru a stoca noul fisier.");
            return null;
        }
        LoggerService.registerSuccess(GeneralManager.generalManagerIpAddress, "User uploaded a new file with size : " + filesize + " and replication factor : " + replication_factor);
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
    public void registerFileRequest(String user, String filename, long crc, long filesize, String userType, String status, String versionDescription) throws Exception{
        synchronized (GeneralManager.contentTable){
            try {
                int replication_factor = getReplicationFactor(userType);
                String newFileStatus = "[PENDING]";
                if(!GeneralManager.contentTable.checkForUserFile(user, filename, -1)){
                    // aici putem ajunge doar la adaugarea unui nou fisier
                    GeneralManager.contentTable.addRegister(user, filename, replication_factor, crc, newFileStatus, filesize, "v1", versionDescription);
                }
                else{
                    // aici putem ajunge si rename si alte operatii asupra fisierului
                    GeneralManager.contentTable.updateFileStatus(user, filename, newFileStatus);
                    GeneralManager.contentTable.updateReplicationFactor(user, filename, replication_factor);
                    GeneralManager.contentTable.updateFileCRC(user, filename, crc);
                    GeneralManager.contentTable.updateFileSize(user, filename, filesize);
                    GeneralManager.contentTable.updateFileVersion(user, filename, status.contains("DELETED")? 1 : -1, versionDescription);
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

    public void registerUserMemoryConsumption(String user, long size, boolean consumption){
        HttpConnectionService httpConnectionService = new HttpConnectionService();
        String apiPath = usersRestApi + "/" + user + "/storage";
        Map<String, Object> consumptionData = new HashMap<String, Object>(){{
            put(consumption? "storage_quantity_consumption" : "storage_quantity_release", size);
        }};
        try{
            String registerResponse = httpConnectionService.putRequest(apiPath, consumptionData);
            LoggerService.registerSuccess(GeneralManager.generalManagerIpAddress, registerResponse);
        }
        catch (IOException exception){
            LoggerService.registerError(GeneralManager.generalManagerIpAddress, "Eroare la inregistrarea noului fisier : "+ exception.getMessage());
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
    private Runnable clientCommunicationThread(Socket clientSocket){
        return new Runnable() {
            @Override
            public void run(){
                ManagerResponse response = new ManagerResponse();
                DataOutputStream dataOutputStream = null;
                try {
                    dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                try {
                    DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                    String chain = "";
                    byte[] buffer = new byte[bufferSize];
                    while(dataInputStream.read(buffer, 0, bufferSize) > 0){
                        ClientManagerRequest clientManagerRequest = (ClientManagerRequest) Serializer.deserialize(buffer);

                        String userId = clientManagerRequest.getUserId();
                        String filename = clientManagerRequest.getFilename();
                        String description = clientManagerRequest.getDescription();
                        ClientRequestStatus fileStatus = checkFileStatus(userId, filename, -1);
                        switch (getOperationType(clientManagerRequest.getClass())){
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
                                            response = new ManagerTextResponse();
                                            ((ManagerTextResponse)response).setResponse(chain);
                                            LoggerService.registerSuccess(GeneralManager.generalManagerIpAddress, "Token-ul a fost trimis catre client : " + chain);
                                            System.out.println("Inregistram noul fisier.");
                                            String status = GeneralManager.contentTable.getFileStatusForUser(userId, filename);
                                            registerFileRequest(userId, filename, crc, filesize, usertype, status, description);
                                            waitForFeedbackFromClient(userId, filename, filesize);
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
                                        long filesize = GeneralManager.contentTable.getFileSizeOfUserFile(userId, filename);
                                        List<String> candidateNodes = GeneralManager.statusTable.getAvailableNodesAddressesForFile(userId, filename);
                                        String feedbackResponseStatus = GeneralManager.fileSystemManager.renameFile(userId, filename, newName, candidateNodes, clientManagerRequest.getDescription());
                                        GeneralManager.contentTable.updateFileName(userId, filename, newName);
                                        response = new ManagerTextResponse();
                                        ((ManagerTextResponse)response).setResponse(feedbackResponseStatus);
                                        confirmUserRequest(userId, newName);
                                        GeneralManager.contentTable.addRegister(userId, filename, 0, currentCRc, "[DELETED]", filesize, currentVersionNo, description);
                                        GeneralManager.contentTable.updateFileVersion(userId, newName, -1, description);
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
                                        registerUserMemoryConsumption(userId, FileSystem.getFileSize(filepath), false);
                                        GeneralManager.contentTable.updateReplicationFactor(userId, filename, 0);
                                        response = new ManagerTextResponse();
                                        ((ManagerTextResponse)response).setResponse("OK");
                                        GeneralManager.contentTable.updateFileStatus(userId, filename, "[VALID]");
                                        break;
                                    }
                                }
                                break;
                            }
                            case DELETE_FILE_FROM_NODE:{
                                switch (fileStatus){
                                    case FILE_NOT_FOUND: {
                                        response.setException("FILE NOT FOUND");
                                        break;
                                    }
                                    case FILE_EXISTS:{
                                        String address = ((DeleteFileFromNodeRequest)clientManagerRequest).getAddress();
                                        GeneralManager.fileSystemManager.deleteFile(userId, filename, Collections.singletonList(address));
                                        response = new ManagerTextResponse();
                                        ((ManagerTextResponse)response).setResponse("File successfully deleted from node.");
                                        break;
                                    }
                                }
                                break;
                            }
                            case GET_USER_FILES:{
                                response = new ManagerComplexeResponse();
                                ((ManagerComplexeResponse)response).setResponse(GeneralManager.contentTable.getUserFilesForFrontend(userId));
                                break;
                            }
                            case GET_USER_FILE_HISTORY:{
                                response = new ManagerComplexeResponse();
                                ((ManagerComplexeResponse)response).setResponse(GeneralManager.fileSystemManager.getUserFileHistoryForFrontend(userId, filename));
                                break;
                            }
                            case GET_NODE_FOR_DOWNLOAD:{
                                response = new ManagerTextResponse();
                                ((ManagerTextResponse)response).setResponse(GeneralManager.statusTable.getCandidateAddress(userId, filename, -1));
                                break;
                            }
                            case GET_CONTENT_TABLE:{
                                response = new ManagerComplexeResponse();
                                List<Object> contentTable = GeneralManager.contentTable.getContentTable()
                                        .stream()
                                        .map(node -> (Object)node)
                                        .collect(Collectors.toList());
                                ((ManagerComplexeResponse)response).setResponse(contentTable);
                                break;
                            }
                            case GET_NODES_FOR_USER_FILE:{
                                List<Object> fileNodes = GeneralManager.statusTable.getAvailableNodesAddressesForFile(userId, filename)
                                        .stream()
                                        .map(node -> (Object)node)
                                        .collect(Collectors.toList());
                                response = new ManagerComplexeResponse();
                                ((ManagerComplexeResponse)response).setResponse(fileNodes);
                                break;
                            }
                            case GET_NODES_STORAGE_QUANTITY:{
                                List<Object> fileNodes = GeneralManager.nodeStorageQuantityTable.getStorageQuantityTable()
                                        .stream()
                                        .map(node -> (Object)node)
                                        .collect(Collectors.toList());
                                response = new ManagerComplexeResponse();
                                ((ManagerComplexeResponse)response).setResponse(fileNodes);
                                break;
                            }
                            case GET_STORAGE_STATUS:{
                                response = new ManagerComplexeResponse();
                                List<Object> contentTable = GeneralManager.statusTable.getStorageStatusTable()
                                        .stream()
                                        .map(node -> (Object)node)
                                        .collect(Collectors.toList());
                                ((ManagerComplexeResponse)response).setResponse(contentTable);
                                break;
                            }
                            case GET_REPLICATION_STATUS:{
                                response = new ManagerComplexeResponse();
                                List<Object> replicationTable = ReplicationManager.getReplicationStatusTable()
                                        .stream()
                                        .map(node -> (Object)node)
                                        .collect(Collectors.toList());
                                ((ManagerComplexeResponse)response).setResponse(replicationTable);
                                break;
                            }
                            case GET_CONNECTION_TABLE :{
                                response = new ManagerComplexeResponse();
                                List<Object> connectionTable = GeneralManager.connectionTable.getConnectionTable()
                                        .stream()
                                        .map(node -> (Object)node)
                                        .collect(Collectors.toList());
                                ((ManagerComplexeResponse)response).setResponse(connectionTable);
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
                    response.setException(exception.getMessage());

                    try {
                        dataOutputStream.write(Serializer.serialize(response));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    System.out.println(exception.getMessage());
                    LoggerService.registerError(GeneralManager.generalManagerIpAddress,
                            String.format("Could not properly close connection with my friend : [%s : %d]", clientSocket.getLocalAddress(), clientSocket.getLocalPort())
                    );
                }
            }
        };
    }

    private void waitForFeedbackFromClient(String userId, String filename, long filesize){
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
                    registerUserMemoryConsumption(userId, filesize, true);
                }
                else{
                    LoggerService.registerError(GeneralManager.generalManagerIpAddress,
                            "Nu putem inregistra fisierul " + fileName + "!");
                }
            }
        }).start();
    }
}
