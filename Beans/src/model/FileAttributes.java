package model;

import java.io.Serializable;

/**
 * Clasa care contine atributele unui fisier.
 */
public class FileAttributes implements Serializable {
    private String userId;
    /**
     * Numele fisierului
     */
    private String filename;
    /**
     * Factorul de replicare
     */
    private int replication_factor;
    /**
     * Statusul fisierului
     */
    private String status;
    /**
     * Dimensiunea fisierului.
     */
    private long fileSize;
    /**
     * Obiectul ce contine toate datele despre versiunea unui fisier.
     */
    private FileVersionData fileVersionData;


    /**
     * Constructor vid
     */
    public FileAttributes(){}
    /**
     * <ul>
     *  <li>Constructor cu argumente</li>
     *  <li>Va instantia fiecare membru al clasei in functie de parametri.</li>
     *  <li>In cazul datelor despre versiune, se va crea un nou obiect de tipul <strong>FileVersionData</strong></li>
     * </ul>
     */
    public FileAttributes(String filename, int replication_factor, String status, long crc, long filesize, String versionNo, String versionDesc) {
        this.filename = filename;
        this.replication_factor = replication_factor;
        this.status = status;
        this.fileSize = filesize;
        this.fileVersionData = new FileVersionData(crc, versionNo, versionDesc, filesize);
    }


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
     * Getter pentru factorul de replicare
     */
    public int getReplication_factor() {
        return replication_factor;
    }
    /**
     * Setter pentru factorul de replicare
     */
    public void setReplication_factor(int replication_factor) {
        this.replication_factor = replication_factor;
    }

    /**
     * Getter pentru statusul fisierului
     */
    public String getStatus() {
        return status;
    }
    /**
     * Setter pentru statusul fisierului
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Getter pentru CRC
     */
    public long getCrc() {
        return this.fileVersionData.getCrc();
    }
    /**
     * Setter pentru CRC
     */
    public void setCrc(long crc) {
        this.fileVersionData.setCrc(crc);
    }

    /**
     * Setter pentru numarul versiunii fisierului.
     */
    public void setVersionNo(String versionNo) {
        this.fileVersionData.setVersionNo(versionNo);
    }
    /**
     * Getter pentru numarul versiunii.
     */
    public String getVersionNo() {
        return this.fileVersionData.getVersionNo();
    }

    /**
     * Setter pentru descrierea versiunii.
     */
    public void setVersionDescription(String versionDescription){ this.fileVersionData.setVersionDescription(versionDescription);}
    /**
     * Getter pentru descrierea versiunii.
     */
    public String getVersionDescription(){ return this.fileVersionData.getVersionDescription();}

    /**
     * Getter pentru dimensiunea fisierului. [Bytes]
     */
    public long getFileSize() {
        return fileSize;
    }
    /**
     * Setter pentru dimensiunea fisierului. [Bytes]
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

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


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(filename).append(" ");
        stringBuilder.append("[repl. : ").append(replication_factor).append("] ");
        stringBuilder.append("[CRC : ").append(Long.toHexString(this.fileVersionData.getCrc())).append(" | ");
        stringBuilder.append("VersionNo : ").append(this.fileVersionData.getVersionNo()).append("] ");
        stringBuilder.append("[Size : ").append(fileSize).append("] ");
        stringBuilder.append("[Status : ").append(status).append("]\n");
        return stringBuilder.toString();
    }
}
