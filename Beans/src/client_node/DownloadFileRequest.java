package client_node;

import java.io.Serializable;

/**
 * <ul>
 *  <li>Clasa care va reprezenta o cerere trimisa de la client catre nodurile interne ale sistemului,
 *      prin care se solicita descarcarea unui fisier.</li>
 *  <li>Obiectul ce va instantia aceasta clasa va trebui trimis prin retea de la client la nodurile interne,
 *      motiv pentru care va trebui sa fie serializabil</li>
 * </ul>
 */
public class DownloadFileRequest implements Serializable {
    /**
     * Id-ul utilizatorului care detine fisierul.
     */
    private String userId;
    /**
     * Numele fisierului ce se doreste a fi descarcat.
     */
    private String filename;


    /**
     * Constructor vid
     */
    public DownloadFileRequest(){}


    /**
     * Getter pentru identificatorul utilizatorului.
     */
    public String getUserId() {
        return userId;
    }
    /**
     * Setter pentru identificatorul utilizatorului.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter pentru numele fisierului.
     */
    public String getFilename() {
        return filename;
    }
    /**
     * Setter pentru numele fisierului.
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }
}
