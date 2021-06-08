package generalstructures;

import model.PendingQueueRegister;

import java.util.ArrayDeque;
import java.util.Queue;


public class PendingQueue {
    private final Queue<PendingQueueRegister> pendingQueue;

    public PendingQueue(){
        this.pendingQueue = new ArrayDeque<PendingQueueRegister>();
    }

    public void addToQueue(String userId, String filename) throws Exception{
        synchronized (this.pendingQueue){
            if(this.containsRegister(userId, filename))
                throw new Exception("Queue already contains file " + filename + " of user " + userId + "!");
            this.pendingQueue.add(new PendingQueueRegister(userId, filename));
        }
    }

    public PendingQueueRegister popFromQueue(){
        synchronized (this.pendingQueue){
            return this.pendingQueue.poll();
        }
    }

    public boolean containsRegister(String userId, String filename){
        synchronized (this.pendingQueue){
            for(PendingQueueRegister request : this.pendingQueue){
                if(request.getUserId().equals(userId) && request.getFilename().equals(filename)){
                    return true;
                }
            }
        }
        return false;
    }
}
