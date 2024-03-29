package tables;

import communication.Address;
import communication.Serializer;
import config.AppConfig;
import data.Pair;
import logger.LoggerService;
import model.FileAttributesForStorage;
import model.FileVersionData;
import node_manager.Beat.FileAttribute;
import node_manager.Beat.NodeBeat;
import os.FileSystem;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <ul>
 * 	<li>Clasa care inglobeaza status-urile stocarii tuturor nodurilor interne conectate la nodul general.</li>
 * 	<li>Va contine lista de utilizatori, impreuna cu fisierele acestara si nodurile pe care sunt stocate.</li>
 * </ul>
 */
public class StorageStatusTable {
    /**
     * Adresa nodului general.
     */
    private static String generalManagerIpAddress = AppConfig.getParam("generalManagerIpAddress");

    /**
     * Tabela de inregistrari;
     * Cheie : Id-ul utilizatorului
     * Valoare : {
     *     Cheie : Nume fisier
     *     Valoare : Vector de adrese ale nodurilor la care se gaseste fisierul
     * }
     */
    private final HashMap<String, List<FileAttributesForStorage>> statusTable;

    /** -------- Constructori -------- **/
    /**
     * <ul>
     * 	<li>Constructorul clasei.</li>
     * 	<li>Initializeaza tabela de inregistrari.</li>
     * </ul>
     */
    public StorageStatusTable(){
        this.statusTable = new HashMap<String, List<FileAttributesForStorage>>();
    }


    /**
     * <ul>
     * 	<li>Functie generala care modifica tabela, la sosirea unui nou heartbeat de la nodul intern.</li>
     * 	<li>Se includ operatii de adaugare a unui fisier, de eliminare a unui fisier, de curatare.</li>
     * </ul>
     * @param storageEntry Heartbeat de la nodul intern; Va contine adresa nodului, impreuna
     *                     cu toti utilizatorii existenti si fisierele acestora
     */
    public void updateTable(NodeBeat storageEntry) throws Exception {
        String nodeAddress = Address.parseAddress(storageEntry.getNodeAddress()).getIpAddress();
        synchronized (this.statusTable){
            for(String user : storageEntry.getUsers()){
                if(!this.statusTable.containsKey(user)){
                    // daca utilizatorul nu exista, il adaugam.
                    this.statusTable.put(user, new ArrayList<FileAttributesForStorage>());
                }

                for(FileAttribute userFile : storageEntry.getUserFilesById(user)){
                    // daca fisierul utilizatorului curent nu exista, il adaugam
                    String filename = userFile.getFilename();
                    long crc = userFile.getCrc();
                    long filesize = userFile.getFilesize();
                    String versionNo = userFile.getVersionNo();
                    String versionDescription = userFile.getDescription();
                    if(!this.checkFileForUser(user, filename)){
                        FileAttributesForStorage data = new FileAttributesForStorage();
                        data.setFilename(filename);
                        try{
                            data.addNode(nodeAddress, crc, versionNo, filesize, versionDescription);
                        }
                        catch (Exception exception){
                            System.out.println("Node already contains address!");
                        }
                        this.statusTable.get(user).add(data);
                    }
                    else{
                        if(!checkAddress(user, filename, nodeAddress)){
                            // daca fisierul utilizatorului curent exista, dar nu contine adresa nodului,
                            // adaugam adresa nodului
                            int candidate = this.getUserFile(user, filename);
                            if(candidate != -1){
                                this.statusTable.get(user).get(candidate).addNode(nodeAddress, crc, versionNo,filesize, versionDescription);
                            }
                        }
                        else{
                            int candidate = this.getUserFile(user, filename);
                            try {
                                this.statusTable.get(user).get(candidate).setVersionNo(nodeAddress, versionNo);
                                long statusTableCRC = this.statusTable.get(user).get(candidate).getCrc(nodeAddress);
                                String statusTableVersioNo = this.statusTable.get(user).get(candidate).getVersionNo(nodeAddress);
                                String statusTabelDescription = this.statusTable.get(user).get(candidate).getVersionDescription(nodeAddress);
                                long statusTableFilesize = this.statusTable.get(user).get(candidate).getFileSize(nodeAddress);
                                if (crc != -1 && statusTableCRC != crc)
                                    this.statusTable.get(user).get(candidate).setCrc(nodeAddress, crc);
                                if (statusTableFilesize != filesize)
                                    this.statusTable.get(user).get(candidate).setFileSize(nodeAddress, filesize);
                                if(!statusTableVersioNo.equals(versionNo))
                                    this.statusTable.get(user).get(candidate).setVersionNo(nodeAddress, versionNo);
                                if(!statusTabelDescription.equals(versionDescription))
                                    this.statusTable.get(user).get(candidate).setVersionDescription(nodeAddress, versionDescription);
                            }
                            catch (NullPointerException exception){
                                LoggerService.registerWarning(generalManagerIpAddress, "File " + this.statusTable.get(user).get(candidate).getFilename() + " of user " + nodeAddress + " skipped! : " + exception.getMessage());
                            }
                        }
                    }
                }

                // verificam daca sunt fisiere care au fost sterse, si le eliminam;
                // eliminam adresa nodului de la care a fost sters, sau fisierul daca nu se afla pe niciun nod
                // verificam si daca avem fisiere care se gasesc in stocare, dar nu se gasesc tabela de content.
                List<String> files = new ArrayList<String>(){{
                    for(FileAttribute fileAttribute : storageEntry.getUserFilesById(user)){
                        add(fileAttribute.getFilename());
                    }
                }};

                for(String deletedFile : getDeletedFilesFromNodeStorage(user, nodeAddress, files)){
                    int index = getUserFile(user, deletedFile);
                    try {
                        this.statusTable.get(user).get(index).removeNode(nodeAddress);
                        if (this.statusTable.get(user).get(index).getNodeListSize() == 0)
                            this.statusTable.get(user).remove(index);
                    }
                    catch (NullPointerException exception){
                        System.out.println(exception.getMessage());
                    }
                }

                // verificam daca exista utilizatori fara fisiere; ii eliminam
                if(this.statusTable.get(user).size() == 0){
                    this.statusTable.remove(user);
                }
            }
        }
    }

    /**
     * <ul>
     * 	<li>Functie care elimina toate inregistrarile unui nod care tocmai s-a deconectat.</li>
     * 	<li>Se elimina fisierele nodului deconectat.</li>
     * 	<li>Daca pentru un user/fisier nu mai exista noduricare sa stocheze fisierul, atunci userul/fisierul se vor elimina din tabela.</li>
     * </ul>
     * @param address Adresa nodului deconectat
     */
    public void cleanUpAtNodeDisconnection(String address){
        synchronized (this.statusTable){
            for(String user : new ArrayList<>(this.statusTable.keySet())){
                for(FileAttributesForStorage file : new ArrayList<>(this.statusTable.get(user))){
                    if(checkAddress(user, file.getFilename(), address)){
                        file.removeNode(address);
                    }
                    if(file.getNodeListSize() == 0){
                        this.statusTable.get(user).remove(file);
                    }
                }
                if(this.statusTable.get(user).size() == 0){
                    this.statusTable.remove(user);
                }
            }
        }
    }

    /**
     * Functie care verifica daca tabela contine un anumit utilizator
     */
    public boolean checkUser(String user){
        return new ArrayList<>(this.statusTable.keySet()).contains(user);
    }

    /**
     * Functie care verifica daca, pentru un anumit fisier al unui anumit utilizator, mai exista adresa unui nod.
     * @param user Id-ul utilizatorului.
     * @param file Numele fisierului.
     * @param address Adresa cautata.
     */
    public boolean checkAddress(String user, String file, String address){
        synchronized (this.statusTable){
            for(FileAttributesForStorage candidate : this.statusTable.get(user)){
                if(candidate.getFilename().equals(file) && candidate.getNodesAddresses().contains(address)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Functie care verifica daca un anumit utilizator detine un anumit fisier.
     * @param user Id-ul utilizatorului.
     * @param file Numele fisierului.
     */
    public boolean checkFileForUser(String user, String file){
        synchronized (this.statusTable){
            if(!checkUser(user))
                return false;
            for(FileAttributesForStorage userFile : this.statusTable.get(user)) {
                if(userFile.getFilename().equals(file))
                    return true;
            }
        }
        return false;
    }

    /**
     * <ul>
     * 	<li>Functie care verifica si returneaza <strong>daca</strong> anumite fisiere au fost eliminate de la anumite noduri.</li>
     * 	<li>Functia se foloseste atunci ca dorim sa verificam daca, in heartbeat-ul curent, anumite fisiere nu se mai
     *      gasesc in stocarea nodului si trebuie eliminate.</li>
     * </ul>
     * @param user Identificatorul utilizatorului.
     * @param userAddress Adresa nodului intern.
     * @param userFiles Fisierele existente in stocarea nodului curent; Daca gasim fisiere care nu se mai afla in stocare,
     *                  dar se afla in tabela, acestea se doresc a fi eliminate.
     * @return Fisierele eliminate.
     */
    public List<String> getDeletedFilesFromNodeStorage(String user, String userAddress, List<String> userFiles){
        synchronized (this.statusTable){
            return this.statusTable.get(user)
                    .stream()
                    .filter(availableFile -> !userFiles.contains(availableFile.getFilename()) && availableFile.containsAddress(userAddress))
                    .map(FileAttributesForStorage::getFilename)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Functie care returneaza lista adreselor nodurilor care stocheaza un anumit fisier.
     * @param user Id-ul utilizatorului.
     * @param file Numele fisierului.
     * @return Lista de noduri.
     */
    public List<String> getAvailableNodesAddressesForFile(String user, String file){
        synchronized (this.statusTable){
            try {
                int candidate = this.getUserFile(user, file);
                if(candidate == -1)
                    return null;
                return this.statusTable.get(user).get(candidate).getNodesAddresses();
            }
            catch (NullPointerException exception){
                return null;
            }
        }
    }

    /**
     * Functie care returneaza lista adreselor nodurilor care stocheaza un anumit fisier.
     * @param user Id-ul utilizatorului.
     * @param file Numele fisierului.
     * @return Lista de noduri.
     */
    public List<Pair<String, FileVersionData>> getAvailableNodesForFile(String user, String file){
        synchronized (this.statusTable){
            try {
                int candidate = this.getUserFile(user, file);
                if(candidate == -1)
                    return null;
                return this.statusTable.get(user).get(candidate).getNodes();
            }
            catch (NullPointerException exception){
                return null;
            }
        }
    }

    /**
     * Functie care returneaza CRC-ul unui anumit fisier.
     * @param user Id-ul utilizatorului.
     * @param file Numele fisierului.
     * @return CRC-ul fisierului.
     */
    public Long getCRCsForFile(String user, String file){
        synchronized (this.statusTable){
            try {
                int candidate = this.getUserFile(user, file);
                if(candidate == -1)
                    return null;
                List<Long> controlSums = this.statusTable.get(user).get(candidate).getNodesCRCs();
                return (Long)this.getMostVoted(controlSums.stream().map(node -> (Object)node).collect(Collectors.toList()));
            }
            catch (NullPointerException exception){
                return null;
            }
        }
    }

    /**
     * Functie care returneaza dimensiunea unui anumit fisier
     * @param user Id-ul utilizatorului.
     * @param file Numele fisierului.
     * @param address Adresa nodului care contine fisierul a carui dimensiune se cauta.
     * @return CRC-ul fisierului.
     */
    public Long getFileSize(String user, String file, String address){
        synchronized (this.statusTable){
            try {
                int candidate = this.getUserFile(user, file);
                if(candidate == -1)
                    return null;
                return this.statusTable.get(user).get(candidate).getFileSize(address);
            }
            catch (NullPointerException exception){
                return null;
            }
        }
    }

    /**
     * Functie care returneaza numarul ultimei versiuni a unui fisier.
     * @param user Id-ul utilizatorului.
     * @param file Numele fisierului.
     * @return CRC-ul fisierului.
     */
    public String getLastVersionOfFile(String user, String file){
        synchronized (this.statusTable){
            try {
                int candidate = this.getUserFile(user, file);
                if(candidate == -1)
                    return null;
                List<String> versionNumbers = this.statusTable.get(user).get(candidate).getNodesVersions();
                return (String)this.getMostVoted(versionNumbers.stream().map(node -> (Object)node).collect(Collectors.toList()));
            }
            catch (NullPointerException exception){
                return null;
            }
        }
    }

    /**
     * Functie care returneaza descrierea ultimei versiuni a unui fisier.
     * @param user Id-ul utilizatorului.
     * @param file Numele fisierului.
     * @return CRC-ul fisierului.
     */
    public String getLastVersionDescriptionOfFile(String user, String file){
        synchronized (this.statusTable){
            try {
                int candidate = this.getUserFile(user, file);
                if(candidate == -1)
                    return null;
                List<String> versionDescriptions = this.statusTable.get(user).get(candidate).getNodesVersionsDescriptions();
                return (String)this.getMostVoted(versionDescriptions.stream().map(node -> (Object)node).collect(Collectors.toList()));
            }
            catch (NullPointerException exception){
                return null;
            }
        }
    }


    /**
     * Functie care returneaza o referinta la un fisier al unui utilizator.
     */
    public int getUserFile(String userId, String filename) {
        if (!checkUser(userId))
            return -1;

        for (FileAttributesForStorage userfile : this.statusTable.get(userId)) {
            if (userfile.getFilename().equals(filename))
                return this.statusTable.get(userId).indexOf(userfile);
        }
        return -1;
    }

    /**
     * Functie care returneaza lista de utilizatori.
     */
    public List<String> getUsers(){
        synchronized (this.statusTable) {
            return new ArrayList<>(this.statusTable.keySet());
        }
    }

    /**
     * Functie care returneaza numarul de noduri interne care stocheaza fiecare fisiers al unui anumit user.
     * @param userId Id-ul utilizatorului.
     */
    public HashMap<String, Integer> getUserFilesNodesCountForFile(String userId){
        synchronized (this.statusTable) {
            if (!this.checkUser(userId))
                return null;
            return new HashMap<String, Integer>(){{
                for (FileAttributesForStorage userfile : statusTable.get(userId)) {
                    put(userfile.getFilename(), userfile.getNodes().size());
                }
            }};
        }
    }

    /**
     * <ul>
     * 	<li>Functie care returenaza adresa unuia dintre nodurile care stocheaza un anumit fisier.</li>
     * 	<li> Adresa va fi necesara in procesul de descarcare a unui fisier sau de extragere a versiunilor unui fisier</li>
     * </ul>
     * @param userId Identificatorul unic al utilizatorului
     * @param filename Numele fisierului
     * @param crc Suma de control a fisierului
     * @return Adresa unui nod care contine o replica a fisierului cautat.
     * @throws Exception Nu s-a gasit niciun nod care stocheaza o replica valida a fisierului
     */
    public String getCandidateAddress(String userId, String filename, long crc) throws Exception{
        int userFileId = this.getUserFile(userId, filename);
        FileAttributesForStorage fileAttributes = this.statusTable.get(userId).get(userFileId);
        for(Pair<String, FileVersionData> node : fileAttributes.getNodes()){
            if(node.getSecond().getCrc() == crc || crc == -1){
                return node.getFirst();
            }
        }
        throw new Exception("No valid node found!");
    }

    /**
     * <ul>
     * 	<li>Functie care returneaza tabela de status, astfel incat sa fie trimisa catre client pentru monitorizare.</li>
     * </ul>
     */
    public List<FileAttributesForStorage> getStorageStatusTable() throws Exception {
        List<FileAttributesForStorage> results =  new ArrayList<>();
        for(String userId : new ArrayList<>(this.statusTable.keySet())){
            for(FileAttributesForStorage fileAttributesForStorage : this.statusTable.get(userId)){
                fileAttributesForStorage.setUserId(userId);
                results.add(fileAttributesForStorage);
            }
        }
        return results;
    }

    /**
     * <ul>
     *  <li>Functie folosita, in special, in contextul operatiei de reconstruire a starii tabelei de continut.</li>
     *  <li>Dintr-o lista de valori posibile ale unui atribut al unui fisier, se alege atributul continut de cele mai multe fisiere.</li>
     * </ul>
     * @return Valoarea cae mai votata a atributului.
     */
    private Object getMostVoted(List<Object> candidates){
        List<Object> candidateSet = new ArrayList<>();
        int[] scores = new int[candidates.size()];
        for(Object candidate : candidates){
            if(!candidateSet.contains(candidate)){
                candidateSet.add(candidate);
                scores[candidateSet.size() - 1] = 1;
            }
            else{
                scores[candidateSet.indexOf(candidate)] += 1;
            }
        }
        int max_idx = 0;
        int max = scores[0];
        for(int index = 1; index < scores.length; index++){
            if(scores[index] > max){
                max = scores[index];
                max_idx = index;
            }
        }
        return candidateSet.get(max_idx);
    }

    public String getSize(){
        try {
            long size = Serializer.getObjectSize(statusTable);
            Pair<Double, String> sz = FileSystem.convertToBestScale(size, 0);
            return sz.getFirst() + " " + sz.getSecond();
        }
        catch (Exception exception){
            System.out.println("Nu pot calcula dimensiunea!");
            return "";
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("------------------------------------\n");
        stringBuilder.append("Storage Status Table\n");
        synchronized (this.statusTable) {
            for (String user : this.statusTable.keySet()) {
                stringBuilder.append("User id  : ").append(user).append("\n");
                for (FileAttributesForStorage file : this.statusTable.get(user)) {
                    stringBuilder.append(file);
                }
                stringBuilder.append("\n");
            }
        }
        stringBuilder.append("------------------------------------\n");
        return stringBuilder.toString();
    }
}
