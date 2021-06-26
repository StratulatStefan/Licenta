package client_manager.data;

import java.io.Serializable;

/**
 * <ul>
 *  <li>Clasa folosita pentru a reprezenta cererile dintre client si managerul general.</li>
 *  <li>Obiectul care va instantia aceasta clasa va fi trimis prin retea, deci va trebui sa fie serializabil.</li>
 *  <li>Clasa cuprinde atributele de baza, corespunzatoare oricarei cereri dintre client si managerul general.</li>
 * </ul>
 * **/
public class ClientManagerRequest implements Serializable {
    /**
     * Id-ul utilizatorului.
     */
    private String userId;
    /**
     * Fisierul care se va prelucra.
     */
    private String filename;
    /**
     * Descrierea request-ului; Se foloseste la versionare
     */
    private String description;


    /**
     * Constructor vid
     */
    public ClientManagerRequest(){}


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
     * Setter pentru descrierea request-ului
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * Getter pentru descrierea request-ului
     */
    public String getDescription() {
        return description;
    }
}
