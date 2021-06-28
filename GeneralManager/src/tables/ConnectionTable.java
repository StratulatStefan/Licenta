package tables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import communication.Serializer;
import data.Pair;
import communication.Address;
import data.Time;
import os.FileSystem;

/**
 * <ul>
 * 	<li>Tabela de conexiuni.</li>
 * 	<li> Va contine adresele tuturor nodurilor conectate la nodul general, impreuna cu momentul de timp la care s-a primit ultima confirmare a existentei.</li>
 * </ul>
 */
public class ConnectionTable {
    /**
     * <ul>
     * 	<li>Aceasta clasa este un wrapper pentru o lista de perechi de adrese.</li>
     * 	<li>Obiectul asupra carora se fac toate operatiile este reprezentat de o lista.</li>
     * 	<li>Lista contine perechi <strong>adresa,contor</strong>, si va fi folosita pentru afisare conexiunilor nodului curent.</li>
     * </ul>
     */
    private final List<Pair<Address, Long>> connectionTable;

    /**
     * Constructorul clasei, care initializeaza lista vida.
     */
    public ConnectionTable(){
        this.connectionTable = Collections.synchronizedList(new ArrayList<>());
    }

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
     * <ul>
     * 	<li>Functie de adaugare a unei adrese la lista de adrese.</li>
     * 	<li>Se considera ca orice adresa valida <strong>o conexiune existenta</strong> are contorul 0.</li>
     * </ul>
     * @param address Adresa ce va fi adaugata.
     */
    public void addAddress(Address address){
        Long timeInSeconds = Time.getCurrentTimestamp();
        synchronized (this.connectionTable) {
            this.connectionTable.add(new Pair<Address, Long>(address, timeInSeconds));
        }
    }

    /**
     * <ul>
     * 	<li>Functie de eliminare a unei adrese din lista de adrese.</li>
     * 	<li>Se apeleaza atunci cand o conexiune nu mai este stabila.</li>
     * </ul>
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
     * <ul>
     * 	<li>Functie care confirma existenta conexiunii cu un anumit nod.</li>
     * 	<li> Aceasta se indica prin setareatimestampului conexiunii respective cu timpul curent.</li>
     * </ul>
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
     * <ul>
     * 	<li>Functie care verifica starea conexiunilor.</li>
     * 	<li> O conexiune exista daca diferenta dintre timestampul curent si timestampulconexiunii este mai mica decat limita.</li>
     * 	<li> Altfel, pe masura ce nodul curent nu mai primeste mesaje de la nodul conex, timestampulnu se mai modifica si,
             daca ajunge la o limita, conexiunea se considera rupta.</li>
     * </ul>
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

    public String getSize(){
        try {
            long size = Serializer.getObjectSize(connectionTable);
            Pair<Double, String> sz = FileSystem.convertToBestScale(size, 0);
            return sz.getFirst() + " " + sz.getSecond();
        }
        catch (Exception exception){
            System.out.println("Nu pot calcula dimensiunea!");
            return "";
        }
    }

    public int numberOfActiveNodes(){
        return connectionTable.size();
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
