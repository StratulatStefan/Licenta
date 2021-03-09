package node_manager;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Clasa care va ingloba mesajul de tip heartbeat, care va fi trimis de la nodul intern la managerul general
 */
public class NodeBeat implements Serializable {
    /**
     * Adresa nodului intern curent;
     */
    private String address;
    /**
     * In interiorul unui heartbeat, va fi trimis si statusul stocarii;
     * Asadar, vom avea un hashmap care va contine fisierele disponibile in stocarea nodului curent, pentru toti userii
     */
    private HashMap<String, String[]> userFiles;

    /**
     * Constructorul clasei; Va initializa lista de fisiere pentru useri;
     */
    public NodeBeat(){
        this.userFiles = new HashMap<>();
    }


    /**
     * Setter pentru lista de fisiere pentru useri;
     * Se va adauga o inregistrare ce va contine identificatorul userului si toate fisierele lui
     * @param userDir Id-ul utilizatorul.
     * @param userFiles Fisierele disponibile pentru utilizator.
     */
    public void AddUserFiles(String userDir, String[] userFiles){
        this.userFiles.put(userDir, userFiles);
    }

    /**
     * Getter pentru lista fisierelor unui user, pe baza id-ului;
     * @param userId Id-ul utilizatorului pentru care se doreste obtinerea fisierelor
     * @return Lista fisierelor pentru utilizatorul specificat
     */
    public String[] GetUserFilesById(String userId){
        return this.userFiles.get(userId);
    }

    /**
     * Functie care returneaza lista utilizatorilor care au fisiere stocate pe acest nod
     * @return Lista utilizatorilor
     */
    public String[] GetUsers(){
        return (String[])this.userFiles.keySet().toArray();
    }

    /**
     * Setter pentru adresa nodului curent
     * @param address Adresa nodului
     */
    public void SetNodeAddress(String address){
        this.address = address;
    }

    /**
     * Getter pentru adresa nodului curent
     * @return Adresa nodului
     */
    public String GetNodeAddress(){
        return this.address;
    }


}
