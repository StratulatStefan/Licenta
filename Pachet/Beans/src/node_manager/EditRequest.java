package node_manager;
import java.io.Serializable;

/**
 * <ul>
 *  <li>Clasa folosita pentru a reprezenta cererile dintre managerul general si nodul intern.</li>
 *  <li>Obiectul care va instantia aceasta clasa va fi trimis prin retea, deci va trebui sa fie serializabil.</li>
 *  <li>Clasa cuprinde atributele de baza, corespunzatoare oricarei cereri dintre managerul general si nodul intern.</li>
 * </ul>
 * **/
public class EditRequest implements Serializable {
    /**
     * Id-ul utilizatorului.
     */
    private String userId;
    /**
     * Fisierul care se va prelucra.
     */
    private String filename;
    /**
     * Descrierea cererii de editare; Folosita la versionare
     */
    private String description;


    /**
     * Constructorul vid
     */
    public EditRequest(){}
    /**
     * <ul>
     * 	<li>Constructorul clasei.</li>
     * 	<li> Instantiaza valorile pentru id-ul utilizatorului si pentru numele fisierului.</li>
     * </ul>
     * @param user Id-ul utilizatoului
     * @param filename Numele fisierului utilizatorului
     */
    public EditRequest(String user, String filename) {
        this.userId = user;
        this.filename = filename;
    }


    /**
     * Setter pentru numele fisierului
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }
    /**
     * Getter pentru numele fisierului
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Setter pentru id-ul utilizatorului
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    /**
     * Getter pentru id-ul utilizatorului.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Getter pentru descrierea cererii
     */
    public String getDescription() {
        return description;
    }
    /**
     * Setter pentru descrierea cererii
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
