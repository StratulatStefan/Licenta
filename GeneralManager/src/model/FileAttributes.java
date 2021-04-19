package model;

/**
 * Clasa care contine atributele unui fisier
 */
public class FileAttributes{
    /** -------- Atribute -------- **/
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

    private FileVersionData fileVersionData = new FileVersionData();

    /** -------- Constructor -------- **/
    public FileAttributes(String filename, int replication_factor, String status, long crc, String versionNo){
        this.filename = filename;
        this.replication_factor = replication_factor;
        this.status = status;
        this.fileVersionData = new FileVersionData(crc, versionNo);
    }

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

    public void setVersionNo(String versionNo) {
        this.fileVersionData.setVersionNo(versionNo);
    }

    public String getVersionNo() {
        return this.fileVersionData.getVersionNo();
    }

    /** -------- Functii de baza, supraincarcate -------- **/
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(filename).append(" ");
        stringBuilder.append("[repl. : ").append(replication_factor).append("] ");
        stringBuilder.append("[CRC : ").append(Long.toHexString(this.fileVersionData.getCrc())).append(" | ");
        stringBuilder.append("VersionNo : ").append(this.fileVersionData.getVersionNo()).append("] ");
        stringBuilder.append("[Status : ").append(status).append("]\n");
        return stringBuilder.toString();
    }
}
