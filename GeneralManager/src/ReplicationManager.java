import config.AppConfig;
import data.Pair;
import logger.LoggerService;
import model.FileVersionData;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReplicationManager implements Runnable{
    /** -------- Atribute -------- **/
    /**
     * Frecventa de executare a buclei de replicare
     */
    private static int replicationFrequency = Integer.parseInt(AppConfig.getParam("replicationFrequency"));
    private static List<String> replicationStatusTable = new ArrayList<>();

    /** -------- Constructor & Configurare -------- **/

    /**
     * Constructorul clasei;
     * Citeste si instantiaza parametrii de configurare
     */
    public ReplicationManager(){

    }


    /** -------- Functii de cautare -------- **/
    /**
     * Functie care parcurge tabela de status a nodurilor conectate si identifica
     * nodurile care ar putea stocare un anumit fisier (trebuie sa nu contina fisierul respectiv
     * si sa aiba spatiu);
     * @param replication_factor Numarul de noduri necesare.
     * @param availableNodes Lista nodurilor care contin deja fisieru
     */
    private List<String> searchCandidatesForReplication(int replication_factor, List<String> availableNodes, long filesize){
        List<String> openNodes = GeneralManager.nodeStorageQuantityTable.getMostSuitableNodes(filesize).subList(0, replication_factor);
        try {
            return GeneralPurposeMethods.listDifferences(openNodes, availableNodes).subList(0, replication_factor - availableNodes.size());
        }
        catch (IndexOutOfBoundsException exception){
            // nu s-au gasit suficiente noduri pe care sa se faca replicarea..
            return null;
        }
    }

    /**
     * Functie care returneaza nodurile care contin un anumit fisier ;
     * Trebuie sa se elimine fisierul de la aceste noduri;
     * @param count Numarul de noduri care trebuie sa elimine fisierul.
     * @param availableNodes Lista tuturor nodurilor disponibile care contin fisierul.
     */
    private List<String> searchCandidatesForDeletion(int count, List<String> availableNodes){
        return availableNodes.subList(0, count);
    }

    public static List<String> getReplicationStatusTable(){
        return replicationStatusTable;
    }

    /** -------- Main -------- **/
    public void run(){
        while(true) {
            replicationStatusTable.clear();
            System.out.println(GeneralManager.statusTable);
            System.out.println(GeneralManager.contentTable);
            System.out.println(GeneralManager.nodeStorageQuantityTable);

            System.out.println("------------------------------------");
            System.out.println("Replication Status");
            try {
                for (String userId : GeneralManager.contentTable.getUsers()) {
                    HashMap<String, Integer> userFiles = GeneralManager.contentTable.getUserFiless(userId);
                    for (String userFile : new ArrayList<>(userFiles.keySet())) {
                        int replication_factor = userFiles.get(userFile);
                        List<Pair<String, FileVersionData>> availableNodesForFile = GeneralManager.statusTable.getAvailableNodesForFile(userId, userFile);
                        List<String> availableNodesAddressesForFile = GeneralManager.statusTable.getAvailableNodesAddressesForFile(userId, userFile);
                        if(availableNodesForFile ==  null){
                            // eroare de sincronizare; se va rezolva la iteratia urmatoare a for-ului prin useri
                            continue;
                        }
                        String replicationStatus = "\t User " + userId + " | File : " + userFile + "  -->  ";
                        if (replication_factor == availableNodesForFile.size()) {
                            if(GeneralManager.contentTable.getFileStatusForUser(userId, userFile).contains("PENDING")) {
                                replicationStatus += "[UNKNOWN]\n";
                                System.out.println(replicationStatus);
                                replicationStatusTable.add(replicationStatus);
                                continue;
                            }
                            long crc = GeneralManager.contentTable.getCRCForUser(userId, userFile);
                            String versionNo = GeneralManager.contentTable.getVersionForUser(userId, userFile);
                            String corruptedFileAddress = this.checkForFileCorruption(crc, versionNo, availableNodesForFile);
                            if(corruptedFileAddress == null) {
                                replicationStatus += "[OK].";
                                System.out.println(replicationStatus);
                                replicationStatusTable.add(replicationStatus);
                            }
                            else{
                                List<String> candidatesForDeletion = new ArrayList<String>(){{
                                    add(corruptedFileAddress);
                                }};
                                replicationStatus += this.deletion(replication_factor, userId, userFile, candidatesForDeletion);
                                replicationStatusTable.add(replicationStatus);
                                continue;
                            }
                        }
                        else if (replication_factor > availableNodesForFile.size() && !GeneralManager.contentTable.getFileStatusForUser(userId, userFile).contains("PENDING")) {
                            long filesize = GeneralManager.contentTable.getFileSizeOfUserFile(userId, userFile);
                            replicationStatus += this.replication(replication_factor, userId, userFile, availableNodesAddressesForFile, filesize);
                            replicationStatusTable.add(replicationStatus);
                        }
                        else if(replication_factor < availableNodesForFile.size()){
                            List<String> candidates = searchCandidatesForDeletion(availableNodesForFile.size() - replication_factor, availableNodesAddressesForFile);
                            replicationStatus += this.deletion(replication_factor, userId, userFile, candidates);
                            replicationStatusTable.add(replicationStatus);
                        }
                        else{
                            replicationStatus += "[UNKNOWN]\n";
                            System.out.println(replicationStatus);
                            replicationStatusTable.add(replicationStatus);
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

    private String checkForFileCorruption(long crc, String versionNo,  List<Pair<String, FileVersionData>> availableNodesForFile){
        if(crc == -1){
            return null;
        }
        for(Pair<String, FileVersionData> file : availableNodesForFile){
            if(file.getSecond().getCrc() != -1 && file.getSecond().getCrc() != crc && file.getSecond().getVersionNo() != versionNo){
                LoggerService.registerWarning(GeneralManager.generalManagerIpAddress, "Found corrupted file at address : " + file.getFirst());
                return file.getFirst();
            }
        }
        return null;
    }

    private String replication(int replication_factor, String userId, String userFile, List<String> availableNodesAddressesForFile, long filesize){
        String status = "[NEED REPLICATION]. " + userFile + " of user " +  userId + "\n";
        LoggerService.registerWarning(GeneralManager.generalManagerIpAddress, status);
        List<String> candidates = searchCandidatesForReplication(replication_factor, availableNodesAddressesForFile, filesize);
        if(replication_factor == 1 || candidates == null){
            LoggerService.registerWarning(GeneralManager.generalManagerIpAddress,"Nu se poate realiza replicarea pentru fisierul curent. " +
                    "Nu exista suficiente noduri pe care sa se faca replicarea.");
        }
        else {
            try{
                GeneralManager.contentTable.updateFileStatus(userId, userFile, "[PENDING]");
            }
            catch (Exception exception){
                System.out.println("Replication : updatefilestatus1 : " + exception.getMessage());
            }
            // cautam un criteriu pe baza caruia selectam nodul de la care se face copierea
            String source = availableNodesAddressesForFile.get(0);
            status += "\t\tFound source of replication : " + source + "\n\t\tFound candidates for replication : ";
            System.out.println("\t\tFound source of replication : " + source);
            System.out.print("\t\tFound candidates for replication : ");
            for (String candidate : candidates) {
                status += "[" + candidate + "] ";
                System.out.print("[" + candidate + "] ");
            }
            System.out.println();
            GeneralManager.fileSystemManager.replicateFile(userId, userFile, source, candidates);
        }
        return status;
    }

    private String deletion(int replication_factor, String userId, String userFile, List<String> candidates) throws Exception {
        String status = String.format("[NEED DELETION OF FILE %s]\n", userFile);
        LoggerService.registerWarning(GeneralManager.generalManagerIpAddress,status);
        status += "\t\tFound nodes to delete file : ";
        System.out.print("\t\tFound nodes to delete file : ");
        for (String candidate : candidates) {
            status += "[" + candidate + "] ";
            System.out.print("[" + candidate + "] ");
        }
        System.out.println();
        GeneralManager.fileSystemManager.deleteFile(userId, userFile, candidates);
        if(replication_factor == 0) {
            GeneralManager.contentTable.updateFileStatus(userId, userFile, "[DELETED]");
        }
        return status;
    }
}
