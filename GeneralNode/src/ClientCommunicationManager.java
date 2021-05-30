import client_node.DownloadFileRequest;
import client_node.NewFileRequestFeedback;
import communication.Address;
import client_node.FileHeader;
import communication.Serializer;
import config.AppConfig;
import log.ProfiPrinter;
import os.FileSystem;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Struct;
import java.time.Year;

/**
 * Clasa care va incapsula toata interactiunea dintre nodul intern si client (frontend).
 * Principala actiune este de a asculta pentru cereri de adaugare de fisiere.
 */
public class ClientCommunicationManager {
    /** -------- Atribute -------- **/
    /**
     * Adresa nodului curent
     */
    private Address nodeAddress;

    private static int feedbackPort;

    private static String frontendIpAddress;
    /**
     * Dimensiunea bufferului in care vor fi citite datele de la un nod adiacent
     */
    private static int bufferSize;
    /**
     * Calea de baza la care se vor stoca fisierele
     */
    private static String storagePath;
    /**
     * Portul pe care este mapat socket-ul de transmitere a datelor
     */
    private static int dataTransmissionPort;


    /** -------- Constructor & Configurare -------- **/
    /**
     * Functie care citeste si initializeaza parametrii de configurare
     */
    public void readConfigParams(){
        bufferSize = Integer.parseInt(AppConfig.getParam("buffersize"));
        dataTransmissionPort = Integer.parseInt(AppConfig.getParam("dataTransmissionPort"));
        storagePath = AppConfig.getParam("storagePath");
        frontendIpAddress = AppConfig.getParam("frontendAddress");
        feedbackPort = Integer.parseInt(AppConfig.getParam("feedbackPort"));
    }

    /**
     * Constructorul clasei;
     * Citeste si instantiaza parametrii de configurare
     */
    public ClientCommunicationManager(String address) throws Exception{
        readConfigParams();
        this.nodeAddress = new Address(address, dataTransmissionPort);
    }


    /** -------- Functii de prelucrare -------- **/
    /**
     * Functie care genereaza noul Stream de iesire pentru socket-ul de transmitere a fisierului catre urmatorul nod din lant.
     * @param socket Socket-ul de comunicare cu urmatorul nod din lant
     * @param header Header-ul fisierului ce va fi transmis
     */
    public DataOutputStream generateNewFileDataStream(Socket socket, FileHeader header) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        System.out.println("Next node in chain header : " + header.getToken());
        dataOutputStream.write(Serializer.serialize(header));
        return dataOutputStream;
    }

    /**
     * Functie care pregateste token-ul pentru urmatorul nod din lant;
     * Prelucrarea presupune eliminarea nodului curent din lant (prima adresa din lant)
     * @param token Lantul de noduri.
     */
    private static String cleanChain(String token){
        if(token.contains("-")){
            int delimiter = token.indexOf("-");
            return token.substring(delimiter + 1);
        }
        else{
            return null;
        }
    }


    /** -------- Gettere -------- **/
    /**
     * Functie care extrage urmatoarea adresa din lant, catre care se va trimite in continuare fisierul
     * @param token Lantul de adrese.
     */
    private static String getDestinationIpAddress(String token) throws Exception{
        if(validateToken(token))
            return token.replace(" ","").split("\\-")[0];
        return null;
    }


    /** -------- Functii de validare -------- **/
    /**
     * Functie valideaza daca tokenul curent este valid;
     * Nu este null si contine adrese IP valide.
     * @param token Token-ul ce trebuie verificat.
     * @throws Exception Generata daca token-ul este invalid.
     */
    private static boolean validateToken(String token) throws Exception{
        if(token.length() == 0)
            throw new Exception("Null token!");
        String[] tokenItems = token.split("\\-");
        for(String tokenItem : tokenItems){
            String[] values = tokenItem.split("\\.");
            if(values.length != 4)
                throw new Exception("Invalid token! The address is not a valid IP Address (invalid length!)");
            for(String value : values){
                try{
                    int parsedValue = Integer.parseInt(value);
                    if(parsedValue < 0 || parsedValue > 255)
                        throw new Exception("Invalid token! The address is not a valid IP Address (8 bit representation of values)");
                }
                catch (NumberFormatException exception) {
                    throw new Exception("Invalid token! The address is not a valid IP Address (it should only contain numbers!)");
                }
            }
        }
        return true;
    }


    /** -------- Main -------- **/
    /**
     * Functie care inglobeaza comunicarea de date cu un nod adicant, avandu-se in vedere primirea de date de la un
     * nod adiacent si, eventual, trimiterea informatiilor mai departe, in cazul in care nu este nod terminal.
     * @param serverAddress Adresa socket-ului pe care este mapat nodul curent.
     * @param clientSocket Socket-ul nodului adiacent, de la care primeste date.
     * @return Runnable-ul necesar pornirii unui thread separat pentru aceasta comunicare.
     */
    private Runnable clientCommunicationThread(String serverAddress, Socket clientSocket){
        return new Runnable() {
            @Override
            public void run(){
                try {
                    InputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                    OutputStream dataOutputStream = null;
                    FileOutputStream fileOutputStream = null;
                    Socket nextElementSocket = null;
                    FileHeader fileHeader = null;
                    String filepath = null;
                    byte[] buffer = new byte[bufferSize];
                    int read;
                    boolean header_found = false;
                    while((read = dataInputStream.read(buffer, 0, bufferSize)) > 0){
                        if(!header_found) {
                            try {
                                fileHeader = (FileHeader) Serializer.deserialize(buffer);
                                filepath = storagePath + serverAddress + "/" + fileHeader.getUserId();
                                if(!Files.exists(Paths.get(filepath)))
                                    Files.createDirectories(Paths.get(filepath));
                                GeneralNode.pendingList.addToList(fileHeader.getUserId(), fileHeader.getFilename());
                                filepath += "/" + fileHeader.getFilename();
                                fileOutputStream = new FileOutputStream(filepath);
                                String token = cleanChain(fileHeader.getToken());
                                if(token != null){
                                    fileHeader.setToken(token);
                                    String nextDestination = getDestinationIpAddress(token);
                                    nextElementSocket = new Socket(nextDestination, nodeAddress.getPort());
                                    dataOutputStream = generateNewFileDataStream(nextElementSocket, fileHeader);
                                }
                                else{
                                    System.out.println("End of chain");
                                }
                                header_found = true;
                                continue;
                            }
                            catch (ClassCastException exception){
                                DownloadFileRequest downloadFileRequest = (DownloadFileRequest)Serializer.deserialize(buffer);
                                FileSystemManager.downloadFile(clientSocket, downloadFileRequest.getUserId(), downloadFileRequest.getFilename());
                            }
                            catch (Exception exception) {
                                ProfiPrinter.PrintException("Exceptie : " + exception.getMessage());
                            }
                        }
                        else {
                            fileOutputStream.write(buffer, 0, read);
                        }
                        if(nextElementSocket != null){
                            dataOutputStream.write(buffer, 0, read);
                        }
                    }
                    System.out.println("File write done");
                    // send feedback to frontend
                    dataInputStream.close();
                    fileOutputStream.close();
                    if(nextElementSocket != null) {
                        nextElementSocket.close();
                        dataOutputStream.close();
                    }
                    clientSocket.close();
                    GeneralNode.pendingList.removeFromList(fileHeader.getUserId(), fileHeader.getFilename());
                    long filecrc = FileSystem.calculateCRC(filepath);
                    GeneralNode.versionControlManager.registerFileVersion(fileHeader.getUserId(), fileHeader.getFilename(), filecrc, fileHeader.getDescription());
                    GeneralNode.crcTable.updateRegister(fileHeader.getUserId(), fileHeader.getFilename(), filecrc);
                    sendFeedbackToFrontend(fileHeader, filecrc);
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

    /**
     * Functie care inglobeaza activitatea de comunicare cu clientul si cu celelalte noduri din sistem;
     * Comunicarea cu clientul are in vedere receptionarea de noi fisiere (cazul in care nodul intern este primul din lant)
     * Comunicarea cu celelalte noduri interne se face in scopul trimiterii/primirii de fisiere (cazul in care nodul intern
     * nu se afla primul in lant)
     * @throws IOException Exceptie generata la crearea ServerSocket-ului.
     */
    public void clientCommunicationLoop() throws Exception {
        ServerSocket serverSocket = new ServerSocket();
        try{
            serverSocket.bind(new InetSocketAddress(nodeAddress.getIpAddress(), nodeAddress.getPort()));
            while(true){
                Socket clientSocket = serverSocket.accept();
                System.out.println(String.format("Client connected : [%s : %d]\n", clientSocket.getLocalAddress(), clientSocket.getLocalPort()));
                new Thread(clientCommunicationThread(serverSocket.getInetAddress().getHostAddress(), clientSocket)).start();
            }
        }
        catch (Exception exception){
            serverSocket.close();
            ProfiPrinter.PrintException(exception.getMessage());
        }
    }

    public void sendFeedbackToFrontend(FileHeader fileHeader, long crc){
        new Thread(new Runnable() {
            @Override
            public void run() {
                NewFileRequestFeedback feedback = new NewFileRequestFeedback();
                feedback.setFilename(fileHeader.getFilename());
                feedback.setUserId(fileHeader.getUserId());
                feedback.setNodeAddress(nodeAddress.getIpAddress());
                feedback.setCrc(crc);

                Socket frontendSocket = null;
                DataOutputStream dataOutputStream = null;

                try{
                    frontendSocket = new Socket(frontendIpAddress, feedbackPort);
                    dataOutputStream = new DataOutputStream(frontendSocket.getOutputStream());

                    dataOutputStream.write(Serializer.serialize(feedback));

                    dataOutputStream.close();
                    frontendSocket.close();
                }
                catch (IOException exception){
                    ProfiPrinter.PrintException("Exceptie IO la sendFeedBackToFrontend : " + exception.getMessage());
                }
                finally {
                    try{
                        dataOutputStream.close();
                        frontendSocket.close();
                    }
                    catch(IOException exception){
                        ProfiPrinter.PrintException("sendFeedBackToFrontend exceptie la inchidere socket-uri");
                    }
                }
            }
        }).start();
    }
}
