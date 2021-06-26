package node_manager.Beat;

import java.io.Serializable;

/**
 * <ul>
 *  <li>Clasa care contine atributele unui fisier.</li>
 *  <li>Reprezinta cererea ce va fi trimisa de la nodurile interne catre nodul general</li>
 *  <li>Va contine date despre un fisier al utilizatorului</li>
 *  <li>Obiectul ce inglobeaza atributele fisierului va fi trimis prin retea, deci
 *      va trebui sa fie serializabil.</li>
 * </ul>
 */
public class FileAttribute implements Serializable {
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
    /**
     * Dimensiunea fisierului.
     */
    private long filesize;


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

    /**
     * Getter pentru dimensiunea fisierului.
     */
    public long getFilesize() {
        return filesize;
    }
    /**
     * Setter pentru dimensiunea fisierului.
     */
    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }
}