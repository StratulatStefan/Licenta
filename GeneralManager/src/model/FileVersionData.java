package model;

import java.io.Serializable;

public class FileVersionData implements Serializable {
    private long crc;
    private String versionNo;
    private String versionDescription;

    public FileVersionData(){}

    public FileVersionData(long crc, String versionNo, String versionDescription){
        this.crc = crc;
        this.versionNo = versionNo;
        this.versionDescription = versionDescription;
    }

    public String getVersionNo() {
        return versionNo;
    }
    public void setVersionNo(String versionNo) {
        this.versionNo = versionNo;
    }

    public String getVersionDescription() {
        return versionDescription;
    }
    public void setVersionDescription(String versionDescription) {
        this.versionDescription = versionDescription;
    }

    public long getCrc() {
        return crc;
    }
    public void setCrc(long crc) {
        this.crc = crc;
    }
}