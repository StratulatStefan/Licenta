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
                AddRegister(filename, replication_factor);
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

        public void AddRegister(String filename, int replication_factor) throws Exception{
            if(this.content.containsKey(filename)){
                throw new Exception("File already exists!");
            }
            this.content.put(filename, replication_factor);
        }

        public void RemoveRegister(String filename) throws Exception {
            if (!this.content.containsKey(filename)) {
                throw new Exception("Register not found");
            }
            this.content.remove(filename);
        }

        public void UpdateReplicationFactor(String filename, int replication_factor) throws Exception{
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



    private final List<ContentRegister> contentTable;

    public ContentTable(){
        this.contentTable = new ArrayList<ContentRegister>();
    }

    public void AddRegister(String userId, String filename, int replication_factor) throws Exception{
        synchronized (this.contentTable){
            for(ContentRegister register : this.contentTable){
                if(register.getUserId().equals(userId)){
                    register.AddRegister(filename, replication_factor);
                    return;
                }
            }
            ContentRegister newRegister = new ContentRegister(userId, filename, replication_factor);
            this.contentTable.add(newRegister);
        }
    }

    public void DeleteRegister(String userId, String filename) throws Exception{
        synchronized (this.contentTable){
            for(ContentRegister register : this.contentTable){
                if(register.getUserId().equals(userId)){
                    register.RemoveRegister(filename);
                    if(register.size() == 0){
                        this.contentTable.remove(userId);
                    }
                    return;
                }
            }
            throw new Exception("Register not found!");
        }
    }

    public void UpdateReplicationFactor(String userId, String filename, int replication_factor) throws Exception{
        synchronized (this.contentTable){
            for(ContentRegister register : this.contentTable){
                if(register.getUserId().equals(userId)){
                    register.UpdateReplicationFactor(filename, replication_factor);
                    return;
                }
            }
            throw new Exception("Register not found!");
        }
    }

    public boolean ContainsUser(String userId){
        synchronized (this.contentTable){
            for(ContentRegister register : this.contentTable){
                if(register.getUserId().equals(userId)){
                    return true;
                }
            }
        }
        return false;
    }

    public List<String> GetUsers(){
        List<String> userIds = new ArrayList<>();
        synchronized (this.contentTable){
            for(ContentRegister register : this.contentTable) {
                userIds.add(register.getUserId());
            }
            return userIds;
        }
    }

    public HashMap<String, Integer> GetUserFiles(String userId){
        synchronized (this.contentTable) {
            for(ContentRegister register : this.contentTable){
                if(register.getUserId().equals(userId)){
                    return register.content;
                }
            }
            return null;
        }
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
