import communication.Serializer;
import config.AppConfig;
import logger.LoggerService;
import model.VersionData;
import node_manager.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <ul>
 * 	<li>Clasa care se ocupa de interactiunea cu nodurile interne, in ceea ce priveste prelucrarea fisierelor.</li>
 * 	<li>Principalele activitati sunt crearea obiectului cererii si trimiterea acestuia catre nodul intern.</li>
 * </ul>
 */
public class FileSystemManager {
    /**
     * Portul pe care nodul general asculta pentru aceste prelucrari.
     */
    private static int replicationPort = Integer.parseInt(AppConfig.getParam("replicationPort"));
    /**
     * Dimensiunea unui pachet de date vehiculat pe canalul de comunicatie.
     */
    private static int buffersize = Integer.parseInt(AppConfig.getParam("buffersize"));


    /**
     * Constructorul clasei;
     */
    public FileSystemManager(){
    }

    /**
     * Functia care trimite un obiect de tip cerere de prelucrare catre nodul general.
     * @param destionationAddress Adresa nodului intern.
     * @param request Obiectul de cerere de prelucrare; Are tipul de baza al acestui tip de cerere de prelucrare (EditRequest)
     */
    public FeedbackResponse makeRequestToFileSystem(String destionationAddress, EditRequest request){
        try{
            Socket socket = new Socket(destionationAddress, replicationPort);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write(Serializer.serialize(request));
            FeedbackResponse feedbackResponse = null;
            if(request.getClass() != ReplicationRequest.class){
                /* la replicare nu am nevoie de feedback de la nodul intern ;
                se cere feedback doar in cazul operatiilor la care am de trimis raspuns la client
                 */
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                byte[] buffer = new byte[buffersize];
                while(dataInputStream.read(buffer, 0, buffersize) > 0){
                    if(request.getClass() == VersionsRequest.class){
                        feedbackResponse = (FeedbackComplexeResponse)Serializer.deserialize(buffer);
                    }
                    else {
                        feedbackResponse = (FeedbackResponse) Serializer.deserialize(buffer);
                    }
                    break;
                }
                dataInputStream.close();
            }
            dataOutputStream.close();
            socket.close();
            LoggerService.registerSuccess(GeneralManager.generalManagerIpAddress, "Cererea de " + request.getClass().getSimpleName() + " a fost trimisa cu succes!");
            return feedbackResponse;
        }
        catch (Exception exception){
            LoggerService.registerError(GeneralManager.generalManagerIpAddress,
                    "MakeRequestToFileSystem  exception : " + request.getClass() + " : " + exception.getMessage());
        }
        return null;
    }

    /**
     * <ul>
     * 	<li>Functie care solicita toate versiunile unui fisier.</li>
     * 	<li> Se va extrage adresa unui nod intern care stocheaza fisierul, din tabela de status,se va trimite o cerere catre acel nod.</li>
     * 	<li> Rezultatul va fi returnat catre client.</li>
     * </ul>
     */
    public List<Object> getUserFileHistoryForFrontend(String user, String filename){
        long fileHash = GeneralManager.contentTable.getCRCForUser(user, filename);
        List<Object> result = new ArrayList<>();
        try {
            String nodeAddress = GeneralManager.statusTable.getCandidateAddress(user, filename, fileHash);
            VersionsRequest versionsRequest = new VersionsRequest(user, filename);
            List<Object> versions = (((FeedbackComplexeResponse)makeRequestToFileSystem(nodeAddress, versionsRequest)).getResponse());
            for(Object version : versions){
                for(VersionData versionData : (List<VersionData>)version){
                    HashMap<String, Object> fileVersion = new HashMap<String, Object>();
                    fileVersion.put("version_no", versionData.getVersionName());
                    String description = versionData.getDescription();
                    description = description.substring(description.indexOf(description.split("\\s")[3]));
                    fileVersion.put("version_desc", description);
                    fileVersion.put("version_hash", versionData.getHash());
                    fileVersion.put("version_timestamp", versionData.getTimestamp());
                    result.add(fileVersion);
                }
            }
            return result;
        }
        catch (Exception exception){
            LoggerService.registerError(GeneralManager.generalManagerIpAddress,
                    "getUserFileHistory : " + exception.getMessage());
            return null;
        }
    }

    /**
     * <ul>
     * 	<li>Cerere de replicare.</li>
     * 	<li>Construire obiect si trimitere.</li>
     * </ul>
     * @param user Id-ul utilizatorului.
     * @param filename Numele fisierului.
     * @param sourceAddress Adresa nodului catre care se trimite si care va initia replicarea.
     * @param destinationAddresses Adresa nodului la care se va stoca replica.
     */
    public void replicateFile(String user, String filename, String sourceAddress, List<String> destinationAddresses){
        new Thread(new Runnable() {
            @Override
            public void run() {
                LoggerService.registerSuccess(GeneralManager.generalManagerIpAddress,
                        "Trimit fisierul " + filename + " al userului " + user + " catre " + sourceAddress);
                ReplicationRequest replicationRequest = new ReplicationRequest(user, filename, destinationAddresses);
                makeRequestToFileSystem(sourceAddress, replicationRequest);

                try{
                    GeneralManager.pendingQueue.addToQueue(user, filename);
                }
                catch (Exception exception){
                    LoggerService.registerError(GeneralManager.generalManagerIpAddress,
                        "Replication : updatefilestatus1 : " + exception.getMessage());
                }
            }
        }).start();
    }

    /**
     * <ul>
     * 	<li>Cerere de eliminare.</li>
     * 	<li>Construire obiect si trimitere.</li>
     * 	<li>Se va trimite o cerere de eliminare catre fiecare nod din lista.</li>
     * </ul>
     * @param user Id-ul utilizatorului.
     * @param filename Numele fisierului.
     * @param destinationAddresses Adresele nodurilor de la care se va elimina fisierului.
     */
    public void deleteFile(String user, String filename, List<String> destinationAddresses){
        for(String destinationAddress : destinationAddresses){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    LoggerService.registerSuccess(GeneralManager.generalManagerIpAddress,
                        "Trimit cerere de eliminare pentru fisierul " + filename + " al userului " + user + " de la nodul " + destinationAddress);
                    DeleteRequest deleteRequest = new DeleteRequest(user, filename);
                    makeRequestToFileSystem(destinationAddress, deleteRequest);
                }
            }).start();
        }
    }

    /**
     * <ul>
     * 	<li>Cerere de redenumire.</li>
     * 	<li>Construire obiect si trimitere.</li>
     * 	<li>Se va trimite o cerere de redenumire catre fiecare nod din lista.</li>
     * </ul>
     * @param userId Id-ul utilizatorului.
     * @param filename Numele fisierului.
     * @param newname Noul nume al fisierului
     * @param candidates Adresele nodurilor de la care se va elimina fisierului.
     */
    public String renameFile(String userId, String filename, String newname, List<String> candidates, String description){
        List<FeedbackTextResponse> feedbackResponses = new ArrayList<FeedbackTextResponse>();
        List<Thread> threadPool = new ArrayList<>();
        for(String destinationAddress : candidates) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    RenameRequest renameRequest = new RenameRequest(userId, filename, newname, description);
                    LoggerService.registerSuccess(GeneralManager.generalManagerIpAddress,
                            "Trimitem cerere de replicare catre " + destinationAddress);

                    feedbackResponses.add((FeedbackTextResponse)makeRequestToFileSystem(destinationAddress, renameRequest));
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
                    System.out.println("Interrupt exception la renameFile thread!");
                }
            }
        }
        FeedbackTextResponse feedbackResponse = new FeedbackTextResponse();
        for(FeedbackTextResponse feedback : feedbackResponses){
            feedbackResponse.setSuccess(feedback.isSuccess());
            feedbackResponse.setStatus(feedback.getStatus());
            if(feedbackResponse.isSuccess())
                break;
        }
        LoggerService.registerSuccess(GeneralManager.generalManagerIpAddress,
                "Status overall : " + feedbackResponse.getStatus());
        return feedbackResponse.getStatus();
    }
}
