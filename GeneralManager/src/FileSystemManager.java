
import communication.Serializer;
import config.AppConfig;
import node_manager.DeleteRequest;
import node_manager.EditRequest;
import node_manager.RenameRequest;
import node_manager.ReplicationRequest;

import java.io.DataOutputStream;
import java.net.Socket;
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


    /** -------- Constructor & Configurare -------- **/
    /**
     * Functie care citeste si initializeaza parametrii de configurare
     */
    public static void readConfigParams(){
        replicationPort = Integer.parseInt(AppConfig.getParam("replicationPort"));
    }

    /**
     * Constructorul clasei;
     * Citeste si instantiaza parametrii de configurare
     */
    public FileSystemManager(){
    }


    /** -------- Trimitere cerere -------- **/
    /**
     * Functia care trimite un obiect de cerere de prelucrare catre nodul general.
     * @param destionationAddress Adresa nodului intern.
     * @param request Obiectul de cerere de prelucrare; Are tipul de baza al acestui tip de cerere de prelucrare (EditRequest)
     */
    public void makeRequestToFileSystem(String destionationAddress, EditRequest request){
        try{
            Socket deleteSocket = new Socket(destionationAddress, replicationPort);
            DataOutputStream dataOutputStream = new DataOutputStream(deleteSocket.getOutputStream());
            dataOutputStream.write(Serializer.serialize(request));
            dataOutputStream.close();
            deleteSocket.close();
        }
        catch (Exception exception){
            System.out.println("MakeRequestToFileSystem  exception : " + request.getClass() + " : " + exception.getMessage());
        }
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

                ReplicationRequest replicationRequest = new ReplicationRequest();
                replicationRequest.setUserId(user);
                replicationRequest.setFilename(filename);
                replicationRequest.setDestionationAddress(destinationAddresses);

                makeRequestToFileSystem(sourceAddress, replicationRequest);
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

                    DeleteRequest deleteRequest = new DeleteRequest();
                    deleteRequest.setUserId(user);
                    deleteRequest.setFilename(filename);

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
    public void renameFile(String userId, String filename, String newname, List<String> candidates){
        for(String destinationAddress : candidates) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    RenameRequest renameRequest = new RenameRequest();
                    renameRequest.setUserId(userId);
                    renameRequest.setFilename(filename);
                    renameRequest.setNewName(newname);
                    System.out.println("dam drumu la treaba..");

                    makeRequestToFileSystem(destinationAddress, renameRequest);
                }
            }).start();
        }
    }
}
