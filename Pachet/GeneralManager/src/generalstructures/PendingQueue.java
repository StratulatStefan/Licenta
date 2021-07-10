package generalstructures;

import model.PendingQueueRegister;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * <ul>
 * 	<li>Clasa care va contine o coada, ce va fi folosita in procesul de stocare de noi fisiere.</li>
 * 	<li>Fiecare element al cozii va fi de tipul <strong>PendingQueueRegister</strong>.</li>
 * </ul>
 */
public class PendingQueue {
    /**
     * <ul>
     * 	<li>Structura de date de tip coada, asupra careia se vor putea efectua operatiile specifice.</li>
     * 	<li>Obiectul va fi <strong>final</strong>, intrucat se va realiza sincronizarea explicita <strong>synchronized</strong>.</li>
     * </ul>
     */
    private final Queue<PendingQueueRegister> pendingQueue;

    /**
     * Constructorul fara parametri, care va instantia coada.
     */
    public PendingQueue(){
        this.pendingQueue = new ArrayDeque<PendingQueueRegister>();
    }

    /**
     * <ul>
     * 	<li>Functie pentru adaugarea unui element in coada.</li>
     * 	<li>Fiecare element va fi identificata in mod unic prin perechea <strong>userid, filename</strong>.</li>
     * 	<li>Inainte de introducere, se impune ca elementul sa nu existe deja in coada.</li>
     * </ul>
     * @param userId Identificatorul unic al utilizatorului.
     * @param filename Numele fisierului.
     * @throws Exception Elementul exista deja in coada.
     */
    public void addToQueue(String userId, String filename) throws Exception{
        synchronized (this.pendingQueue){
            if(this.containsRegister(userId, filename))
                throw new Exception("Queue already contains file " + filename + " of user " + userId + "!");
            this.pendingQueue.add(new PendingQueueRegister(userId, filename));
        }
    }

    /**
     * Functie prin care se extrage elementul din capul cozii.
     */
    public PendingQueueRegister popFromQueue(){
        synchronized (this.pendingQueue){
            return this.pendingQueue.poll();
        }
    }

    /**
     * <ul>
     * 	<li>Functie care verifica daca un element exista in coada.</li>
     * 	<li> Verificarea se face pe baza perechii <strong>id utilizator, nume fisier</strong>.</li>
     * </ul>
     * @param userId Identificatorul unic al utilizatorului.
     * @param filename Numele fisierului.
     */
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
