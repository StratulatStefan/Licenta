package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import data.Pair;

/**
 * Clasa care incapsuleaza tabela ce va contine toate fisierele care ar trebui sa se afle la nodul general,
 * impreuna cu utilizatorul caruia ii apartin si factorul de replicare.
 */
public class ContentTable {
    /** -------- Extra-descrieri -------- **/
    /**
     * Clasa care va ingloba o inregistrare din aceasta tabela;
     * Este specifica fiecarui utilizator;
     * Contine fisierele unui utilizator, impreuna cu factorul de replicare
     */
    public class ContentRegister{
        /** -------- Atribute -------- **/
        /**
         * Id-ul utilizatorului; Unic per inregistrare
         */
        private String userId;
        /**
         * Tabela ce contine fisierele si factorii de replicare
         */
        private HashMap<String, Integer> content;


        /** -------- Constructori -------- **/
        /**
         * Constructorul care creeaza o noua inregistrare pentru un utilizator
         * @param userId Id-ul userului pentru care se creeaza inregistrarea.
         */
        public ContentRegister(String userId){
            this.userId = userId;
            this.content = new HashMap<String, Integer>();
        }

        /**
         * Constructor care adauga o noua inregistrare pentru un utilizator, pornind de la un fisier
         * ce se doreste a se adauga.
         * @param userId Id-ul utilizatorului inregistarii.
         * @param filename Numele fisierului ce se doreste a fi adaugat.
         * @param replication_factor Factorul de replicare al fisierului.
         */
        public ContentRegister(String userId, String filename, int replication_factor){
            this.userId = userId;
            this.content = new HashMap<String, Integer>();
            try {
                addRegister(filename, replication_factor);
            }
            catch (Exception exception){
                // nu avem cum sa ajungem aici.
            }
        }


        /** -------- Gettere & Settere -------- **/
        /**
         * Getter pentru id-ul utilizatorului.
         */
        public String getUserId() {
            return userId;
        }

        /**
         * Setter pentru id-ul utilizatorului.
         */
        public void setUserId(String userId) {
            this.userId = userId;
        }

        /**
         * Getter pentru dimensiunea tabelei de fisiere.
         */
        public int size(){
            return this.content.size();
        }


        /** -------- Functii de prelucrare a tabelei -------- **/
        /**
         * Functie de adaugare a unui nou fisier in tabela inregistrarii curent.
         * @param filename Numele fisierului.
         * @param replication_factor Factorul de replicare al fisierului.
         * @throws Exception Se genereaza daca fisierul exista deja.
         */
        public void addRegister(String filename, int replication_factor) throws Exception{
            if(this.content.containsKey(filename)){
                throw new Exception("File already exists!");
            }
            this.content.put(filename, replication_factor);
        }

        /**
         * Functie de eliminare a unui fisier din tabela inregistrarii curente
         * @param filename Numele fisierului.
         * @throws Exception Generata daca fisierul nu exista.
         */
        public void removeRegister(String filename) throws Exception {
            if (!this.content.containsKey(filename)) {
                throw new Exception("Register not found");
            }
            this.content.remove(filename);
        }

        /**
         * Functie de modificare a factorului de replicare al unui fisier.
         * @param filename Numele fisierului care se doreste a fi modificat.
         * @param replication_factor Noul factor de replicare.
         * @throws Exception Generata daca fisierul nu exista.
         */
        public void updateReplicationFactor(String filename, int replication_factor) throws Exception{
            if(!this.content.containsKey(filename)){
                throw new Exception("Register not found");
            }
            this.content.put(filename, replication_factor);
        }

        /**
         * Functie de modificare a numelui unui fisier din tabela inregistrarii curente.
         * @param filename Numele (vechi) fisierului.
         * @param newfilename Noul nume.
         * @throws Exception Fisierul nu exista.
         */
        public void updateFileName(String filename, String newfilename) throws Exception{
            if(!this.content.containsKey(filename)){
                throw new Exception("Register not found");
            }
            int replFactor = this.content.get(filename);
            this.content.remove(filename);
            this.addRegister(newfilename, replFactor);
        }


        /** -------- Functii de baza, supraincarcate -------- **/
        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\t" + this.userId + "\n");
            for(String filename : new ArrayList<>(this.content.keySet())){
                stringBuilder.append("\t\t" + filename + " [" + this.content.get(filename) + "]\n");
            }
            stringBuilder.append("\n");
            return stringBuilder.toString();
        }
    }



    /** -------- Atribute -------- **/
    /**
     * Flag care sugereaza daca tabela necesita initializare;
     * Se va modifica imediat dupa initializare.
     */
    public boolean needInit = true;

    /**
     * Tabela de inregistrari; Utilizatorul unic si fisierele acestuia.
     */
    private final List<ContentRegister> contentTable;


    /** -------- Constructori & Initializare -------- **/
    /**
     * Constructorul clasei;
     * Creeaza o noua tabela de inregistrari.
     */
    public ContentTable(){
        this.contentTable = new ArrayList<ContentRegister>();
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
                for (String filename : new ArrayList<>(userFilesNodesCount.keySet())) {
                    try {
                        this.addRegister(user, filename, userFilesNodesCount.get(filename));
                    }
                    catch (Exception exception){
                        // nu prea avem cum sa ajunge aici la init
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
    public void addRegister(String userId, String filename, int replication_factor) throws Exception{
        synchronized (this.contentTable){
            for(ContentRegister register : this.contentTable){
                if(register.getUserId().equals(userId)){
                    register.addRegister(filename, replication_factor);
                    return;
                }
            }
            ContentRegister newRegister = new ContentRegister(userId, filename, replication_factor);
            this.contentTable.add(newRegister);
        }
    }

    /**
     * Functie de eliminare a unei noi inregistrari din tabela (un fisier sau chiar si userul daca nu mai are alte fisiere).
     * @param userId Id-ul utilizatorului pentru care se doreste adaugarea inregistrarii;
     * @param filename Numele fisierului;
     * @throws Exception Daca inregistrarea nu exista
     */
    public void deleteRegister(String userId, String filename) throws Exception{
        synchronized (this.contentTable){
            for(ContentRegister register : this.contentTable){
                if(register.getUserId().equals(userId)){
                    register.removeRegister(filename);
                    if(register.size() == 0){
                        this.contentTable.remove(register);
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
            for(ContentRegister register : this.contentTable){
                if(register.getUserId().equals(userId)){
                    register.updateReplicationFactor(filename, replication_factor);
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
            for(ContentRegister register : this.contentTable){
                if(register.getUserId().equals(userId)){
                    register.updateFileName(filename, newfilename);
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
            for(ContentRegister register : this.contentTable){
                if(register.getUserId().equals(userId)){
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
        HashMap<String, Integer> userFiles = getUserFiles(userId);
        return userFiles != null && new ArrayList<>(userFiles.keySet()).contains(filename);
    }


    /** -------- Gettere -------- **/
    /**
     * Getter pentru lista id-urilor tuturor utilizatorilor.
     */
    public List<String> getUsers(){
        List<String> userIds = new ArrayList<>();
        synchronized (this.contentTable){
            for(ContentRegister register : this.contentTable) {
                userIds.add(register.getUserId());
            }
            return userIds;
        }
    }

    /**
     * Getter pentru lista fisierelor unui utilizator.
     * @param userId Id-ul utilizatorului ale carui fisiere sunt solicitate.
     */
    public HashMap<String, Integer> getUserFiles(String userId){
        synchronized (this.contentTable) {
            for(ContentRegister register : this.contentTable){
                if(register.getUserId().equals(userId)){
                    return register.content;
                }
            }
            return null;
        }
    }


    /** -------- Functii de baza, supraincarcate -------- **/
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("------------------------------------\n");
        stringBuilder.append("Content Table\n");
        synchronized (this.contentTable) {
            for (ContentRegister register : this.contentTable) {
                stringBuilder.append(register);
            }
        }
        stringBuilder.append("------------------------------------\n");
        return stringBuilder.toString();
    }
}
