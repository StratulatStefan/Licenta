package node_manager;
import java.util.ArrayList;
import java.util.List;

/**
 * <ul>
 * 	<li>Clasa folosita pentru a reprezenta cererea de replicare a unui fisier, trimisa de la managerul general la nodul intern.</li>
 * 	<li>Va mosteni clasa ClientRequestManager, care incapsuleaza o cerere dintre managerul general si nodul intern.</li>
 * 	<li>Pe langa atributele de baza ale unei cereri dintre client si managerul general, avem nevoie si lista de noduri care vor stoca noul fisier.</li>
 * </ul>
 * **/
public class ReplicationRequest extends EditRequest {
    /**
     * <ul>
     * 	<li>Lista de noduri interne care vor stoca noul fisier.</li>
     * 	<li>Tipul ArrayList este serializabil.</li>
     * </ul>
     */
    private ArrayList<String> destionationAddress;


    /**
     * <ul>
     * 	<li> Constructorul clasei.</li>
     * 	<li> Apeleaza constructorul clasei parinte, furnizand cele doua argumente generice fiecarei cereri.</li>
     * 	<li> Instantiaza lista de adrese.</li>
     * </ul>
     * @param user Id-ul utilizatoului
     * @param filename Numele fisierului utilizatorului
     * @param destinationAddress Lista de adrese destinatie, pe care se va replica fisierul
     */
    public ReplicationRequest(String user, String filename, List<String> destinationAddress){
        super(user, filename);
        this.destionationAddress = new ArrayList<>(destinationAddress);
    }
    /**
     * Constructor vid
     */
    public ReplicationRequest(){super();}


    /**
     * Getter pentru lista de noduri care vor stoca noul fisier.
     */
    public List<String> getDestionationAddress() {
        return destionationAddress;
    }
    /**
     * <ul>
     * 	<li>Setter pentru lista de noduri care vor stoca noul fisier.</li>
     * 	<li>Nu se va folosi tipul parametrului de intrare (List), ci se va crea un nou obiect de tipul
     ArrayList care, spre deosebire de tipul parametrului, este Serializabil.</li>
     * </ul>
     */
    public void setDestionationAddress(List<String> destionationAddress) {
        this.destionationAddress = new ArrayList<>(destionationAddress);
    }
}
