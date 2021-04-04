package model;

import communication.Address;
import data.Pair;
import node_manager.Beat.FileAttribute;
import node_manager.Beat.NodeBeat;

import java.util.*;

/**
 * Clasa care inglobeaza atributele fiecarui fisier
 */
class FileAttributesForStorage{
    /** -------- Atribute -------- **/
    /**
     * Numele fisierului
     */
    private String filename;
    /**
     * CRC-ul fisierului
     */
    private long crc;
    /**
     * Lista nodurilor pe care se afla fisierului
     */
    private List<String> nodes;


    /** -------- Constructor -------- **/
    public FileAttributesForStorage(){
        this.nodes = new ArrayList<>();
    }


    /** -------- Gettere & Settere -------- **/
    /**
     * Getter pentru numele fisierului
     */
    public String getFilename() {
        return filename;
    }
    /**
     * Setter pentru numele fisierului
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Getter pentru CRC
     */
    public long getCrc() {
        return crc;
    }
    /**
     * Setter pentru CRC
     */
    public void setCrc(long crc) {
        this.crc = crc;
    }

    /**
     * Getter pentru lista nodurilor
     */
    public List<String> getNodes() {
        return nodes;
    }
    /**
     * Getter pentru dimensiunea listei de noduri
     */
    public int getNodeListSize(){
        return this.nodes.size();
    }
    /**
     * Setter pentru lista nodurilor.
     */
    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }


    /** -------- Functii de prelucrare -------- **/
    /**
     * Functie de adaugare a unui nou nod in lista
     * @param nodeAddress Adresa nodului
     */
    public void addNode(String nodeAddress) throws Exception{
        if(!this.containsAddress(nodeAddress))
            this.nodes.add(nodeAddress);
        else
            throw new Exception("Node already exists!");
    }

    /**
     * Functie de eliminare a unui nou nod in lista
     * @param nodeAddress Adresa nodului
     */
    public void removeNode(String nodeAddress) throws NullPointerException{
        if(this.containsAddress(nodeAddress))
            this.nodes.remove(nodeAddress);
        else
            throw new NullPointerException("Node not found!");
    }


    /** -------- Functii de validare -------- **/
    /**
     * Functie care verifica daca un anumit nod se afla in lista
     * @param nodeAddress Adresa nodului
     */
    public boolean containsAddress(String nodeAddress){
        return this.nodes.contains(nodeAddress);
    }


    /** -------- Functii de baza, supraincarcate -------- **/
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\tFilename : ").append(filename).append("\n");
        stringBuilder.append("\t\tCRC : ").append(Long.toHexString(crc)).append("\n");
        for (String address : this.nodes) {
            stringBuilder.append("\t\t").append(address).append("\n");
        }
        return stringBuilder.toString();
    }
}

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
    public void updateTable(NodeBeat storageEntry) throws Exception {
        Address nodeAddress = Address.parseAddress(storageEntry.getNodeAddress());
        synchronized (this.statusTable){
            for(String user : storageEntry.getUsers()){
                List<FileAttribute> userFiles = storageEntry.getUserFilesById(user);

                if(!this.statusTable.containsKey(user)){
                    // daca utilizatorul nu exista, il adaugam.
                    this.statusTable.put(user, new ArrayList<FileAttributesForStorage>());
                }

                for(FileAttribute userFile : userFiles){
                    // daca fisierul utilizatorului curent nu exista, il adaugam
                    if(!this.checkFileForUser(user, userFile.getFilename())){
                        FileAttributesForStorage data = new FileAttributesForStorage();
                        data.setFilename(userFile.getFilename());
                        data.setCrc(userFile.getCrc());
                        try{
                            data.addNode(nodeAddress.getIpAddress());
                        }
                        catch (Exception exception){
                            System.out.println("Node already contains address!");
                        }
                        this.statusTable.get(user).add(data);
                    }
                    else{
                        if(!checkAddress(user, userFile.getFilename(), nodeAddress.getIpAddress())){
                            // daca fisierul utilizatorului curent exista, dar nu contine adresa nodului,
                            // adaugam adresa nodului
                            int candidate = this.getUserFile(user, userFile.getFilename());
                            if(candidate != -1){
                                this.statusTable.get(user).get(candidate).addNode(nodeAddress.getIpAddress());
                            }
                        }
                    }
                }

                // verificam daca sunt useri stersi complet de la un nod; in acest caz;
                // eliminam adresa nodului de la care a fost sters, sau intregul user
                cleanUpOnDeletedUser(nodeAddress.getIpAddress(), storageEntry.getUsers());

                // verificam daca sunt fisiere care au fost sterse, si le eliminam;
                // eliminam adresa nodului de la care a fost sters, sau fisierul daca nu se afla pe niciun nod
                List<String> files = new ArrayList<>();
                for(FileAttribute fileAttribute : storageEntry.getUserFilesById(user)){
                    files.add(fileAttribute.getFilename());
                }
                List<String> deletedFiles = getDeletedFiles(user, nodeAddress.getIpAddress(), files);
                if(deletedFiles.size() > 0){
                    int x = 0;
                }
                for(String deletedFile : deletedFiles){
                    int index = getUserFile(user, deletedFile);
                    try {
                        this.statusTable.get(user).get(index).removeNode(nodeAddress.getIpAddress());
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
     * Functie care elimina toate inregistrarile unui nod care tocmai s-a deconectat;
     * Se elimina fisierele nodului deconectat; Daca pentru un user/fisier nu mai exista noduri
     * care sa stocheze fisierul, atunci userul/fisierul se vor elimina din tabela;
     * @param address Adresa nodului deconectat
     */
    public void cleanUpAtNodeDisconnection(String address){
        synchronized (this.statusTable){
            List<String> users = new ArrayList<>(this.statusTable.keySet());
            for(String user : users){
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
                if(candidate.getFilename().equals(file) && candidate.getNodes().contains(address)){
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
            List<FileAttributesForStorage> userFiles = this.statusTable.get(user);
            for(FileAttributesForStorage userFile : userFiles) {
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
    public List<String> getDeletedFiles(String user, String userAddress, List<String> userFiles){
        List<String> deletedFiles = new ArrayList<>();
        synchronized (this.statusTable){
            for(FileAttributesForStorage availableFile : this.statusTable.get(user)){
                if(!userFiles.contains(availableFile.getFilename()) && availableFile.containsAddress(userAddress)){
                    deletedFiles.add(availableFile.getFilename());
                }
            }
        }
        return deletedFiles;
    }

    /**
     * Functie care returneaza lista adreselor nodurilor care stocheaza un anumit fisier.
     * @param user Id-ul utilizatorului.
     * @param file Numele fisierului.
     * @return Lista de noduri.
     */
    public List<String> getAvailableNodesForFile(String user, String file){
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
    public long getCRCForFile(String user, String file){
        synchronized (this.statusTable){
            try {
                int candidate = this.getUserFile(user, file);
                if(candidate == -1)
                    return -1;
                return this.statusTable.get(user).get(candidate).getCrc();
            }
            catch (NullPointerException exception){
                return -1;
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
    public HashMap<String, Integer> getUserFilesNodesCount(String userId){
        synchronized (this.statusTable) {
            if(!this.checkUser(userId))
                return null;
            HashMap<String, Integer> filesNodesCounts = new HashMap<>();
            for (FileAttributesForStorage userfile : this.statusTable.get(userId)) {
                filesNodesCounts.put(userfile.getFilename(), userfile.getNodes().size());
            }
            return filesNodesCounts;
        }
    }

    /**
     * Functie care returneaza crc-ul fiecarui fisier al unui anumit user.
     * @param userId Id-ul utilizatorului.
     */
    public HashMap<String, Long> getUserFilesCRC(String userId){
        synchronized (this.statusTable) {
            if(!this.checkUser(userId))
                return null;
            HashMap<String, Long> filesNodesCounts = new HashMap<>();
            for (FileAttributesForStorage userfile : this.statusTable.get(userId)) {
                filesNodesCounts.put(userfile.getFilename(), userfile.getCrc());
            }
            return filesNodesCounts;
        }
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
