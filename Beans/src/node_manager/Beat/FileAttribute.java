package node_manager.Beat;

import java.io.Serializable;

/**
 * Clasa care inglobeaza atributele unui fisier.
 * Obiectul ce inglobeaza atributele fisierului va fi trimis prin retea, deci
 * va trebui sa fie serializabil.
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
    /**
     * Numarul versiunii fisierului.
     */
    private String versionNo;

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

    /**
     * Getter pentru numarul versiunii
     */
    public String getVersionNo() {
        return versionNo;
    }
    /**
     * Setter pentru numarul versiunii
     */
    public void setVersionNo(String versionNo) {
        this.versionNo = versionNo;
    }
}