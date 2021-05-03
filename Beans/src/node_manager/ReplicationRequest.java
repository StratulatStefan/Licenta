package node_manager;
import java.util.ArrayList;
import java.util.List;

/**
 * Clasa folosita pentru a incapsula cererea de replicare a unui fisier, trimisa de la managerul general la nodul intern
 * Va mosteni clasa ClientRequestManager, care incapsuleaza o cerere dintre managerul general si nodul intern
 * Pe langa atributele de baza ale unei cereri dintre client si managerul general, avem nevoie si lista de noduri care vor stoca noul fisier.
 * **/
public class ReplicationRequest extends EditRequest {
    /** -------- Atribute -------- **/
    /**
     * Lista de noduri interne care vor stoca noul fisier.
     * Tipul ArrayList este serializabil.
     */
    private ArrayList<String> destionationAddress;

    /**
     * Constructorul clasei; Apeleaza constructorul clasei parinte, furnizand cele doua argumente
     * generice fiecarei cereri; Mai mult decat atat, instantiaza lista de adrese;
     * @param user Id-ul utilizatoului
     * @param filename Numele fisierului utilizatorului
     * @param destinationAddress Lista de adrese destinatie, pe care se va replica fisierul
     */
    public ReplicationRequest(String user, String filename, List<String> destinationAddress){
        super(user, filename);
        this.destionationAddress = new ArrayList<>(destinationAddress);
    }


    /** -------- Gettere & Settere -------- **/
    /**
     * Getter pentru lista de noduri care vor stoca noul fisier.
     */
    public List<String> getDestionationAddress() {
        return destionationAddress;
    }
    /**
     * Setter pentru lista de noduri care vor stoca noul fisier.
     * Nu se va folosi tipul parametrului de intrare (List), ci se va crea un nou obiect de tipul
     * ArrayList care, spre deosebire de tipul parametrului, este Serializabil.
     */
    public void setDestionationAddress(List<String> destionationAddress) {
        this.destionationAddress = new ArrayList<>(destionationAddress);
    }
}
