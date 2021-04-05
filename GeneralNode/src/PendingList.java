import data.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class PendingList {
    private final List<Pair<String, String>> pendingList;

    public PendingList(){
        this.pendingList = new ArrayList<>();
    }

    public void addToList(String userId, String filename) throws Exception{
        synchronized (this.pendingList){
            if(this.containsRegister(userId, filename))
                throw new Exception("Queue already contains file " + filename + " of user " + userId + "!");
            this.pendingList.add(new Pair<>(userId, filename));
        }
    }

    public List<Pair<String, String>> getPendingList(){
        synchronized (this.pendingList){
            return this.pendingList;
        }
    }

    public void removeFromList(String user, String file) throws Exception{
        synchronized (this.pendingList){
            for(Pair<String, String> request : this.pendingList){
                if(request.getFirst().equals(user) && request.getSecond().equals(file)){
                    this.pendingList.remove(request);
                    return;
                }
            }
            throw new Exception("Queue does not contain contains file " + file + " of user " + user + "!");
        }
    }
    
    public boolean containsRegister(String userId, String filename){
        synchronized (this.pendingList){
            for(Pair<String, String> request : this.pendingList){
                if(request.getFirst().equals(userId) && request.getSecond().equals(filename)){
                    return true;
                }
            }
        }
        return false;
    }
}
