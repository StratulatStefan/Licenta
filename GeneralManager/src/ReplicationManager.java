import config.AppConfig;
import data.Pair;
import log.ProfiPrinter;
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
    private static int replicationFrequency;

    /**
     * Portul pe care este deschis socket-ul de pe nodul intern
     */
    private int replicationPort;


    /** -------- Constructor & Configurare -------- **/
    /**
     * Functie care citeste si initializeaza parametrii de configurare
     */
    public void readConfigParams(){
        replicationPort = Integer.parseInt(AppConfig.getParam("replicationPort"));
        replicationFrequency = Integer.parseInt(AppConfig.getParam("replicationFrequency"));
    }

    /**
     * Constructorul clasei;
     * Citeste si instantiaza parametrii de configurare
     */
    public ReplicationManager(){
        readConfigParams();
    }


    /** -------- Functii de cautare -------- **/
    /**
     * Functie care parcurge tabela de status a nodurilor conectate si identifica
     * nodurile care ar putea stocare un anumit fisier (trebuie sa nu contina fisierul respectiv
     * si sa aiba spatiu);
     * @param replication_factor Numarul de noduri necesare.
     * @param availableNodes Lista nodurilor care contin deja fisieru
     */
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

    /**
     * Functie care returneaza nodurile care contin un anumit fisier ;
     * Trebuie sa se elimine fisierul de la aceste noduri;
     * @param count Numarul de noduri care trebuie sa elimine fisierul.
     * @param availableNodes Lista tuturor nodurilor disponibile care contin fisierul.
     */
    public List<String> searchCandidatesForDeletion(int count, List<String> availableNodes){
        return availableNodes.subList(0, count);
    }


    /** -------- Main -------- **/
    public void run(){
        while(true) {
            System.out.println(GeneralManager.statusTable);
            System.out.println(GeneralManager.contentTable);
            System.out.println(GeneralManager.nodeStorageQuantityTable);
            System.out.println(GeneralManager.userStorageQuantityTable);

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
                        System.out.print("\t User " + userId + " | File : " + userFile + "  -->  ");
                        if (replication_factor == availableNodesForFile.size()) {
                            if(GeneralManager.contentTable.getFileStatusForUser(userId, userFile).contains("PENDING")) {
                                System.out.println("[UNKNOWN]\n");
                                continue;
                            }
                            long crc = GeneralManager.contentTable.getCRCForUser(userId, userFile);
                            String versionNo = GeneralManager.contentTable.getVersionForUser(userId, userFile);
                            String corruptedFileAddress = this.checkForFileCorruption(crc, versionNo, availableNodesForFile);
                            if(corruptedFileAddress == null) {
                                System.out.println("[OK].");
                            }
                            else{
                                List<String> candidatesForDeletion = new ArrayList<String>(){{
                                    add(corruptedFileAddress);
                                }};
                                this.deletion(replication_factor, userId, userFile, candidatesForDeletion);
                                continue;
                            }
                        }
                        else if (replication_factor > availableNodesForFile.size() && !GeneralManager.contentTable.getFileStatusForUser(userId, userFile).contains("PENDING")) {
                            this.replication(replication_factor, userId, userFile, availableNodesAddressesForFile);
                        }
                        else if(replication_factor < availableNodesForFile.size()){
                            List<String> candidates = searchCandidatesForDeletion(availableNodesForFile.size() - replication_factor, availableNodesAddressesForFile);
                            this.deletion(replication_factor, userId, userFile, candidates);
                        }
                        else{
                            System.out.println("[UNKNOWN]\n");
                        }
                    }
                    // verificam daca sunt fisiere care sunt in storage status table, dar nu sunt in tabela de content (situatie intalnita atunci cand un nod moare si, intre timp,
                    // un fisier a fost redenumit); nodul care invie va declara fisierul, dar contenttable nu va sti de el
                    // TODO aici.. cu redenumirea..

                }
                System.out.println("------------------------------------\n");
                Thread.sleep((int) (replicationFrequency * 1e3));
            }
            catch (Exception exception){
                ProfiPrinter.PrintException("Replication loop interrupted exception : " + exception.getMessage());
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

    private void replication(int replication_factor, String userId, String userFile, List<String> availableNodesAddressesForFile){
        LoggerService.registerWarning(GeneralManager.generalManagerIpAddress,"[NEED REPLICATION]. " + userFile + " of user " +  userId);
        List<String> candidates = searchCandidatesForReplication(replication_factor, availableNodesAddressesForFile);
        if(replication_factor == 1 || candidates == null){
            LoggerService.registerWarning(GeneralManager.generalManagerIpAddress,"Nu se poate realiza replicarea pentru fisierul curent. " +
                    "Nu exista suficiente noduri pe care sa se faca replicarea.");
        }
        else {
            try{
                GeneralManager.contentTable.updateFileStatus(userId, userFile, "[PENDING]");
            }
            catch (Exception exception){
                ProfiPrinter.PrintException("Replication : updatefilestatus1 : " + exception.getMessage());
            }
            // cautam un criteriu pe baza caruia selectam nodul de la care se face copierea
            String source = availableNodesAddressesForFile.get(0);
            System.out.println("\t\tFound source of replication : " + source);
            System.out.print("\t\tFound candidates for replication : ");
            for (String candidate : candidates) {
                System.out.print("[" + candidate + "] ");
            }
            System.out.println();
            GeneralManager.fileSystemManager.replicateFile(userId, userFile, source, candidates);
        }

    }

    public void deletion(int replication_factor, String userId, String userFile, List<String> candidates) throws Exception {
        LoggerService.registerWarning(GeneralManager.generalManagerIpAddress,String.format("[NEED DELETION OF FILE %s]", userFile));
        System.out.print("\t\tFound nodes to delete file : ");
        for (String candidate : candidates) {
            System.out.print("[" + candidate + "] ");
        }
        System.out.println();
        GeneralManager.fileSystemManager.deleteFile(userId, userFile, candidates);
        if(replication_factor == 0) {
            GeneralManager.contentTable.updateFileStatus(userId, userFile, "[DELETED]");
        }
    }
}
