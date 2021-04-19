package node_manager;
import java.io.Serializable;

/**
 * Clasa folosita pentru a incapsula cererile dintre managerul general si nodul intern.
 * Obiectul care va instantia aceasta clasa va fi trimis prin retea, deci va trebui sa fie serializabil.
 * Clasa cuprinde atributele de baza, corespunzatoare oricarei cereri dintre managerul general si nodul intern.
 * **/
public class EditRequest implements Serializable {
    /** -------- Atribute -------- **/
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

    public EditRequest(String user, String filename) {
        this.userId = user;
        this.filename = filename;
    }


    /** -------- Gettere & Settere -------- **/
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
