package model;

import communication.Address;
import node_manager.NodeBeat;

import java.util.*;

public class StorageStatusTable {
    private final HashMap<String, HashMap<String, List<String>>> statusTable;

    public StorageStatusTable(){
        this.statusTable = new HashMap<>();
    }

    public void UpdateTable(NodeBeat storageEntry){
        Address nodeAddress = Address.parseAddress(storageEntry.GetNodeAddress());
        synchronized (this.statusTable){
            List<String> users = new ArrayList<>(storageEntry.GetUsers());
            for(String user : users){
                String[] userFiles = storageEntry.GetUserFilesById(user);

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
                        if(!CheckAddress(user, userFile, nodeAddress.getIpAddress())){
                            // daca fisierul utilizatorului curent exista, dar nu contine adresa nodului,
                            // adaugam adresa nodului
                            this.statusTable.get(user).get(userFile).add(nodeAddress.getIpAddress());
                        }
                    }
                }

                // verificam daca sunt useri stersi complet de la un nod; in acest caz;
                // eliminam adresa nodului de la care a fost sters, sau intregul user
                CleanUpOnDeletedUser(nodeAddress.getIpAddress(), new ArrayList<>(storageEntry.GetUsers()));

                // verificam daca sunt fisiere care au fost sterse, si le eliminam;
                // eliminam adresa nodului de la care a fost sters, sau fisierul daca nu se afla pe niciun nod
                List<String> deletedFiles = GetDeletedFiles(user, nodeAddress.getIpAddress(), storageEntry.GetUserFilesById(user));
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

    public void CleanUpAtNodeDisconnection(String address){
        synchronized (this.statusTable){
            List<String> users = new ArrayList<>(this.statusTable.keySet());
            for(String user : users){
                List<String> filenames = new ArrayList<>(this.statusTable.get(user).keySet());
                for(String filename : filenames){
                    if(CheckAddress(user, filename, address)){
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

    public List<String> GetDeletedFiles(String user, String userAddress, String[] userFiles){
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

    public void CleanUpOnDeletedUser(String userAddress, List<String> users){
        boolean found = false;
        synchronized (this.statusTable){
            for(String availableUser : new ArrayList<>(this.statusTable.keySet())){
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
                    if(this.statusTable.get(availableUser).get(file).contains(userAddress)){
                        this.statusTable.get(availableUser).get(file).remove(userAddress);
                    }
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

    public boolean CheckAddress(String user, String file, String address){
        synchronized (this.statusTable){
            for(String candidate : this.statusTable.get(user).get(file)){
                if(candidate.equals(address)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean CheckFileForUser(String user, String file){
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
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("------------------------------------\n");
        stringBuilder.append("Storage Status Table\n");
        synchronized (this.statusTable) {
            for (String user : this.statusTable.keySet()) {
                stringBuilder.append("User id  : " + user + "\n");
                for (String file : this.statusTable.get(user).keySet()) {
                    stringBuilder.append("\tFilename : " + file + "\n");
                    for (String address : this.statusTable.get(user).get(file)) {
                        stringBuilder.append("\t\t" + address + "\n");
                    }
                }
                stringBuilder.append("\n");
            }
        }
        stringBuilder.append("------------------------------------\n");
        return stringBuilder.toString();
    }
}
