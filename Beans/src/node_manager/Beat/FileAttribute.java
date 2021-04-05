package node_manager.Beat;

import java.io.Serializable;

/**
 * Clasa care inglobeaza atributele unui fisier
 */
public class FileAttribute implements Serializable {
    /** -------- Atribute -------- **/
    /**
     * Numele fisierului
     */
    private String filename;
    /**
     * CRC
     */
    private long crc;

    /** -------- Gettere & Settere -------- **/
    /**
     * Getter pentru numele fisierului
     */
    public String getFilename() {
        return filename;
    }
    /**
     * Setter pentru numele fisierului
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Getter pentru CRC
     */
    public long getCrc() {
        return crc;
    }
    /**
     * Setter pentru CRC
     */
    public void setCrc(long crc) {
        this.crc = crc;
    }
}