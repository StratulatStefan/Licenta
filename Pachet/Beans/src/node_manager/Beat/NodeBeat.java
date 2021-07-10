package node_manager.Beat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * <ul>
 *  <li>Clasa care va reprezenta mesajul de tip heartbeat, care va fi trimis de la nodul intern la managerul general.</li>
 *  <li>Va contine informarii de identificare a nodului, cantitatea de memorie ocupata si maparea de tip utilizator-fisiere</li>
 *  <li>Obiectul care va instantia aceasta clasa va fi trimis prin retea, deci va trebui sa fie serializabil.</li>
 * </ul>
 */
public class NodeBeat implements Serializable {
    /**
     * Adresa nodului intern curent;
     */
    private String address;
    /**
     * Cantitatea de memorie ocupata la nodul curent; in kilobytes
     */
    private long memoryQuantity;
    /**
     * In interiorul unui heartbeat, va fi trimis si statusul stocarii;
     * Asadar, vom avea un hashmap care va contine fisierele disponibile in stocarea nodului curent, pentru toti userii
     */
    private HashMap<String, List<FileAttribute>> userFiles;


    /**
     * Constructorul clasei; Va initializa lista de fisiere pentru useri;
     */
    public NodeBeat(){
        this.userFiles = new HashMap<String, List<FileAttribute>>();
    }


    /**
     * Getter pentru cantitatea de memorie
     */
    public long getMemoryQuantity() {
        return memoryQuantity;
    }
    /**
     * Setter pentru cantitatea de memorie
     */
    public void setMemoryQuantity(long memoryQuantity) {
        this.memoryQuantity = memoryQuantity;
    }

    /**
     * Setter pentru adresa nodului curent
     * @param address Adresa nodului
     */
    public void setNodeAddress(String address){
        this.address = address;
    }
    /**
     * Getter pentru adresa nodului curent
     * @return Adresa nodului
     */
    public String getNodeAddress(){
        return this.address;
    }


    /**
     * Setter pentru lista de fisiere pentru useri;
     * Se va adauga o inregistrare ce va contine identificatorul userului si toate fisierele lui
     * @param userDir Id-ul utilizatorul.
     * @param userFiles Fisierele disponibile pentru utilizator.
     */
    public void addUserFiles(String userDir, List<FileAttribute> userFiles){
        this.userFiles.put(userDir, userFiles);
    }
    /**
     * Getter pentru lista fisierelor unui user, pe baza id-ului;
     * @param userId Id-ul utilizatorului pentru care se doreste obtinerea fisierelor
     * @return Lista fisierelor pentru utilizatorul specificat
     */
    public List<FileAttribute> getUserFilesById(String userId){
        return this.userFiles.get(userId);
    }
    /**
     * Functie care returneaza lista utilizatorilor care au fisiere stocate pe acest nod
     * @return Lista utilizatorilor
     */
    public List<String> getUsers(){
        return new ArrayList<>(this.userFiles.keySet());
    }
    /**
     * Functie care returneaza intreaga tabela de status al stocarii
     */
    public HashMap<String, List<FileAttribute>> getUserFiles(){
        return this.userFiles;
    }

    /**
     * Functie apelata cand se doreste citirea noului status al stocarii;
     * Lista de fisiere disponibile trebuie sa fie curatata (goala)
     */
    public void cleanUp(){
        this.userFiles.clear();
    }
}
