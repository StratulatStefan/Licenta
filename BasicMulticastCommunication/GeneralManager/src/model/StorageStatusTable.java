package model;

import communication.Address;
import data.Pair;
import data.Time;
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
            Set<String> users = storageEntry.GetUsers();
            for(String user : users){
                String[] userFiles = storageEntry.GetUserFilesById(user);
                if(!this.statusTable.containsKey(user)){
                    this.statusTable.put(user, new HashMap<String, List<String>>());
                }
                for(String userFile : userFiles){
                    if(!this.statusTable.get(user).containsKey(userFile)){
                        this.statusTable.get(user).put(userFile, new ArrayList<String>() {{
                            add(nodeAddress.getIpAddress());
                        }});
                    }
                    else{
                        if(!CheckAddress(user, userFile, nodeAddress.getIpAddress())){
                            this.statusTable.get(user).get(userFile).add(nodeAddress.getIpAddress());
                        }
                    }
                }
            }
        }
    }

    public void CleanUpAtNodeDisconnection(String address){
        synchronized (this.statusTable){
            Set<String> users = this.statusTable.keySet();
            for(String user : users){
                Set<String> filenames = this.statusTable.get(user).keySet();
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

    public void CleanUpFull(){
        synchronized (this.statusTable){
            this.statusTable.clear();
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
