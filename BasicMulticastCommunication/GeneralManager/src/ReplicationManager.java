import communication.Serializer;
import node_manager.ReplicationRequest;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReplicationManager implements Runnable{
    private static int replicationFrequency;

    private int replicationPort;

    public ReplicationManager(int replicationPort, int replicationFrequency){
        this.replicationPort = replicationPort;
        this.replicationFrequency = replicationFrequency;
    }

    private void ReplicationLoop(){
        while(true) {
            System.out.println(GeneralManager.statusTable);
            System.out.println(GeneralManager.contentTable);

            System.out.println("------------------------------------");
            System.out.println("Replication Status");
            try {
                for (String userId : GeneralManager.contentTable.GetUsers()) {
                    HashMap<String, Integer> userFiles = GeneralManager.contentTable.GetUserFiles(userId);
                    for (String userFile : new ArrayList<>(userFiles.keySet())) {
                        int replication_factor = userFiles.get(userFile);
                        List<String> availableNodesForFile = GeneralManager.statusTable.GetAvailableNodesForFile(userId, userFile);
                        if(availableNodesForFile ==  null){
                            // eroare de sincronizare; se va rezolva la iteratia urmatoare
                            continue;
                        }
                        System.out.print("\t User " + userId + " | File : " + userFile + "  -->  ");
                        if (replication_factor > availableNodesForFile.size()) {
                            System.out.println("[NEED REPLICATION].");

                            List<String> candidates = SearchCandidatesForReplication(replication_factor, availableNodesForFile);
                            if(replication_factor == 1 || candidates == null){
                                System.out.println("Nu se poate realiza replicarea pentru fisierul curent. " +
                                        "Nu exista suficiente noduri pe care sa se faca replicarea.");
                            }
                            else {
                                // cautam un criteriu pe baza caruia selectam nodul de la care se face copierea
                                String source = availableNodesForFile.get(0);
                                System.out.println("\t\tFound source of replication : " + source);
                                System.out.print("\t\tFound candidates for replication : ");
                                for (String candidate : candidates) {
                                    System.out.print("[" + candidate + "] ");
                                }
                                System.out.println();
                                ReplicateFile(userId, userFile, source, candidates);
                            }
                        }
                        else if (replication_factor == availableNodesForFile.size()) {
                            System.out.println("[OK].");
                        }
                    }
                }
                System.out.println("------------------------------------\n");
                Thread.sleep((int) (replicationFrequency * 1e3));
            }
            catch (InterruptedException exception){
                System.out.println("Replication loop interrupted exception : " + exception.getMessage());
            }
        }
    }

    public List<String> SearchCandidatesForReplication(int replication_factor, List<String> availableNodes){
        List<String> openNodes = GeneralManager.connectionTable.getConnectionTable();
        // criteriu de selectie a anumitor noduri, mai libere, ca sa stocheze noul fisier
        // momentam selectam primele gasite

        try {
            return GeneralPurposeMethods.ListDifferences(openNodes, availableNodes).subList(0, replication_factor - availableNodes.size());
        }
        catch (IndexOutOfBoundsException exception){
            // nu s-au gasit suficiente noduri pe care sa se faca replicarea..
            return null;
        }
    }

    public void ReplicateFile(String user, String filename, String sourceAddress, List<String> destinationAddresses){
        for(String destinationAddress : destinationAddresses){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Trimit fisierul " + filename + " al userului " + user + " catre " + destinationAddress);

                    ReplicationRequest replicationRequest = new ReplicationRequest();
                    replicationRequest.setUserId(user);
                    replicationRequest.setFilename(filename);
                    replicationRequest.setDestionationAddress(destinationAddress);

                    try {
                        Socket replicationSocket = new Socket(sourceAddress, replicationPort);
                        DataOutputStream dataOutputStream = new DataOutputStream(replicationSocket.getOutputStream());
                        dataOutputStream.write(Serializer.Serialize(replicationRequest));
                        dataOutputStream.close();
                        replicationSocket.close();
                    }
                    catch (Exception exception){
                        System.out.println("Replicate file exception : " + exception.getMessage());
                    }
                }
            }).start();
        }
    }

    public void run(){
        this.ReplicationLoop();
    }

}