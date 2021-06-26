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
 * <ul>
 * 	<li>Clasa care va incapsula toata interactiunea dintre nodul general si client.</li>
 * 	<li>Principala actiune este de a asculta pentru cereri de prelucrare sau adaugare de fisiere si de a delega actiunea catre nodurilor interne.</li>
 * </ul>
 */
public class ClientCommunicationManager {
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

    /**
     * Dimensiunea unui pachet de date vehiculat prin canalul de comunicatie
     */
    private static int bufferSize = Integer.parseInt(AppConfig.getParam("buffersize"));
    /**
     * Port-ul de transmisie a datelor (client - nod general)
     */
    private static int dataTransmissionPort = Integer.parseInt(AppConfig.getParam("dataTransmissionPort"));
    /**
     * <ul>
     * 	<li>Adresa serverului cu interfata de tip Rest.</li>
     * 	<li>Va fi necesara pentru a se putea realiza conectarea la acest server,
     *       in vederea inregistrarii cantitatii de memorie ocupate de un fisier, in urma adaugarii.</li>
     * </ul>
     */
    private static String usersRestApi = AppConfig.getParam("usersRestApi");

    /** -------- Constructor & Configurare -------- **/
    /**
     * Constructorul clasei;
     */
    public ClientCommunicationManager(){
    }

    /**
     * Functie care identifica tipul operatiei solicitate de utilizator pe baza obiectului cerere primit
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
     * <ul>
     * 	<li>Functie care verifica daca un anumit utilizator detine un anumit fisier.</li>
     * 	<li>Cautarea se face in tabela de content.</li>
     * </ul>
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
     * <ul>
     * 	<li>Functie care returneaza factorul de replicare specific tipului de utilizator.</li>
     * 	<li>Factorul de replicare va fi citit din fisierul de configurare.</li>
     * </ul>
     */
    private int getReplicationFactor(String userType){
        switch (userType){
            case "STANDARD": return Integer.parseInt(AppConfig.getParam("basicUserReplicationFactor"));
            case "PREMIUM" : return Integer.parseInt(AppConfig.getParam("premiumUserReplicationFactor"));
        }
        return 0;
    }

    /**
     * <ul>
     * 	<li>La adaugarea unei versiuni actualizate a unui fisier, din punct de vedere a continutul,
     *      se va proceda ca in cazul procesuluide stocare a unui nou fisier.</li>
     * 	<li>Diferenta se va realiza in procesul de identificare a nodurilor interne care sa stocheze replicile fisierului.</li>
     * 	<li>In cazul unui nou fisier se vor cauta trei noi noduri care sa stocheze fisierul.</li>
     * 	<li>Aceasta functie va cauta adresele nodurilor care stocheaza deja fisierul.</li>
     * </ul>
     * @param userId Identificatorul unic al fisierului.
     * @param filename Numele fisierului.
     * @return Lista de noduri ce cont fisierul, organizata sub forma unui string.
     */
    private String generateChainForUpdate(String userId, String filename){
        List<String> candidates = GeneralManager.statusTable.getAvailableNodesAddressesForFile(userId, filename);
        return String.join("-", candidates);
    }

    /**
     * <ul>
     * 	<li>Functie care va genera lantul de noduri la care se va stoca un fisier nou aparut in sistem.</li>
     * 	<li>Totadata, inregistreaza consumul de memorie in tabela de stocare a nodurilor.</li>
     * </ul>
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
     * <ul>
     * 	<li>Functie apelata la adaugarea unui nou fisier.</li>
     * 	<li>Aadauga fisierul in tabela de content <strong>cea care descrie toate fisierele care ar trebui sa fie existe in sistem</strong>.</li>
     * 	<li>Aceasta functie se apeleaza cand apare un nou fisier in sistem.</li>
     * 	<li>Se va pune inregistrarea in starea de PENDING.</li>
     * 	<li>Fisierul nu va fi considerat de catre mecanismul de replicare in aceasta stare.</li>
     * 	<li> Se asteapta pana se primeste confirmare,adica se schimba starea in valid.</li>
     * </ul>
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
     * <ul>
     * 	<li>Functie apelata la confirmarea stocarii unui nou fisier.</li>
     * 	<li>Fisierul va fi adaugat intr-o coada de asteptare, de unde va fi scos abia atunci cand
     *      toate nodurile interne vor indica stocarea fisierului <strong>NodeBeat</strong>Schimba starea fisierului din pending in valid.</li>
     * </ul>
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

    /**
     * <ul>
     * 	<li>Functie care inregistreaza consumul din memoria utilizatorului, la adaugarea/eliminarea unui fisier.</li>
     * 	<li>Se realizeaza o cerere catre API-ul REST, care va salva aceste date in baza de date, in tabela de tip <strong>User</strong>.</li>
     * </ul>
     * @param user Identificatorul unic al utilizatorului
     * @param size Cantitatea de memorie consumata
     * @param consumption
     *        <ul>
     * 	        <li>true = a fost consumata memorie.</li>
     * 	        <li>false = a fost eliberata memorie.</li>
     *        </ul>
     */
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

    /**
     * <ul>
     * 	<li>Functia care instantiaza comunicarea cu clientul.</li>
     * 	<li> Se instantiaza un obiect de tip <strong>ServerSocket</strong>, prin intermediul caruia se va putea comunica cu clientul.</li>
     * 	<li>Se asteapta pentru conexiuni, urmand ca fiecare interactiune cu clientul sa fie tratata in mod paralel.</li>
     * </ul>
     */
    public void clientCommunicationLoop() throws Exception{
        Address address = new Address(GeneralManager.generalManagerIpAddress, dataTransmissionPort);
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
     * Functie care descrie functionalitatile necesare realizarii fiecarei operatii solicitate de client.
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

    /**
     * <ul>
     * 	<li>In cazul operatiei de adaugare a unui nou fisier, inregistrarea completa a fisierului in tabela de continut,
     *      cu starea <strong>VALID</strong> se realizeaza abia dupa ce se primeste confirmare ca fisierul a fost inregistrat cu succes.</li>
     * 	<li> Aceasta functie asteapta confirmarea din partea aplicatiei intermediar.</li>
     * </ul>
     */
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
