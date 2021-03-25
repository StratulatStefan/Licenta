import communication.Serializer;
import config.AppConfig;
import node_manager.DeleteRequest;
import node_manager.ReplicationRequest;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReplicationManager implements Runnable{
    private static int replicationFrequency;

    private int replicationPort;

    public ReplicationManager(){
        readConfigParams();
    }

    public List<String> searchCandidatesForReplication(int replication_factor, List<String> availableNodes){
        List<String> openNodes = GeneralManager.connectionTable.getConnectionTable();
        // criteriu de selectie a anumitor noduri, mai libere, ca sa stocheze noul fisier
        // momentam selectam primele gasite

        try {
            return GeneralPurposeMethods.listDifferences(openNodes, availableNodes).subList(0, replication_factor - availableNodes.size());
        }
        catch (IndexOutOfBoundsException exception){
            // nu s-au gasit suficiente noduri pe care sa se faca replicarea..
            return null;
        }
    }

    public List<String> searchCandidatesForDeletion(int count, List<String> availableNodes){
        return availableNodes.subList(0, count);
    }

    public void readConfigParams(){
        replicationPort = Integer.parseInt(AppConfig.getParam("replicationPort"));
        replicationFrequency = Integer.parseInt(AppConfig.getParam("replicationFrequency"));
    }

    public void run(){
        while(true) {
            System.out.println(GeneralManager.statusTable);
            System.out.println(GeneralManager.contentTable);

            System.out.println("------------------------------------");
            System.out.println("Replication Status");
            try {
                for (String userId : GeneralManager.contentTable.getUsers()) {
                    HashMap<String, Integer> userFiles = GeneralManager.contentTable.getUserFiles(userId);
                    for (String userFile : new ArrayList<>(userFiles.keySet())) {
                        int replication_factor = userFiles.get(userFile);
                        List<String> availableNodesForFile = GeneralManager.statusTable.getAvailableNodesForFile(userId, userFile);
                        if(availableNodesForFile ==  null){
                            // eroare de sincronizare; se va rezolva la iteratia urmatoare a for-ului prin useri
                            continue;
                        }
                        System.out.print("\t User " + userId + " | File : " + userFile + "  -->  ");
                        if (replication_factor == availableNodesForFile.size()) {
                            System.out.println("[OK].");
                        }
                        else if (replication_factor > availableNodesForFile.size()) {
                            System.out.println("[NEED REPLICATION].");

                            List<String> candidates = searchCandidatesForReplication(replication_factor, availableNodesForFile);
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
                                GeneralManager.fileSystemManager.replicateFile(userId, userFile, source, candidates);
                            }
                        }
                        else {
                            System.out.println("[NEED DELETION OF FILE FROM ONE NODE]");

                            List<String> candidates = searchCandidatesForDeletion(availableNodesForFile.size() - replication_factor, availableNodesForFile);
                            System.out.print("\t\tFound nodes to delete file : ");
                            for (String candidate : candidates) {
                                System.out.print("[" + candidate + "] ");
                            }
                            System.out.println();
                            GeneralManager.fileSystemManager.deleteFile(userId, userFile, candidates);
                            if(replication_factor == 0) {
                                GeneralManager.contentTable.deleteRegister(userId, userFile);
                            }
                        }
                    }
                }
                System.out.println("------------------------------------\n");
                Thread.sleep((int) (replicationFrequency * 1e3));
            }
            catch (Exception exception){
                System.out.println("Replication loop interrupted exception : " + exception.getMessage());
            }
        }
    }

}
