package client_manager.data;

import java.io.Serializable;

/**
 * Clasa folosita pentru a incapsula cererile dintre client si managerul general.
 * Obiectul care va instantia aceasta clasa va fi trimis prin retea, deci va trebui sa fie serializabil.
 * Clasa cuprinde atributele de baza, corespunzatoare oricarei cereri dintre client si managerul general.
 * **/
public class ClientManagerRequest implements Serializable {
    /** -------- Atribute -------- **/
    /**
     * Id-ul utilizatorului.
     */
    private String userId;
    /**
     * Fisierul care se va prelucra.
     */
    private String filename;

    private String description;


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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
