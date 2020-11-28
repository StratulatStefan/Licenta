package model;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConnectionTable {
    /**
     * Aceasta clasa este un wrapper pentru o lista de perechi de adrese;
     * Obiectul asupra carora se fac toate operatiile este reprezentat de o lista;
     * Lista contine perechi (adresa,contor), si va fi folosita pentru afisare conexiunilor nodului curent.
     */
    private List<Pair<Address, Integer>> connectionTable;

    /**
     * Constructorul clasei, care initializeaza lista vida.
     */
    public ConnectionTable(){
        this.connectionTable = new ArrayList<>();
    }

    /**
     * Functie de adaugare a unei adrese la lista de adrese.
     * Se considera ca orice adresa valida (o conexiune existenta) are contorul 0.
     * @param address Adresa ce va fi adaugata.
     */
    public void addAddress(Address address){
        this.connectionTable.add(new Pair<Address, Integer>(address, 0));
    }

    /**
     * Functie de eliminare a unei adrese din lista de adrese.
     * Se apeleaza atunci cand o conexiune nu mai este stabila.
     * @param address Adresa ce va fi stearsa.
     */
    public void removeAddress(Address address){
        if(this.containsAddress(address)) {
            this.connectionTable.removeIf(connection -> connection.getFirst().equals(address));
        }
    }

    /**
     * Functie de verificare a existentei unei anumite adrese in lista de adrese.
     * @param adddress Adresa cautata
     * @return existenta adresei in lista
     */
    public boolean containsAddress(Address adddress){
        for(Pair<Address, Integer> connection : this.connectionTable){
            if(connection.getFirst().equals(adddress)){
                return true;
            }
        }
        return false;
    }

    /**
     * Functie care "reseteaza" valorile contoarelor fiecarei adrese.
     * Aceasta functie este folosita pentru validarea conexiunii, intrucat incrementeaza contorul curent
     * pana la limita la care o conexiune este considerata instabila/rupta.
     */
    public void resetAddressList(){
        for(Pair<Address, Integer> connection : this.connectionTable){
            connection.setSecond(connection.getSecond() + 1);
        }
    }

    /**
     * Functie care verifica starea conexiunilor. O conexiune exista daca contorul conexiunii respective
     * este 0. Altfel, pe masura ce nodul curent nu mai primeste mesaje de la nodul conex, contorul se incrementeaza
     * si, daca ajunge la o limita, conexiunea se considera rupta.
     * @param limit Limita de la care o conexiune este considerata rupta.
     * @return Lista de conexiuni rupte.
     */
    public List<Address> checkDisconnection(int limit){
        List<Address> disconnected = new ArrayList();
        for(Pair<Address, Integer> connection : this.connectionTable){
            if(connection.getSecond() >= limit){
                disconnected.add(connection.getFirst());
            }
        }
        return disconnected;
    }

    /**
     * Functie care confirma existenta conexiunii cu un anumit nod. Aceasta se indica prin setarea
     * contorului conexiunii respective la 0.
     * @param address Adresa nodului cu care exista conexiune.
     * @throws Exception generata daca adresa specificata nu exista
     */
    public void confirmAvailability(Address address) throws Exception {
        if (!this.containsAddress(address)) {
            throw new Exception("Address not found : " + address);
        }
        for (Pair<Address, Integer> connection : this.connectionTable) {
            if (connection.getFirst().equals(address)) {
                connection.setSecond(0);
            }
        }
    }

    /**
     * @return Dimensiunea listei de adrese.
     */
    public int size(){
        return this.connectionTable.size();
    }

    /**
     * Formatul afisarii listei de adrese.
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("------------------------------------\n");
        for (Pair<Address, Integer> connection : this.connectionTable) {
            stringBuilder.append(connection.getFirst().toString() + "\n");
        }
        stringBuilder.append("------------------------------------\n");
        return stringBuilder.toString();
    }
}
