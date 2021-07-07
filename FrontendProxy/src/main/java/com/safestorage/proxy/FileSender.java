package com.safestorage.proxy;

import client_manager.data.ClientManagerRequest;
import client_node.DownloadFileRequest;
import client_node.FileHeader;
import client_node.NewFileRequestFeedback;
import com.safestorage.FrontendProxyUiApplication;
import com.safestorage.controller.FileController;
import communication.Serializer;
import config.AppConfig;

import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * <ul>
 * 	<li>Clasa care expune toate modele necesare vehicularii de fisiere intre client si nodurile interne.</li>
 * 	<li>Fiecare cerere va incepe cu o comunicare cu managerul general, in vederea extragerii informatiilor specifice cererii.</li>
 * 	<li> Sunt expuse metodele de <strong>upload</strong> si <strong>download</strong>.</li>
 * 	<li>Pentru fiecare operatie, se vor expune si metodele de asteptare a feedback-ului de la nodurile interne.</li>
 * 	<li>Parametrii de configurare vor fi incarcati din fisierul de configurare, cu ajutorul clasei <strong>AppConfig</strong></li>
 * </ul>
 */
public class FileSender {
    /**
     * Dimensiunea unui pachet de date primit pe canalul de comunicatie, in comunicarea cu nodul intern.
     */
    private static int bufferSize               = Integer.parseInt(AppConfig.getParam("buffersize"));
    /**
     * Portul nodului general.
     */
    private static int generalManagerPort       = Integer.parseInt(AppConfig.getParam("generalManagerPort"));
    /**
     * Adresa nodului general.
     */
    private static String generalManagerAddress = AppConfig.getParam("generalManagerAddress");
    /**
     * Portul de feedback.
     */
    private static int feedbackPort             = Integer.parseInt(AppConfig.getParam("feedbackport"));
    /**
     * Calea la care vor fi stocate fisierele clientului, in urma descarcarii de la nodurile interne.
     */
    private static String downloadFilePath      = AppConfig.getParam("filedownloadpath");
    /**
     * Timeout-ul pentru primirea feeedback-ului.
     */
    private static Long feedbackRecvTimeout     = Long.parseLong(AppConfig.getParam("feedbackRecvTimeout"));

    /**
     * <ul>
     * 	<li>Fiecare fisier al unui utilizator va fi stocat in mai multe replici, pe mai multe noduri interne.</li>
     * 	<li> Nodul general va decide care vor fi aceste noduri,prin intermediul unui <strong>token</strong>, ce va contine un lant cu nodurile care vor stoca fisierul.</li>
     * 	<li> Aceasta functie verifica daca <strong>token</strong>-ul respecta forma de lant iarfiecare element individual reprezinta o adresa IP valida.</li>
     * </ul>
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

    /**
     * <ul>
     * 	<li>Functie prin care sunt extrase adresele nodurilor interne din <strong>token</strong>.</li>
     * 	<li> Inainte de extragere, se va verifica daca token-ul este valid,cu ajutorul metodei <strong>validateToken</strong>.</li>
     * </ul>
     * @return Lista de adrese.
     * @throws Exception Token invalid.
     */
    private static String[] getAddressesFromToken(String token) throws Exception{
        if(validateToken(token))
            return token.replace(" ","").split("\\-");
        return null;
    }

    /**
     * <ul>
     * 	<li>Functie prin care se realizeaza tot procesul trimiterii unui fisier catre primul din lantul de adrese.</li>
     * 	<li> Se vor trimite mai intai metadatele, sub formaobiectului de tip <strong>FileHeader</strong>, apoi se va trimite fluxul de octeti ce alcatuiesc fisierul.</li>
     * </ul>
     * @param clientManagerRequest Cererea clientului, de stocare a unui fisier.
     * @param token Token-ul primit de la nodul general.
     */
    public static void sendFile(ClientManagerRequest clientManagerRequest, String token){
        try {
            String destinationAddress = getAddressesFromToken(token)[0];

            Socket socket = new Socket(destinationAddress, generalManagerPort);
            FileHeader fileHeader = new FileHeader();
            fileHeader.setFilename(clientManagerRequest.getFilename());
            fileHeader.setToken(token);
            fileHeader.setFilesize(new File(clientManagerRequest.getFilename()).length());
            fileHeader.setUserId(clientManagerRequest.getUserId());
            fileHeader.setDescription(clientManagerRequest.getDescription());

            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(clientManagerRequest.getFilename()));
            outputStream.write(Serializer.serialize(fileHeader));

            byte[] binaryFile = new byte[bufferSize];
            int count;
            while ((count = inputStream.read(binaryFile)) > 0) {
                outputStream.write(binaryFile, 0, count);
            }
            inputStream.close();
            outputStream.close();
            socket.close();
        }
        catch (Exception exception){
            System.out.println("Exceptie la trimiterea unui nou fisier: " + exception.getMessage());
        }
    }

    /**
     * <ul>
     * 	<li>Functie care expune procesul receptionarii si validarii feedback-ului primit de la nodurile interne in urma procesului de stocare a unui fisier.</li>
     * 	<li>In mod paralel, se asteapta un feedback de la fiecare nod intern.</li>
     * 	<li> Un feedback se va considera valid daca vor corespunde atat numele fisierului,cat si suma de control.</li>
     * 	<li> Pentru a valida feedback-ul global, se impune ca cel putin un feedback primit sa fie valid.</li>
     * 	<li>Daca feedback-ul global este valid, se trimite confirmare catre nodul general si catre client.</li>
     * </ul>
     * @param userId Identificatorul unic al utilizatorului care detine fisierul.
     * @param token Lista de fisiere de la care se asteapta feedback.
     * @param filename Numele fisierului.
     * @param timeout Timpul de expirare al asteptarii feedback-ului de la un nod.
     * @param CRC Suma de control a fisierului.
     * @throws Exception Feedback invalid sau Timeout.
     */
    public static void waitForFeedback(String userId, String token, String filename, long timeout, long CRC) throws Exception {
        int total_nodes = 0;
        int received_nodes = 0;
        final int[] valid_nodes = {0};
        boolean another_exception = false;
        String[] fnamelist = filename.split("\\\\");
        final String fname = fnamelist[fnamelist.length - 1];
        final List<Thread> threads = new ArrayList<>();
        try {
            final List<String> addresses = new LinkedList<String>(Arrays.asList(getAddressesFromToken(token)));
            final List<Long> feedbackRecvTimes = new ArrayList<Long>();
            total_nodes = addresses.size();
            while(received_nodes != total_nodes){
                received_nodes += 1;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long starting_time = System.currentTimeMillis();
                        long recvLimit;
                        try{
                            NewFileRequestFeedback feedback;
                            while ((feedback   = FrontendProxyUiApplication.feedbackManager.getFeedback(userId, fname)) == null){
                                synchronized (feedbackRecvTimes) {
                                    recvLimit = feedbackRecvTimes.isEmpty() ? feedbackRecvTimeout : Collections.max(feedbackRecvTimes) * 5;
                                    if ((System.currentTimeMillis() - starting_time) / 1e3 > recvLimit)
                                        throw new Exception("Feedback receive timeout!");
                                }
                            }
                            String nodeAddress = feedback.getNodeAddress();
                            String fileName    = feedback.getFilename();
                            String userID      = feedback.getUserId();
                            long crc           = feedback.getCrc();
                            System.out.print(String.format("Feedback primit de la : [%s]\n", nodeAddress));
                            if (fileName.equals(fname) && userID.equals(userId) && CRC == crc) {
                                System.out.println(" >> [OK]");
                                valid_nodes[0] += 1;
                            } else
                                System.out.println(" >> [INVALID]");
                            feedbackRecvTimes.add((long)((System.currentTimeMillis() - starting_time) / 1e3));
                        }
                        catch (Exception exception){
                            System.out.println("Exceptie la primirea feedback-ului! : " + exception.getMessage());
                        }
                    }
                });
                threads.add(thread);
                thread.start();
            }
        }
        catch (Exception exception){
            System.out.println("Exceptie : " + exception.getMessage());
            another_exception = true;
        }
        finally {
            System.out.println("Am primit feedback de la " + received_nodes + "/" + total_nodes);
            for(Thread thread : threads){
                if(thread.isAlive()){
                    thread.join();
                }
            }
            if(another_exception || received_nodes == 0 || valid_nodes[0] == 0) {
                sendFeedbackToGM(userId, fname, "ERROR");
                return;
            }
            if(valid_nodes[0] >= 1){
                System.out.println("Sending confirmation feedback to general manager");
                sendFeedbackToGM(userId, fname, "OK");
                FileController.uploadPendingQueue.addToQueue(userId, filename);
            }
        }
    }

    /**
     * <ul>
     * 	<li>Functie prin care se expune functionalitatea trimiterii feedback-ului catre nodul general, in urma primirii feedback-ului de la nodurile interne.</li>
     * 	<li>Comunicarea cu nodul general se va realiza pe un <strong>thread</strong> separat, prin trimiterea obiectelor de tip <strong>NewFileRequestFeedback</strong>.</li>
     * </ul>
     * @param userId Identificatorul unic al utilizatorului care detine fisierul.
     * @param filename Numele fisierului.
     * @param status Feedback-ul primit de la nodurile interne, ce trebuie trimis catre nodul general.
     */
    public static void sendFeedbackToGM(String userId, String filename, String status){
        new Thread(new Runnable() {
            @Override
            public void run() {
                NewFileRequestFeedback feedback = new NewFileRequestFeedback();
                feedback.setFilename(filename);
                feedback.setUserId(userId);
                feedback.setStatus(status);
                try{
                    Socket frontendSocket = new Socket(generalManagerAddress, feedbackPort);
                    DataOutputStream dataOutputStream = new DataOutputStream(frontendSocket.getOutputStream());

                    dataOutputStream.write(Serializer.serialize(feedback));

                    dataOutputStream.close();
                    frontendSocket.close();
                }
                catch (IOException exception){
                    System.out.println("Exceptie IO la sendFeedBackToGM : " + exception.getMessage());
                }
            }
        }).start();
    }

    /**
     * <ul>
     * 	<li>Functie care expune tot procesul descarcarii unui fisier.</li>
     * 	<li> Se creeaza cererea de descarcare a unui fisier <strong>DownloadFileRequest</strong>, care este trimisa catre nodul intern,
     * 	     identificat prin adresa furnizata de nodul general.</li>
     * 	<li> Se primeste de la nodul intern fluxul de octeti ce alcatuiesc fisier sise scrie fisierul in memoria proprie.</li>
     * </ul>
     * @param destionationAddress Adresa nodului intern care va furniza fisierul.
     * @param userId Identificatorul unic al utilizatorului ce detine fisierul.
     * @param filename Numele fisierului.
     * @return Calea catre locatia din sistemul de fisiere unde a fost salvat fisierul.
     */
    public static String downloadFile(String destionationAddress, String userId, String filename){
        try {
            DownloadFileRequest downloadFileRequest = new DownloadFileRequest();
            downloadFileRequest.setFilename(filename);
            downloadFileRequest.setUserId(userId);

            Socket socket = new Socket(destionationAddress, generalManagerPort);

            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.write(Serializer.serialize(downloadFileRequest));
            String filepath = downloadFilePath + filename;
            FileOutputStream fileOutputStream = new FileOutputStream(filepath);

            InputStream dataInputStream = new DataInputStream(socket.getInputStream());

            byte[] binaryFile = new byte[bufferSize];
            int count;
            while ((count = dataInputStream.read(binaryFile)) > 0) {
                fileOutputStream.write(binaryFile, 0, count);
            }

            fileOutputStream.close();
            dataInputStream.close();
            outputStream.close();
            socket.close();
            return "/buffer/" + filename;
        }
        catch (Exception exception){
            System.out.println("Exceptie la trimiterea unui nou fisier: " + exception.getMessage());
            return null;
        }
    }
}
