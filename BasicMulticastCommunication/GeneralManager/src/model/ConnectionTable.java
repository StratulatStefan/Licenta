package model;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConnectionTable {
    /**
     * Aceasta clasa este un wrapper pentru o lista de perechi de adrese;
     * Obiectul asupra carora se fac toate operatiile este reprezentat de o lista;
     * Lista contine perechi (adresa,contor), si va fi folosita pentru afisare conexiunilor nodului curent.
     */
    private List<Pair<Address, Long>> connectionTable;

    /**
     * Timestamp-ul la care ne raportam, astfel incat sa nu obtinem o valoare foarte mare.
     */
    private static Timestamp baseTimestamp = Timestamp.valueOf("2020-01-01 00:00:00");

    /**
     * Constructorul clasei, care initializeaza lista vida.
     */
    public ConnectionTable(){
        this.connectionTable = new ArrayList<>();
    }

    /**
     * Functie care returneaza lista de adrese din tabela de conexiuni
     * @return
     */
    public List<Address> getConnectionTable(){
        List<Address> addresses = new ArrayList<Address>();
        for(Pair<Address, Long> connection : this.connectionTable){
            addresses.add(connection.getFirst());
        }
        return addresses;
    }

    /**
     * Functie de adaugare a unei adrese la lista de adrese.
     * Se considera ca orice adresa valida (o conexiune existenta) are contorul 0.
     * @param address Adresa ce va fi adaugata.
     */
    public void addAddress(Address address){
        Long timeInSeconds = getCurrentTimestamp();
        this.connectionTable.add(new Pair<Address, Long>(address, timeInSeconds));
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
        for(Pair<Address, Long> connection : this.connectionTable){
            if(connection.getFirst().equals(adddress)){
                return true;
            }
        }
        return false;
    }

    /**
     * Functie care "reseteaza" valorile timestamp-urilor fiecarei adrese.
     * Aceasta functie este folosita pentru validarea conexiunii, intrucat actualizeaza timestampul curent
     * pana la limita la care o conexiune este considerata instabila/rupta.
     * - Posibil sa nu mai fie nevoie -
     */
    public void resetAddressList(){
        for(Pair<Address, Long> connection : this.connectionTable){
            connection.setSecond(connection.getSecond() + 1);
        }
    }

    /**
     * Functie care verifica starea conexiunilor. O conexiune exista daca diferenta dintre timestampul curent si timestampul
     * conexiunii este mai mica decat limita. Altfel, pe masura ce nodul curent nu mai primeste mesaje de la nodul conex, timestampul
     * nu se mai modifica si, daca ajunge la o limita, conexiunea se considera rupta.
     * @param limit Limita de timp de la care o conexiune este considerata rupta.
     * @return Lista de conexiuni rupte.
     */
    public List<Address> checkDisconnection(long limit){
        List<Address> disconnected = new ArrayList();
        long currentTimestamp = getCurrentTimestamp();
        for(Pair<Address, Long> connection : this.connectionTable){
            if((currentTimestamp - connection.getSecond()) >= limit){
                disconnected.add(connection.getFirst());
            }
        }
        return disconnected;
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
        long currentTimestamp = getCurrentTimestamp();
        for (Pair<Address, Long> connection : this.connectionTable) {
            if (connection.getFirst().equals(address)) {
                connection.setSecond(currentTimestamp);
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
     * Functie care returneaza timestamp-ul curent, raportat la timestampul de baza al clasei
     */
    public static long getCurrentTimestamp(){
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        return (currentTimestamp.getTime() - ConnectionTable.baseTimestamp.getTime()) / 1000;
    }

    /**
     * Formatul afisarii listei de adrese.
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("------------------------------------\n");
        stringBuilder.append("Connection Table\n");
        for (Pair<Address, Long> connection : this.connectionTable) {
            stringBuilder.append("\t" + connection.getFirst().toString() + "\n");
        }
        stringBuilder.append("------------------------------------\n");
        return stringBuilder.toString();
    }
}
