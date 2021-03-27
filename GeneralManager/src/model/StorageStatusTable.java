package model;

import communication.Address;
import node_manager.NodeBeat;

import java.util.*;

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
    private final HashMap<String, HashMap<String, List<String>>> statusTable;


    /** -------- Constructori -------- **/
    /**
     * Constructorul clasei;
     * Initializeaza tabela de inregistrari
     */
    public StorageStatusTable(){
        this.statusTable = new HashMap<>();
    }


    /** -------- Functii de prelucrare a tabelei -------- **/
    /**
     * Functie generala care modifica tabela, la sosirea unui nou heartbeat de la nodul intern;
     * Se includ operatii de adaugare a unui fisier, de eliminare a unui fisier, de curatare.
     * @param storageEntry Heartbeat de la nodul intern; Va contine adresa nodului, impreuna
     *                     cu toti utilizatorii existenti si fisierele acestora
     */
    public void updateTable(NodeBeat storageEntry){
        Address nodeAddress = Address.parseAddress(storageEntry.getNodeAddress());
        synchronized (this.statusTable){
            List<String> users = new ArrayList<>(storageEntry.getUsers());
            for(String user : users){
                String[] userFiles = storageEntry.getUserFilesById(user);

                if(!this.statusTable.containsKey(user)){
                    // daca utilizatorul nu exista, il adaugam.
                    this.statusTable.put(user, new HashMap<String, List<String>>());
                }
                for(String userFile : userFiles){
                    // daca fisierul utilizatorului curent nu exista, il adaugam
                    if(!this.statusTable.get(user).containsKey(userFile)){
                        this.statusTable.get(user).put(userFile, new ArrayList<String>() {{
                            add(nodeAddress.getIpAddress());
                        }});
                    }
                    else{
                        if(!checkAddress(user, userFile, nodeAddress.getIpAddress())){
                            // daca fisierul utilizatorului curent exista, dar nu contine adresa nodului,
                            // adaugam adresa nodului
                            this.statusTable.get(user).get(userFile).add(nodeAddress.getIpAddress());
                        }
                    }
                }

                // verificam daca sunt useri stersi complet de la un nod; in acest caz;
                // eliminam adresa nodului de la care a fost sters, sau intregul user
                cleanUpOnDeletedUser(nodeAddress.getIpAddress(), new ArrayList<>(storageEntry.getUsers()));

                // verificam daca sunt fisiere care au fost sterse, si le eliminam;
                // eliminam adresa nodului de la care a fost sters, sau fisierul daca nu se afla pe niciun nod
                List<String> deletedFiles = getDeletedFiles(user, nodeAddress.getIpAddress(), storageEntry.getUserFilesById(user));
                for(String deletedFile : deletedFiles){
                    this.statusTable.get(user).get(deletedFile).remove(nodeAddress.getIpAddress());
                    if(this.statusTable.get(user).get(deletedFile).size() == 0){
                        this.statusTable.get(user).remove(deletedFile);
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
                List<String> filenames = new ArrayList<>(this.statusTable.get(user).keySet());
                for(String filename : filenames){
                    if(checkAddress(user, filename, address)){
                        this.statusTable.get(user).get(filename).remove(address);
                    }
                    if(this.statusTable.get(user).get(filename).size() == 0){
                        this.statusTable.get(user).remove(filename);
                    }
                }
                if(this.statusTable.get(user).keySet().size() == 0){
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
            for(String availableUser : new ArrayList<>(this.statusTable.keySet())){
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
                for(String file : new ArrayList<>(this.statusTable.get(availableUser).keySet())){
                    this.statusTable.get(availableUser).get(file).remove(userAddress);
                    if(this.statusTable.get(availableUser).get(file).size() == 0){
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
     * Functie care verifica daca, pentru un anumit fisier al unui anumit utilizator, mai exista adresa unui nod.
     * @param user Id-ul utilizatorului.
     * @param file Numele fisierului.
     * @param address Adresa cautata.
     */
    public boolean checkAddress(String user, String file, String address){
        synchronized (this.statusTable){
            for(String candidate : this.statusTable.get(user).get(file)){
                if(candidate.equals(address)){
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
            if(!this.statusTable.containsKey(user)){
                return false;
            }
            List<String> userFiles = new ArrayList<>(this.statusTable.get(user).keySet());
            if(!userFiles.contains(file)){
                return false;
            }
        }
        return true;
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
    public List<String> getDeletedFiles(String user, String userAddress, String[] userFiles){
        List<String> deletedFiles = new ArrayList<>();
        boolean found;
        synchronized (this.statusTable){
            for(String availableFile : new ArrayList<>(this.statusTable.get(user).keySet())){
                found = false;
                for(String candidateFile : userFiles){
                    if(availableFile.equals(candidateFile) && this.statusTable.get(user).get(availableFile).contains(userAddress)){
                        found = true;
                        break;
                    }
                }
                if(!found){
                    deletedFiles.add(availableFile);
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
                return statusTable.get(user).get(file);
            }
            catch (NullPointerException exception){
                return null;
            }
        }
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
            HashMap<String, Integer> filesNodesCounts = new HashMap<>();
            for (String filename : new ArrayList<>(this.statusTable.get(userId).keySet())) {
                filesNodesCounts.put(filename, this.statusTable.get(userId).get(filename).size());
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
                for (String file : this.statusTable.get(user).keySet()) {
                    stringBuilder.append("\tFilename : ").append(file).append("\n");
                    for (String address : this.statusTable.get(user).get(file)) {
                        stringBuilder.append("\t\t").append(address).append("\n");
                    }
                }
                stringBuilder.append("\n");
            }
        }
        stringBuilder.append("------------------------------------\n");
        return stringBuilder.toString();
    }
}
