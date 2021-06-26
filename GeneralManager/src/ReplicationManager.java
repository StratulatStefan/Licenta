import config.AppConfig;
import data.Pair;
import logger.LoggerService;
import model.FileVersionData;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <ul>
 * 	<li>Clasa va gestiona procesul de asigurare a disponibilitatii sistemului, prin intermediul mecanismului de replicare.</li>
 * 	<li>La intervale regulate de timp, se va verifica daca datele despre fisiere din <strong>tabela de continut</strong>
 *      coincid cu datele despre fisiere din <strong>tabela de status a stocarii</strong>, alcatuita pe baza heartbeat-urilor.</li>
 * </ul>
 */
public class ReplicationManager implements Runnable{
    /**
     * Frecventa de executare a buclei de replicare
     */
    private static int replicationFrequency = Integer.parseInt(AppConfig.getParam("replicationFrequency"));
    /**
     * Lista care va contine mesajele generate de mecanismul de replicare pentru fiecare fisier.
     */
    private static List<String> replicationStatusTable = new ArrayList<>();

    /**
     * Constructorul clasei;
     */
    public ReplicationManager(){

    }

    /**
     * <ul>
     * 	<li>Functie care parcurge tabela de status a nodurilor conectate si identifica nodurile care ar putea stocare un anumit fisier.</li>
     * 	<li> trebuie sa nu contina fisierul respectiv si sa aiba spatiu.</li>
     * </ul>
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
     * <ul>
     * 	<li>Functie care returneaza nodurile care contin un anumit fisier .</li>
     * 	<li> Trebuie sa se elimine fisierul de la aceste noduri.</li>
     * </ul>
     * @param count Numarul de noduri care trebuie sa elimine fisierul.
     * @param availableNodes Lista tuturor nodurilor disponibile care contin fisierul.
     */
    private List<String> searchCandidatesForDeletion(int count, List<String> availableNodes){
        return availableNodes.subList(0, count);
    }

    /**
     * <ul>
     * 	<li>Getter pentru lista mesajelor de replicare.</li>
     * </ul>
     */
    public static List<String> getReplicationStatusTable(){
        return replicationStatusTable;
    }

    /**
     * <ul>
     * 	<li>Functie care verifica daca exista coruperi la nivelul fisierelor.</li>
     * 	<li> Se verifica <strong>numarul de versiuni</strong> si <strong>suma de control</strong>.</li>
     * 	<li> Nu se includ fisierele cu suma de control <strong>-1</strong>.</li>
     * </ul>
     * @param crc Suma de control a fisierului.
     * @param versionNo Numarul versiunii
     * @param availableNodesForFile Nodurile care stocheaza o replica a fisierului.
     */
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

    /**
     * <ul>
     * 	<li>Functie care executa procesul de replicare a unui fisier.</li>
     * 	<li> Se cauta un candidat pentru replicare.</li>
     * 	<li> Se trimite o cerere catre acest candidat, de trimitere a fisierului catre nodul destinatie.</li>
     * 	<li> Se pune fisierul in starea <strong>PENDING</strong> pentru a fi ignorat de procesul de replicare, pana se primeste confirmare de stocare cu succes.</li>
     * </ul>
     * @param replication_factor Factorul de replicare
     * @param userId Identificatorul unic al fisierului.
     * @param userFile Numele fisierului
     * @param availableNodesAddressesForFile Lista de adrese a nodurilor care detin fisierul.
     * @param filesize Dimensiunea fisierului
     * @return Statusul procesului de inregistrare.
     */
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

    /**
     * <ul>
     * 	<li>Functia de solicitare a eliminarii tuturor replicilor unui fisier de pe nodurile specificate.</li>
     * </ul>
     * @param replication_factor Factorul de replicare al fisierului.
     * @param userId Identificatorul unic al fisierului
     * @param userFile Numele fisierului
     * @param candidates Fisierele care contin o replica a fisierului.
     */
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

    /**
     * <ul>
     * 	<li>Functia care executa tot procesul de verificare si incercare de identificare a inconsistentelor.</li>
     * 	<li> Se compara cele doua tabele <strong>de status</strong> si <strong>de content</strong>.</li>
     * 	<li> Se ignora fisierele cu suma de control = -1.</li>
     * 	<li> Se ignora fisierele in starea <strong>PENDING</strong>.</li>
     * </ul>
     */
    @Override
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
}
