
import communication.Serializer;
import config.AppConfig;
import log.ProfiPrinter;
import node_manager.*;

import java.awt.datatransfer.FlavorEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Clasa care se ocupa de interactiunea cu nodurile generale, in ceea ce priveste prelucrarea fisierelor;
 * Principalele activitati sunt crearea obiectului cererii si trimiterea acestuia catre nodul general.
 */
public class FileSystemManager {
    /** -------- Atribute -------- **/
    /**
     * Portul pe care nodul general asculta pentru aceste prelucrari.
     */
    private static int replicationPort;

    private static int buffersize;


    /** -------- Constructor & Configurare -------- **/
    /**
     * Functie care citeste si initializeaza parametrii de configurare
     */
    public static void readConfigParams(){
        buffersize = Integer.parseInt(AppConfig.getParam("buffersize"));
        replicationPort = Integer.parseInt(AppConfig.getParam("replicationPort"));
    }

    /**
     * Constructorul clasei;
     * Citeste si instantiaza parametrii de configurare
     */
    public FileSystemManager(){
    }


    /** -------- Trimitere cerere -------- **/
    /* TODO schimba nume obiecte */
    /**
     * Functia care trimite un obiect de cerere de prelucrare catre nodul general.
     * @param destionationAddress Adresa nodului intern.
     * @param request Obiectul de cerere de prelucrare; Are tipul de baza al acestui tip de cerere de prelucrare (EditRequest)
     */
    public FeedbackResponse makeRequestToFileSystem(String destionationAddress, EditRequest request){
        try{
            Socket deleteSocket = new Socket(destionationAddress, replicationPort);
            DataOutputStream dataOutputStream = new DataOutputStream(deleteSocket.getOutputStream());
            dataOutputStream.write(Serializer.serialize(request));
            FeedbackResponse feedbackResponse = null;
            if(request.getClass() != ReplicationRequest.class){
                /* la replicare nu am nevoie de feedback de la nodul intern ;
                se cere feedback doar in cazul operatiilor la care am de trimis raspuns la client
                 */
                DataInputStream dataInputStream = new DataInputStream(deleteSocket.getInputStream());
                byte[] buffer = new byte[buffersize];
                while(dataInputStream.read(buffer, 0, buffersize) > 0){
                    feedbackResponse = (FeedbackResponse)Serializer.deserialize(buffer);
                    break;
                }
                dataInputStream.close();
            }
            dataOutputStream.close();
            deleteSocket.close();
            return feedbackResponse;
        }
        catch (Exception exception){
            ProfiPrinter.PrintException("MakeRequestToFileSystem  exception : " + request.getClass() + " : " + exception.getMessage());
        }
        return null;
    }


    /** -------- Construirea obiecte cerere & Trimitere -------- **/
    /**
     * Cerere de replicare;
     * Construire obiect si trimitere.
     * @param user Id-ul utilizatorului.
     * @param filename Numele fisierului.
     * @param sourceAddress Adresa nodului catre care se trimite si care va initia replicarea.
     * @param destinationAddresses Adresa nodului la care se va stoca replica.
     */
    public void replicateFile(String user, String filename, String sourceAddress, List<String> destinationAddresses){
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Trimit fisierul " + filename + " al userului " + user + " catre " + sourceAddress);
                ReplicationRequest replicationRequest = new ReplicationRequest(user, filename, destinationAddresses);
                makeRequestToFileSystem(sourceAddress, replicationRequest);

                try{
                    GeneralManager.pendingQueue.addToQueue(user, filename);
                }
                catch (Exception exception){
                    ProfiPrinter.PrintException("Replication : updatefilestatus1 : " + exception.getMessage());
                }
            }
        }).start();
    }

    /**
     * Cerere de eliminare;
     * Construire obiect si trimitere.
     * Se va trimite o cerere de eliminare catre fiecare nod din lista.
     * @param user Id-ul utilizatorului.
     * @param filename Numele fisierului.
     * @param destinationAddresses Adresele nodurilor de la care se va elimina fisierului.
     */
    public void deleteFile(String user, String filename, List<String> destinationAddresses){
        for(String destinationAddress : destinationAddresses){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Trimit cerere de eliminare pentru fisierul " + filename + " al userului " + user + " de la nodul " + destinationAddress);
                    DeleteRequest deleteRequest = new DeleteRequest(user, filename);
                    makeRequestToFileSystem(destinationAddress, deleteRequest);
                }
            }).start();
        }
    }

    /**
     * Cerere de redenumire;
     * Construire obiect si trimitere.
     * Se va trimite o cerere de redenumire catre fiecare nod din lista.
     * @param userId Id-ul utilizatorului.
     * @param filename Numele fisierului.
     * @param newname Noul nume al fisierului
     * @param candidates Adresele nodurilor de la care se va elimina fisierului.
     */
    public String renameFile(String userId, String filename, String newname, List<String> candidates, String description){
        List<FeedbackResponse> feedbackResponses = new ArrayList<FeedbackResponse>();
        List<Thread> threadPool = new ArrayList<>();
        for(String destinationAddress : candidates) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    RenameRequest renameRequest = new RenameRequest(userId, filename, newname, description);
                    System.out.println("Trimitem cerere de replicare catre " + destinationAddress);

                    feedbackResponses.add(makeRequestToFileSystem(destinationAddress, renameRequest));
                    System.out.println("Am primis feedback de la " + destinationAddress);

                }
            });
            threadPool.add(thread);
            thread.start();
        }
        for(Thread thread : threadPool){
            if(thread.isAlive()) {
                try {
                    thread.join();
                }
                catch (InterruptedException exception){
                    ProfiPrinter.PrintException("Interrupt exception la renameFile thread!");
                }
            }
        }
        return getOverallFeedback(feedbackResponses).getStatus();
    }

    public FeedbackResponse getOverallFeedback(List<FeedbackResponse> feedbackResponses){
        FeedbackResponse feedbackResponse = new FeedbackResponse();
        for(FeedbackResponse feedback : feedbackResponses){
            feedbackResponse.setSuccess(feedback.isSuccess());
            feedbackResponse.setStatus(feedback.getStatus());
            if(feedbackResponse.isSuccess())
                break;
        }
        System.out.println("Status overall : " + feedbackResponse.getStatus());
        return feedbackResponse;
    }
}
