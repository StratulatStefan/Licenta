package client_manager.data;

/**
 * <ul>
 *  <li>Clasa folosita pentru a reprezenta cererea de adaugare a unui fisier, trimisa de la client la managerul general.</li>
 *  <li>Va mosteni clasa ClientRequestManager, care incapsuleaza o cerere dintre client si managerul general.</li>
 *  <li>Pe langa atributele de baza ale unei cereri dintre client si managerul general, avem nevoie si de dimensiunea fisierului si
 *      factorul de replicare.</li>
 * </ul>
 * **/
public class NewFileRequest extends ClientManagerRequest {
    /**
     * Dimensiunea fisierului
     */
    private long filesize;
    /**
     * Tipul utilizatorului.
     */
    private String userType;
    /**
     * CRC-ul fisierului
     */
    private long crc;


    /**
     * Constructor vid
     */
    public NewFileRequest(){
        super();
    }


    /**
     * Setter pentru dimensiunea fisierului.
     */
    public void setFilesize(long filesize){
        this.filesize = filesize;
    }
    /**
     * Getter pentru dimensiunea fisierului.
     */
    public long getFilesize() {
        return filesize;
    }

    /**
     * Setter pentru tipul utilizatorului
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }
    /**
     * Getter pentru tipul utilizatorului
     */
    public String getUserType() {
        return userType;
    }

    /**
     * Setter pentru CRC
     */
    public void setCrc(long crc) {
        this.crc = crc;
    }
    /**
     * Getter pentru CRC
     */
    public long getCrc() {
        return crc;
    }
}
