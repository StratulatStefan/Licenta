package model;

import data.Pair;

import java.util.ArrayDeque;
import java.util.Queue;

public class PendingQueue {
    private final Queue<Pair<String, String>> pendingQueue;

    public PendingQueue(){
        this.pendingQueue = new ArrayDeque<>();
    }

    public void addToQueue(String userId, String filename) throws Exception{
        synchronized (this.pendingQueue){
            if(this.containsRegister(userId, filename))
                throw new Exception("Queue already contains file " + filename + " of user " + userId + "!");
            this.pendingQueue.add(new Pair<>(userId, filename));
        }
    }

    public Pair<String, String> popFromQueue(){
        synchronized (this.pendingQueue){
            return this.pendingQueue.poll();
        }
    }

    public boolean containsRegister(String userId, String filename){
        synchronized (this.pendingQueue){
            for(Pair<String, String> request : this.pendingQueue){
                if(request.getFirst().equals(userId) && request.getSecond().equals(filename)){
                    return true;
                }
            }
        }
        return false;
    }
}
