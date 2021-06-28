package generalstructures;

import data.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * <ul>
 * 	<li>Clasa care defineste o lista de asteptare.</li>
 * 	<li>Se va folosi pentru a evita calcularea sumei de contorl pentru un fisiere care se afla in prelucrare.</li>
 * </ul>
 */
public class PendingList {
    /**
     * <ul>
     * 	<li>Obiectul de tip lista.</li>
     * 	<li> Va avea ca membri o pereche de doua siruri de caractere.</li>
     * 	<li> Obiectul va fi final pentru a se putea realiza mecanismele de sincronizare explicite.</li>
     * </ul>
     */
    private final List<Pair<String, String>> pendingList;

    /**
     * <ul>
     * 	<li>Constructorul clasei.</li>
     * 	<li> Va instantia obiectul de tip lista.</li>
     * </ul>
     */
    public PendingList(){
        this.pendingList = new ArrayList<>();
    }

    /**
     * <ul>
     * 	<li>Functie pentru adaugarea unui element in lista.</li>
     * 	<li> Se vor primi ca parametri cele doua siruri de caractere.</li>
     * 	<li> Se va genera o exceptie in cazul in care inregistrarea exista deja in lista.</li>
     * </ul>
     */
    public void addToList(String userId, String filename) throws Exception{
        synchronized (this.pendingList){
            if(this.containsRegister(userId, filename))
                throw new Exception("Queue already contains file " + filename + " of user " + userId + "!");
            this.pendingList.add(new Pair<>(userId, filename));
        }
    }

    /**
     * <ul>
     * 	<li>Functie pentru extragerea obiectului de tip lista de asteptare.</li>
     * </ul>
     */
    public List<Pair<String, String>> getPendingList(){
        synchronized (this.pendingList){
            return this.pendingList;
        }
    }

    /**
     * <ul>
     * 	<li>Functie pentru eliminarea unui element din lista.</li>
     * 	<li> Elementul va fi identificat pe baza celor doua siruri de caractere ce alcatuiesc obiectul.</li>
     * 	<li> Se va returna o exceptie in cazul in care elementul nu exista in lista.</li>
     * </ul>
     */
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

    /**
     * <ul>
     * 	<li>Functie care verifica daca un element exista in lista.</li>
     * 	<li> Se vor verifica cei doi membri ai unui element.</li>
     * </ul>
     */
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
