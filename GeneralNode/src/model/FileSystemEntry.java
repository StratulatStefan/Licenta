package model;

/**
 * <ul>
 * 	<li>Clasa care descrie caracteristicile de baza ale unui fisier, din punct de vedere al modului in care este privit de catre nodul intern.</li>
 * </ul>
 */
public class FileSystemEntry{
    /**
     * Identificatorul unic al utilizatorului.
     */
    private String userId;
    /**
     * Numele fisierului
     */
    private String filename;
    /**
     * Suma de control a fisierului.
     */
    private long crc;

    /**
     * Constructorul clasei, care initializeaza cei trei membri pe baza parametrilor.
     */
    public FileSystemEntry(String userId, String filename, long crc){
        this.userId = userId;
        this.filename = filename;
        this.crc = crc;
    }

    /**
     * Setter pentru suma de control
     */
    public void setCrc(long crc) {
        this.crc = crc;
    }
    /**
     * Getter pentru suma de control
     */
    public long getCrc() {
        return crc;
    }

    /**
     * Setter pentru numele fisierului.
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }
    /**
     * Getter pentru numele fisierului.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Getter pentru identificatorul unic al uitilzatorului.
     */
    public String getUserId() {
        return userId;
    }
    /**
     * Setter pentru identificatorul unic al uitilzatorului.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
}

