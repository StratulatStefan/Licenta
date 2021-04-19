package model;

public class FileVersionData{
    private long crc;
    private String versionNo;

    public FileVersionData(){}

    public FileVersionData(long crc, String versionNo){
        this.crc = crc;
        this.versionNo = versionNo;
    }

    public String getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(String versionNo) {
        this.versionNo = versionNo;
    }

    public long getCrc() {
        return crc;
    }

    public void setCrc(long crc) {
        this.crc = crc;
    }
}