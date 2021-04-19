package model;

import java.util.ArrayList;
import java.util.List;

public class FileSystemEntry{
    private String userId;
    private String filename;
    private long crc;

    public FileSystemEntry(String userId, String filename, long crc){
        this.userId = userId;
        this.filename = filename;
        this.crc = crc;
    }

    public void setCrc(long crc) {
        this.crc = crc;
    }

    public long getCrc() {
        return crc;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

