package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import data.Pair;
import log.ProfiPrinter;
import node_manager.Beat.FileAttribute;

/** -------- Extra-descrieri -------- **/
/**
 * Clasa care contine atributele unui fisier
 */
class FileAttributes{
    /** -------- Atribute -------- **/
    /**
     * Numele fisierului
     */
    private String filename;
    /**
     * Factorul de replicare
     */
    private int replication_factor;
    /**
     * Statusul fisierului
     */
    private String status;
    /**
     * CRC
     */
    private long crc;

    /** -------- Constructor -------- **/
    public FileAttributes(String filename, int replication_factor, String status, long crc){
        this.filename = filename;
        this.replication_factor = replication_factor;
        this.status = status;
        this.crc = crc;
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
     * Getter pentru factorul de replicare
     */
    public int getReplication_factor() {
        return replication_factor;
    }
    /**
     * Setter pentru factorul de replicare
     */
    public void setReplication_factor(int replication_factor) {
        this.replication_factor = replication_factor;
    }

    /**
     * Getter pentru statusul fisierului
     */
    public String getStatus() {
        return status;
    }
    /**
     * Setter pentru statusul fisierului
     */
    public void setStatus(String status) {
        this.status = status;
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


    /** -------- Functii de baza, supraincarcate -------- **/
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(filename).append(" ");
        stringBuilder.append("[repl. : ").append(replication_factor).append("] ");
        stringBuilder.append("[CRC : ").append(Long.toHexString(crc)).append("] ");
        stringBuilder.append("[Status : ").append(status).append("]\n");
        return stringBuilder.toString();
    }
}


/**
 * Clasa care incapsuleaza tabela ce va contine toate fisierele care ar trebui sa se afle la nodul general,
 * impreuna cu utilizatorul caruia ii apartin si factorul de replicare.
 */
public class ContentTable {
    /** -------- Atribute -------- **/
    /**
     * Flag care sugereaza daca tabela necesita initializare;
     * Se va modifica imediat dupa initializare.
     */
    public boolean needInit = true;

    /**
     * Tabela de inregistrari; Utilizatorul unic si fisierele acestuia.
     */
    private final HashMap<String, List<FileAttributes>> contentTable;


    /** -------- Constructori & Initializare -------- **/
    /**
     * Constructorul clasei;
     * Creeaza o noua tabela de inregistrari.
     */
    public ContentTable(){
        this.contentTable = new HashMap<String, List<FileAttributes>>();
    }

    /**
     * Functia de initializare;
     * Adauga inregistrari in tabela, pe baza status-urilor stocarii primite de la toate nodurile interne
     * pe parcursului primului heartbeat;
     * Se va apela o singura data, la initializare;
     * @param storageStatusTable Tabela cu status-urile stocarii nodurilor interne
     */
    public void initialize(StorageStatusTable storageStatusTable){
        synchronized (this.contentTable) {
            for (String user : storageStatusTable.getUsers()) {
                HashMap<String, Integer> userFilesNodesCount = storageStatusTable.getUserFilesNodesCount(user);
                HashMap<String, List<Long>> userFilesCRC = storageStatusTable.getUserFilesCRC(user);
                for (String filename : new ArrayList<>(userFilesNodesCount.keySet())) {
                    try {
                        this.addRegister(user, filename, userFilesNodesCount.get(filename), userFilesCRC.get(filename).get(0), "[VALID]");
                    }
                    catch (Exception exception){
                        // nu prea avem cum sa ajunge aici la init intrucat exceptia se genereaza doar daca inregistrarea exista deja
                    }
                }
            }
        }
        needInit = false;
    }


    /** -------- Functii de prelucrare a tabelei -------- **/
    /**
     * Functie de adaugare a unei noi inregistrari in tabela.
     * Daca inregistrarea exista deja, se va modifica; Altfel, se va crea una noua.
     * @param userId Id-ul utilizatorului pentru care se doreste adaugarea inregistrarii;
     * @param filename Numele fisierului;
     * @param replication_factor Factorul de replicare;
     */
    public void addRegister(String userId, String filename, int replication_factor, long crc, String filestatus) throws Exception{
        synchronized (this.contentTable){
            FileAttributes fileAttribute = new FileAttributes(filename, replication_factor, filestatus, crc);
            if(this.containsUser(userId)){
                this.contentTable.get(userId).add(fileAttribute);
            }
            else{
                List<FileAttributes> fileAttributes = new ArrayList<>();
                fileAttributes.add(fileAttribute);
                this.contentTable.put(userId, fileAttributes);
            }
        }
    }

    /**
     * Functie de eliminare a unei inregistrari din tabela (un fisier sau chiar si userul daca nu mai are alte fisiere).
     * @param userId Id-ul utilizatorului pentru care se doreste adaugarea inregistrarii;
     * @param filename Numele fisierului;
     * @throws Exception Daca inregistrarea nu exista
     */
    public void deleteRegister(String userId, String filename) throws Exception{
        synchronized (this.contentTable){
            if(!this.containsUser(userId)){
                throw new Exception("Register not found!");
            }
            for(FileAttributes fileAttributes : this.contentTable.get(userId)){
                if(fileAttributes.getFilename().equals(filename)){
                    this.contentTable.get(userId).remove(fileAttributes);

                    if(this.contentTable.get(userId).size() == 0){
                        this.contentTable.remove(userId);
                    }
                    return;
                }
            }
            throw new Exception("Register not found!");
        }
    }

    /**
     * Functie de modificare a factorului de replicare al unui fisier.
     * @param filename Numele fisierului care se doreste a fi modificat.
     * @param replication_factor Noul factor de replicare.
     * @throws Exception Generata daca fisierul nu exista.
     */
    public void updateReplicationFactor(String userId, String filename, int replication_factor) throws Exception{
        synchronized (this.contentTable){
            if(!this.containsUser(userId)){
                throw new Exception("Register not found!");
            }
            for(FileAttributes fileAttributes : this.contentTable.get(userId)){
                if(fileAttributes.getFilename().equals(filename)){
                    fileAttributes.setReplication_factor(replication_factor);
                    return;

                }
            }
            throw new Exception("Register not found!");
        }
    }

    /**
     * Functie de modificare a numelui unui fisier.
     * @param filename Numele (vechi) fisierului.
     * @param newfilename Noul nume.
     * @throws Exception Fisierul nu exista.
     */
    public void updateFileName(String userId, String filename, String newfilename) throws Exception{
        synchronized (this.contentTable){
            if(!this.containsUser(userId)){
                throw new Exception("Register not found!");
            }
            for(FileAttributes fileAttributes : this.contentTable.get(userId)){
                if(fileAttributes.getFilename().equals(filename)){
                    fileAttributes.setFilename(newfilename);
                    return;

                }
            }
            throw new Exception("Register not found!");
        }
    }

    /**
     * Functie de modificare a statusului unui fisier.
     * @param filename Numele (vechi) fisierului.
     * @param filestatus Noul status.
     * @throws Exception Fisierul nu exista.
     */
    public void updateFileStatus(String userId, String filename, String filestatus) throws Exception{
        synchronized (this.contentTable){
            if(!this.containsUser(userId)){
                throw new Exception("Register not found!");
            }
            for(FileAttributes fileAttributes : this.contentTable.get(userId)){
                if(fileAttributes.getFilename().equals(filename)){
                    fileAttributes.setStatus(filestatus);
                    return;
                }
            }
            throw new Exception("Register not found!");
        }
    }

    /**
     * Functie de modificare a crc-ului unui fisier.
     * @param filename Numele (vechi) fisierului.
     * @param crc Noul crc.
     * @throws Exception Fisierul nu exista.
     */
    public void updateFileCRC(String userId, String filename, long crc) throws Exception{
        synchronized (this.contentTable){
            if(!this.containsUser(userId)){
                throw new Exception("Register not found!");
            }
            for(FileAttributes fileAttributes : this.contentTable.get(userId)){
                if(fileAttributes.getFilename().equals(filename)){
                    fileAttributes.setCrc(crc);
                    return;

                }
            }
            throw new Exception("Register not found!");
        }
    }




    /** -------- Functii de validare -------- **/
    /**
     * Functie care verifica daca un utilizator exista.
     * @param userId Id-ul utilizatorului cautat;
     */
    boolean containsUser(String userId){
        synchronized (this.contentTable){
            for(String user : this.getUsers()){
                if(user.equals(userId)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Functie care verifica daca un utilizator contine un anumit fisier.
     * @param userId Id-ul utilizatorului
     * @param filename Numele fisierului cautat.
     */
    public boolean checkForUserFile(String userId, String filename){
        try {
            List<FileAttributes> userFiles = getUserFiles(userId);
            if (userFiles == null)
                return false;
            for (FileAttributes userFile : userFiles) {
                if (userFile.getFilename().equals(filename)) {
                    //if(userFile.getReplication_factor() != 0)
                    return true;
                }
            }
        }
        catch (Exception exception){
            //return false; (e deja aruncat la final)
        }
        return false;
    }


    /** -------- Gettere -------- **/
    /**
     * Getter pentru lista id-urilor tuturor utilizatorilor.
     */
    public List<String> getUsers(){
        synchronized (this.contentTable) {
            return new ArrayList<>(this.contentTable.keySet());
        }
    }

    /**
     * Getter pentru lista fisierelor unui utilizator.
     * @param userId Id-ul utilizatorului ale carui fisiere sunt solicitate.
     */
    public List<FileAttributes> getUserFiles(String userId) throws Exception{
        synchronized (this.contentTable) {
            if(!this.containsUser(userId)){
                throw new Exception("Register not found!");
            }
            return this.contentTable.get(userId);
        }
    }

    public HashMap<String, Integer> getUserFiless(String userId) throws Exception{
        synchronized (this.contentTable) {
            if(!this.containsUser(userId)){
                throw new Exception("Register not found!");
            }
            HashMap<String, Integer> userFiles = new HashMap<>();
            for(FileAttributes fileAttributes : this.contentTable.get(userId)){
                userFiles.put(fileAttributes.getFilename(), fileAttributes.getReplication_factor());
            }
            return userFiles;
        }
    }

    public String getFileStatusForUser(String userId, String filename){
        try {
            for(FileAttributes file : this.getUserFiles(userId)){
                if(file.getFilename().equals(filename)){
                    return file.getStatus();
                }
            }
        }
        catch (Exception exception){
            ProfiPrinter.PrintException("User not found!");
        }
        return null;
    }

    public long getCRCForUser(String userId, String filename){
        try {
            for(FileAttributes file : this.getUserFiles(userId)){
                if(file.getFilename().equals(filename)){
                    return file.getCrc();
                }
            }
        }
        catch (Exception exception){
            ProfiPrinter.PrintException("User not found!");
        }
        return -1;
    }


    /** -------- Functii de baza, supraincarcate -------- **/
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("------------------------------------\n");
        stringBuilder.append("Content Table\n");
        synchronized (this.contentTable) {
            for(String userId : this.getUsers()){
                for(FileAttributes file : this.contentTable.get(userId)){
                    stringBuilder.append(file);
                }
            }
        }
        stringBuilder.append("------------------------------------\n");
        return stringBuilder.toString();
    }
}
