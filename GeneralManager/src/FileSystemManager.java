
import communication.Serializer;
import node_manager.DeleteRequest;
import node_manager.EditRequest;
import node_manager.RenameRequest;
import node_manager.ReplicationRequest;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;

public class FileSystemManager {
    private static int replicationPort = 8082;

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

    public void renameFile(String userId, String filename, String newname, List<String> candidates){
        for(String destinationAddress : candidates) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    RenameRequest renameRequest = new RenameRequest();
                    renameRequest.setUserId(userId);
                    renameRequest.setFilename(filename);
                    renameRequest.setNewname(newname);
                    System.out.println("dam drumu la treaba..");

                    makeRequestToFileSystem(destinationAddress, renameRequest);
                }
            }).start();
        }
    }

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
}
