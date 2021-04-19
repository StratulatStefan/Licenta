package tables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import data.Pair;
import communication.Address;
import data.Time;

/**
 * Tabela de conexiuni; Va contine adresele tuturor nodurilor conectate la nodul general, impreuna cu
 * momentul de timp la care s-a primit ultima confirmare a existentei.
 */
public class ConnectionTable {
    /** -------- Atribute -------- **/
    /**
     * Aceasta clasa este un wrapper pentru o lista de perechi de adrese;
     * Obiectul asupra carora se fac toate operatiile este reprezentat de o lista;
     * Lista contine perechi (adresa,contor), si va fi folosita pentru afisare conexiunilor nodului curent.
     */
    private final List<Pair<Address, Long>> connectionTable;


    /** -------- Constructor -------- **/
    /**
     * Constructorul clasei, care initializeaza lista vida.
     */
    public ConnectionTable(){
        this.connectionTable = Collections.synchronizedList(new ArrayList<>());
    }


    /** -------- Getter -------- **/
    /**
     * Functie care returneaza lista de adrese din tabela de conexiuni
     */
    public List<String> getConnectionTable(){
        List<String> addresses = new ArrayList<String>();
        for(Pair<Address, Long> connection : this.connectionTable){
            addresses.add(connection.getFirst().getIpAddress());
        }
        return addresses;
    }

    /**
     * @return Dimensiunea listei de adrese.
     */
    public int size(){
        return this.connectionTable.size();
    }


    /** -------- Functii de prelucrare a tabelei -------- **/
    /**
     * Functie de adaugare a unei adrese la lista de adrese.
     * Se considera ca orice adresa valida (o conexiune existenta) are contorul 0.
     * @param address Adresa ce va fi adaugata.
     */
    public void addAddress(Address address){
        Long timeInSeconds = Time.getCurrentTimestamp();
        synchronized (this.connectionTable) {
            this.connectionTable.add(new Pair<Address, Long>(address, timeInSeconds));
        }
    }

    /**
     * Functie de eliminare a unei adrese din lista de adrese.
     * Se apeleaza atunci cand o conexiune nu mai este stabila.
     * @param address Adresa ce va fi stearsa.
     */
    public void removeAddress(Address address){
        if(this.containsAddress(address)) {
            synchronized (this.connectionTable) {
                this.connectionTable.removeIf(connection -> connection.getFirst().equals(address));
            }
        }
    }

    /**
     * Functie care confirma existenta conexiunii cu un anumit nod. Aceasta se indica prin setarea
     * timestampului conexiunii respective cu timpul curent.
     * @param address Adresa nodului cu care exista conexiune.
     * @throws Exception generata daca adresa specificata nu exista
     */
    public void confirmAvailability(Address address) throws Exception {
        if (!this.containsAddress(address)) {
            throw new Exception("Address not found : " + address);
        }
        long currentTimestamp = Time.getCurrentTimestamp();
        synchronized (this.connectionTable) {
            for (Pair<Address, Long> connection : this.connectionTable) {
                if (connection.getFirst().equals(address)) {
                    connection.setSecond(currentTimestamp);
                }
            }
        }
    }



    /** -------- Functii de validare a tabelei -------- **/
    /**
     * Functie de verificare a existentei unei anumite adrese in lista de adrese.
     * @param adddress Adresa cautata
     * @return existenta adresei in lista
     */
    public boolean containsAddress(Address adddress){
        for(Pair<Address, Long> connection : this.connectionTable){
            if(connection.getFirst().equals(adddress)){
                return true;
            }
        }
        return false;
    }

    /**
     * Functie care verifica starea conexiunilor. O conexiune exista daca diferenta dintre timestampul curent si timestampul
     * conexiunii este mai mica decat limita. Altfel, pe masura ce nodul curent nu mai primeste mesaje de la nodul conex, timestampul
     * nu se mai modifica si, daca ajunge la o limita, conexiunea se considera rupta.
     * @param limit Limita de timp de la care o conexiune este considerata rupta.
     * @return Lista de conexiuni rupte.
     */
    public List<Address> checkDisconnection(double limit){
        List<Address> disconnected = new ArrayList();
        long currentTimestamp = Time.getCurrentTimestamp();
        for(Pair<Address, Long> connection : this.connectionTable){
            if((currentTimestamp - connection.getSecond()) > limit){
                disconnected.add(connection.getFirst());
            }
        }
        return disconnected;
    }


    /** -------- Functii de baza, supraincarcate -------- **/
    /**
     * Formatul afisarii listei de adrese.
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("------------------------------------\n");
        stringBuilder.append("Connection Table\n");
        for (Pair<Address, Long> connection : this.connectionTable) {
            stringBuilder.append("\t").append(connection.getFirst().toString()).append("\n");
        }
        stringBuilder.append("------------------------------------\n");
        return stringBuilder.toString();
    }
}
