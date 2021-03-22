package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import data.Pair;

public class ContentTable {
    public class ContentRegister{
        private String userId;
        private HashMap<String, Integer> content;

        public ContentRegister(String userId){
            this.userId = userId;
            this.content = new HashMap<String, Integer>();
        }

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

        public String getUserId() {
            return userId;
        }
        public void setUserId(String userId) {
            this.userId = userId;
        }

        public void addRegister(String filename, int replication_factor) throws Exception{
            if(this.content.containsKey(filename)){
                throw new Exception("File already exists!");
            }
            this.content.put(filename, replication_factor);
        }

        public void removeRegister(String filename) throws Exception {
            if (!this.content.containsKey(filename)) {
                throw new Exception("Register not found");
            }
            this.content.remove(filename);
        }

        public void updateReplicationFactor(String filename, int replication_factor) throws Exception{
            if(!this.content.containsKey(filename)){
                throw new Exception("Register not found");
            }
            this.content.put(filename, replication_factor);
        }

        public int size(){
            return this.content.size();
        }

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

    public boolean needInit = true;

    private final List<ContentRegister> contentTable;

    public ContentTable(){
        this.contentTable = new ArrayList<ContentRegister>();
    }

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

    public boolean containsUser(String userId){
        synchronized (this.contentTable){
            for(ContentRegister register : this.contentTable){
                if(register.getUserId().equals(userId)){
                    return true;
                }
            }
        }
        return false;
    }

    public List<String> getUsers(){
        List<String> userIds = new ArrayList<>();
        synchronized (this.contentTable){
            for(ContentRegister register : this.contentTable) {
                userIds.add(register.getUserId());
            }
            return userIds;
        }
    }

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

    public boolean checkForUserFile(String userId, String filename){
        HashMap<String, Integer> userFiles = getUserFiles(userId);
        return userFiles != null && new ArrayList<>(userFiles.keySet()).contains(filename);
    }

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
