package tables;

import communication.Address;
import data.Pair;
import log.ProfiPrinter;
import model.FileAttributes;
import model.FileAttributesForStorage;
import model.FileVersionData;
import node_manager.Beat.FileAttribute;
import node_manager.Beat.NodeBeat;
import tables.ContentTable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Clasa care inglobeaza status-urile stocarii tuturor nodurilor interne conectate la nodul general.
 * Va contine lista de utilizatori, impreuna cu fisierele fiecarui utilizator si nodurile la care sunt stocate
 */
public class StorageStatusTable {
    /** -------- Atribute -------- **/
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
     * Constructorul clasei;
     * Initializeaza tabela de inregistrari
     */
    public StorageStatusTable(){
        this.statusTable = new HashMap<String, List<FileAttributesForStorage>>();
    }


    /** -------- Functii de prelucrare a tabelei -------- **/
    /**
     * Functie generala care modifica tabela, la sosirea unui nou heartbeat de la nodul intern;
     * Se includ operatii de adaugare a unui fisier, de eliminare a unui fisier, de curatare.
     * @param storageEntry Heartbeat de la nodul intern; Va contine adresa nodului, impreuna
     *                     cu toti utilizatorii existenti si fisierele acestora
     */
    public void updateTable(NodeBeat storageEntry, ContentTable contentTable) throws Exception {
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
                    String versionNo = userFile.getVersionNo();
                    if(!this.checkFileForUser(user, filename)){
                        FileAttributesForStorage data = new FileAttributesForStorage();
                        data.setFilename(filename);
                        try{
                            data.addNode(nodeAddress, crc, versionNo);
                        }
                        catch (Exception exception){
                            ProfiPrinter.PrintException("Node already contains address!");
                        }
                        this.statusTable.get(user).add(data);
                    }
                    else{
                        if(!checkAddress(user, filename, nodeAddress)){
                            // daca fisierul utilizatorului curent exista, dar nu contine adresa nodului,
                            // adaugam adresa nodului
                            int candidate = this.getUserFile(user, filename);
                            if(candidate != -1){
                                this.statusTable.get(user).get(candidate).addNode(nodeAddress, crc, versionNo);
                            }
                        }
                        else{
                            int candidate = this.getUserFile(user, filename);
                            try {
                                this.statusTable.get(user).get(candidate).setVersionNo(nodeAddress, versionNo);
                                long statusTableCRC = this.statusTable.get(user).get(candidate).getCrc(nodeAddress);
                                String statusTableVersioNo = this.statusTable.get(user).get(candidate).getVersionNo(nodeAddress);
                                if (crc != -1 && statusTableCRC != crc)
                                    this.statusTable.get(user).get(candidate).setCrc(nodeAddress, crc);
                                if(!statusTableVersioNo.equals(versionNo))
                                    this.statusTable.get(user).get(candidate).setVersionNo(nodeAddress, versionNo);
                            }
                            catch (NullPointerException exception){
                                ProfiPrinter.PrintException("File " + this.statusTable.get(user).get(candidate).getFilename() + " of user " + nodeAddress + " skipped! : " + exception.getMessage());
                            }
                        }
                    }
                }

                // verificam daca sunt useri stersi complet de la un nod; in acest caz;
                // eliminam adresa nodului de la care a fost sters, sau intregul user
                /* TODO fix this sh*t */
                //cleanUpOnDeletedUser(nodeAddress.getIpAddress(), storageEntry.getUsers());

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
                        ProfiPrinter.PrintException(exception.getMessage());
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
     * Functie care elimina toate inregistrarile unui nod care tocmai s-a deconectat;
     * Se elimina fisierele nodului deconectat; Daca pentru un user/fisier nu mai exista noduri
     * care sa stocheze fisierul, atunci userul/fisierul se vor elimina din tabela;
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
     * Functie de stergere a inregistrarilor, la eliminarea unui user de la un nod;
     * !! are probleme !!
     */
    public void cleanUpOnDeletedUser(String userAddress, List<String> users){
        boolean found;
        synchronized (this.statusTable){
            for(String availableUser : this.getUsers()){
                found = false;
                for(String existingUser : users){
                    if(availableUser.equals(existingUser)){
                        found = true;
                        break;
                    }
                }
                if(found){
                    continue;
                }
                for(FileAttributesForStorage file : this.statusTable.get(availableUser)){
                    file.removeNode(userAddress);
                    if(file.getNodeListSize() == 0){
                        this.statusTable.get(availableUser).remove(file);
                    }
                }
                if(this.statusTable.get(availableUser).size() == 0){
                    this.statusTable.remove(availableUser);
                }
            }
        }
    }


    /** -------- Functii de valiare -------- **/
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


    /** -------- Gettere -------- **/
    /**
     * Functie care verifica si returneaza (daca) anumite fisiere au fost eliminate de la anumite noduri;
     * Functia se foloseste atunci ca dorim sa verificam daca, in heartbeat-ul curent, anumite fisiere nu se mai
     * gasesc in stocarea nodului si trebuie eliminate.
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

    public List<String> getUserFiles(String user) {
        synchronized (this.statusTable) {
            return this.statusTable.get(user)
                    .stream()
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
                return this.statusTable.get(user).get(candidate).getNodesCRCs().get(0);
            }
            catch (NullPointerException exception){
                return null;
            }
        }
    }

    public String getLastVersionOfFile(String user, String file){
        synchronized (this.statusTable){
            try {
                int candidate = this.getUserFile(user, file);
                if(candidate == -1)
                    return null;
                return this.statusTable.get(user).get(candidate).getNodesVersions().get(0);
            }
            catch (NullPointerException exception){
                return null;
            }
        }
    }

    /**
     * Functie care returneaza o referinta la un fisier
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
     * Functie care returneaza crc-ul fiecarui fisier al unui anumit user.
     * @param userId Id-ul utilizatorului.
     */
    public HashMap<String, List<Long>> getUserFilesCRC(String userId){
        synchronized (this.statusTable) {
            if(!this.checkUser(userId))
                return null;
            return new HashMap<String, List<Long>>(){{
                for (FileAttributesForStorage userfile : statusTable.get(userId)) {
                    put(userfile.getFilename(), userfile.getNodesCRCs());
                }
            }};
        }
    }

    public HashMap<String, List<String>> getUserFilesVersions(String userId){
        synchronized (this.statusTable) {
            if (!this.checkUser(userId))
                return null;
            return new HashMap<String, List<String>>() {{
                for (FileAttributesForStorage userfile : statusTable.get(userId)) {
                    put(userfile.getFilename(), userfile.getNodesVersions());
                }
            }};
        }
    }

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


    /** -------- Functii de baza, supraincarcate -------- **/
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
