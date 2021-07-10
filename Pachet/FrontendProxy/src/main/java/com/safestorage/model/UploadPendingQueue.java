package com.safestorage.model;

import data.Pair;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * <ul>
 * 	<li>Clasa care va descrie o coada si toate functionalitatile aferente.</li>
 * 	<li>Va avea ca parametri obiecte de tip pereche de siruri de caractere (<strong>Pair(String,String)</strong>).</li>
 * 	<li>Cei doi membri ai perechii vor fi (id_utilizator, nume_fisier)</li>
 * </ul>
 */
public class UploadPendingQueue {
    /**
     * <ul>
     * 	<li>Structura de date de tip coada, care va contine ca inregistrari, perechi de doua siruri de caractere.</li>
     * 	<li>Coada trebuie sa fie <strong>final</strong> pentru a se putea realiza sincronizarea explicita (<strong>synchronized</strong>).</li>
     * </ul>
     */
    private final Queue<Pair<String, String>> pendingQueue;


    /**
     * Constructorul fara parametri, care va instantia obiectul de tip coada.
     */
    public UploadPendingQueue(){
        this.pendingQueue = new ArrayDeque<Pair<String, String>>();
    }


    /**
     * <ul>
     * 	<li>Functia de adaugare a unei inregistrari in coada.</li>
     * 	<li>Se impune ca inregistrarea sa nu existe deja in coada, altfel se va genera o exceptie.</li>
     * 	<li>Se asigura sincronizarea cozii inainte de accesare.</li>
     * </ul>
     * @param userId Identificatorul utilizatorului
     * @param filename Numele fisierului
     * @throws Exception Exceptie generata daca inregistrarea exista deja.
     */
    public void addToQueue(String userId, String filename) throws Exception{
        synchronized (this.pendingQueue){
            if(this.containsRegister(userId, filename))
                throw new Exception("Queue already contains file " + filename + " of user " + userId + "!");
            this.pendingQueue.add(new Pair<String, String>(userId, filename));
        }
    }

    /**
     * <ul>
     * 	<li>Functie de extragere a elementului din capul cozii.</li>
     * 	<li>Se asigura sincronizarea cozii inainte de accesare.</li>
     * </ul>
     */
    public Pair<String, String> popFromQueue(){
        synchronized (this.pendingQueue){
            return this.pendingQueue.poll();
        }
    }

    /**
     * <ul>
     * 	<li>Functie de verificare a existentei unei inregistrari in coada.</li>
     * 	<li> Verificarea se face din punct de vedere a celor doi membri ai perechii.</li>
     * 	<li>Se asigura sincronizarea cozii inainte de accesare.</li>
     * </ul>
     */
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
