package com.dropbox.frontend_proxy_ui.model;

import data.Pair;

import java.util.ArrayDeque;
import java.util.Queue;

public class UploadPendingQueue {
    private final Queue<Pair<String, String>> pendingQueue;

    public UploadPendingQueue(){
        this.pendingQueue = new ArrayDeque<Pair<String, String>>();
    }

    public void addToQueue(String userId, String filename) throws Exception{
        synchronized (this.pendingQueue){
            if(this.containsRegister(userId, filename))
                throw new Exception("Queue already contains file " + filename + " of user " + userId + "!");
            this.pendingQueue.add(new Pair<String, String>(userId, filename));
        }
    }

    public Pair<String, String> popFromQueue(){
        synchronized (this.pendingQueue){
            return this.pendingQueue.poll();
        }
    }

    public boolean containsRegister(String userId, String filename){
        synchronized (this.pendingQueue){
            if(pendingQueue.size() == 0)
                return false;
            Pair<String, String> queueHead = this.pendingQueue.element();
            if(queueHead.getFirst().equals(userId) && queueHead.getSecond().equals(filename)){
                return true;
            }
        }
        return false;
    }
}
