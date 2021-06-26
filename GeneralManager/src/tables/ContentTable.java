package tables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import config.AppConfig;
import logger.LoggerService;
import model.FileAttributes;

/**
 * <ul>
 * 	<li>Clasa care incapsuleaza tabela ce va contine toate fisierele care ar trebui sa se afle la nodul general,impreuna cu utilizatorul caruia ii apartin si factorul de replicare.</li>
 * </ul>
 */
public class ContentTable implements Serializable {
    /**
     * Adresa nodului general.
     */
    private static String generalManagerIpAddress = AppConfig.getParam("generalManagerIpAddress");

    /**
     * <ul>
     * 	<li>Flag care sugereaza daca tabela necesita initializare.</li>
     * 	<li>Se va modifica imediat dupa initializare.</li>
     * </ul>
     */
    public boolean needInit = true;

    /**
     * <ul>
     * 	<li>Tabela de inregistrari.</li>
     * 	<li> Utilizatorul unic si fisierele acestuia.</li>
     * </ul>
     */
    private final HashMap<String, List<FileAttributes>> contentTable;

    /**
     * <ul>
     * 	<li>Constructorul clasei.</li>
     * 	<li>Creeaza o noua tabela de inregistrari.</li>
     * </ul>
     */
    public ContentTable(){
        this.contentTable = new HashMap<String, List<FileAttributes>>();
    }

    /**
     * <ul>
     * 	<li>Functia de initializare.</li>
     * 	<li>Adauga inregistrari in tabela, pe baza status-urilor stocarii primite de la toate nodurile interne
     * 	    pe parcursului primului heartbeat.</li>
     * 	<li>Se va apela la intervale regulate de timp, pana se va efectua cu succes initializarea</li>
     * </ul>
     * @param storageStatusTable Tabela cu status-urile stocarii nodurilor interne
     */
    public void initialize(StorageStatusTable storageStatusTable){
        synchronized (this.contentTable) {
            this.contentTable.clear();
            long crc;
            String versionNo;
            int nodesCount;
            long filesize;
            needInit = false;
            for (String user : storageStatusTable.getUsers()) {
                HashMap<String, Integer> userFilesNodesCount = storageStatusTable.getUserFilesNodesCountForFile(user);
                for (String filename : new ArrayList<>(userFilesNodesCount.keySet())) {
                    try {
                        crc = storageStatusTable.getCRCsForFile(user, filename);
                        if(crc == -1){
                            needInit = true;
                        }
                        versionNo = storageStatusTable.getLastVersionOfFile(user, filename);
                        nodesCount = userFilesNodesCount.get(filename);
                        filesize = storageStatusTable.getFileSize(user, filename, storageStatusTable.getAvailableNodesForFile(user, filename).get(0).getFirst());
                        this.addRegister(user, filename, nodesCount, crc, "[VALID]", filesize, versionNo, "");
                    }
                    catch (Exception exception){
                        // nu prea avem cum sa ajunge aici la init intrucat exceptia se genereaza doar daca inregistrarea exista deja
                    }
                }
            }
        }
    }

    /**
     * <ul>
     * 	<li>Functie de adaugare a unei noi inregistrari in tabela.</li>
     * 	<li>Daca inregistrarea exista deja, se va modifica.</li>
     * 	<li> Altfel, se va crea una noua.</li>
     * </ul>
     * @param userId Id-ul utilizatorului pentru care se doreste adaugarea inregistrarii;
     * @param filename Numele fisierului;
     * @param replication_factor Factorul de replicare;
     */
    public void addRegister(String userId, String filename, int replication_factor, long crc, String filestatus,long filesize, String versionNo, String versionDescription ){
        synchronized (this.contentTable){
            FileAttributes fileAttribute = new FileAttributes(filename, replication_factor, filestatus, crc, filesize, versionNo, versionDescription);
            if(this.containsUser(userId)){
                this.contentTable.get(userId).add(fileAttribute);
            }
            else{
                this.contentTable.put(userId, new ArrayList<FileAttributes>(){{
                    add(fileAttribute);
                }});
            }
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

    /**
     * Functie de modificare a versiunii unui fisier.
     * @param userId identificatorul utilizatorului
     * @param filename numele fisierului
     * @param version numarul versiunii
     * @param description descrierea versiunii
     * @throws Exception Inregisrarea exista deja
     */
    public void updateFileVersion(String userId, String filename, int version, String description) throws Exception {
        synchronized (this.contentTable) {
            if (!this.containsUser(userId)) {
                throw new Exception("Register not found!");
            }
            for (FileAttributes fileAttributes : this.contentTable.get(userId)) {
                if (fileAttributes.getFilename().equals(filename)) {
                    try {
                        if(version == -1) {
                            int versionNumber = Integer.parseInt(fileAttributes.getVersionNo().substring(1));
                            fileAttributes.setVersionNo("v" + (versionNumber + 1));
                        }
                        else{
                            fileAttributes.setVersionNo("v" + version);
                        }
                        fileAttributes.setVersionDescription(description);
                        return;
                    }
                    catch (NumberFormatException exception){
                        LoggerService.registerWarning(generalManagerIpAddress, "Exceptie de parsare la updateFileVersionNo");
                    }

                }
            }
            throw new Exception("Register not found!");
        }
    }

    /**
     * Functie de modificare a dimensiunii unui fisier.
     * @param userId identificatorul utilizatorului
     * @param filename numele fisierului
     * @param filesize Dimensiunea fisierului
     * @throws Exception Inregisrarea exista deja
     */
    public void updateFileSize(String userId, String filename, long filesize) throws Exception{
        synchronized (this.contentTable) {
            if (!this.containsUser(userId)) {
                throw new Exception("Register not found!");
            }
            for (FileAttributes fileAttributes : this.contentTable.get(userId)) {
                if (fileAttributes.getFilename().equals(filename)) {
                    try {
                        fileAttributes.setFileSize(filesize);
                        return;
                    }
                    catch (NumberFormatException exception){
                        LoggerService.registerWarning(generalManagerIpAddress, "Exceptie de parsare la updateFileSize");
                    }

                }
            }
            throw new Exception("Register not found!");
        }
    }

    /**
     * Functie care verifica daca un utilizator exista.
     * @param userId Id-ul utilizatorului cautat;
     */
    public boolean containsUser(String userId){
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
    public boolean checkForUserFile(String userId, String filename, long crc){
        try {
            List<FileAttributes> userFiles = getUserFiles(userId);
            if (userFiles == null)
                return false;
            for (FileAttributes userFile : userFiles) {
                if (userFile.getFilename().equals(filename) && (crc == -1 || userFile.getCrc() == crc)) {
                    return true;
                }
            }
        }
        catch (Exception exception){
            //return false; (e deja aruncat la final)
        }
        return false;
    }

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

    /**
     * Getter pentru lista fisierelor unui utilizator.
     * @param userId Identificatorul utilizatorului
     */
    public HashMap<String, Integer> getUserFiless(String userId) throws Exception{
        synchronized (this.contentTable) {
            if(!this.containsUser(userId)){
                throw new Exception("Register not found!");
            }
            return new HashMap<String, Integer>(){{
                for(FileAttributes fileAttributes : contentTable.get(userId)){
                    put(fileAttributes.getFilename(), fileAttributes.getReplication_factor());
                }
            }};
        }
    }

    /**
     * Getter pentru statusul unui fisier.
     * @param userId Identificatorul utilizatorului
     * @param filename Numele fisierului
     * @return Statusul fisierului.
     */
    public String getFileStatusForUser(String userId, String filename){
        try {
            for(FileAttributes file : this.getUserFiles(userId)){
                if(file.getFilename().equals(filename)){
                    return file.getStatus();
                }
            }
        }
        catch (Exception exception){
            System.out.println("User not found!");
        }
        return null;
    }

    /**
     * Getter pentru suma de control a unui fisier.
     * @param userId Identificatorul utilizatorului
     * @param filename Numele fisierului
     * @return Suma de control.
     */
    public long getCRCForUser(String userId, String filename){
        try {
            for(FileAttributes file : this.getUserFiles(userId)){
                if(file.getFilename().equals(filename)){
                    return file.getCrc();
                }
            }
        }
        catch (Exception exception){
            System.out.println("User not found!");
        }
        return -1;
    }

    /**
     * Getter pentru numarul versiunii unui fisier.
     * @param userId Identificatorul utilizatorului
     * @param filename Numele fisierului
     * @return Numarul versiunii
     */
    public String getVersionForUser(String userId, String filename){
        try {
            for(FileAttributes file : this.getUserFiles(userId)){
                if(file.getFilename().equals(filename)){
                    return file.getVersionNo();
                }
            }
        }
        catch (Exception exception){
            System.out.println("User not found!");
        }
        return null;
    }

    /**
     * Getter pentru dimensiunea unui fisier.
     * @param userId Identificatorul utilizatorului
     * @param filename Numele fisierului
     * @return Dimensiunea unui fisier.
     */
    public long getFileSizeOfUserFile(String userId, String filename){
        try {
            for(FileAttributes file : this.getUserFiles(userId)){
                if(file.getFilename().equals(filename)){
                    return file.getFileSize();
                }
            }
        }
        catch (Exception exception){
            System.out.println("User not found!");
        }
        return -1;
    }

    /**
     * <ul>
     * 	<li>Functie care creeaza o reprezentare a tebelei de content, astfel incat sa poata fitrimisa catre client.</li>
     * 	<li> Se va crea o lista de <strong>Object</strong>, pentru a putea adauga diverse tipuri de date.</li>
     * </ul>
     * @param userId Identificatorul utilizatorului
     * @return Reprezentarea tabelei de content sub forma unei liste de obiecte.
     */
    public List<Object> getUserFilesForFrontend(String userId) throws Exception {
        List<Object> userFiles = new ArrayList<>();
        for(FileAttributes file : this.getUserFiles(userId)){
            if(file.getStatus().contains("DELETE") || file.getStatus().contains("PENDING")){
                continue;
            }
            HashMap<String, Object> userFile = new HashMap<>();
            userFile.put("filename", file.getFilename());
            userFile.put("hash", file.getCrc());
            userFile.put("version", file.getVersionNo());
            userFile.put("version_description", file.getVersionDescription());
            userFile.put("filesize", file.getFileSize());
            userFiles.add(userFile);
        }
        return userFiles;
    }

    /**
     * Getter pentru tabela de continut
     */
    public List<FileAttributes> getContentTable() throws Exception {
        List<FileAttributes> results =  new ArrayList<>();
        for(String userId : new ArrayList<>(this.contentTable.keySet())){
            for(FileAttributes fileAttribute : this.getUserFiles(userId)){
                fileAttribute.setUserId(userId);
                results.add(fileAttribute);
            }
        }
        return results;
    }

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
