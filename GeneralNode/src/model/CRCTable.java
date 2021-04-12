package model;

import java.util.ArrayList;
import java.util.List;

class FileSystemEntry{
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

public class CRCTable {
    private final List<FileSystemEntry> fileSystemEntryList = new ArrayList<>();


    public void resetRegister(String userId, String filename){
        synchronized (this.fileSystemEntryList) {
            for (FileSystemEntry fileSystemEntry : fileSystemEntryList) {
                if(fileSystemEntry.getUserId().equals(userId) && fileSystemEntry.getFilename().equals(filename)){
                    fileSystemEntry.setCrc(-1);
                    return;
                }
            }
        }
    }

    public long getCrcForFile(String userId, String filename){
        synchronized (this.fileSystemEntryList) {
            for (FileSystemEntry fileSystemEntry : fileSystemEntryList) {
                if(fileSystemEntry.getUserId().equals(userId) && fileSystemEntry.getFilename().equals(filename)){
                    return fileSystemEntry.getCrc();
                }
            }
            return -1;
        }
    }

    public void updateRegister(String userId, String filename, long crc){
        synchronized (this.fileSystemEntryList) {
            for (FileSystemEntry fileSystemEntry : fileSystemEntryList) {
                if(fileSystemEntry.getUserId().equals(userId) && fileSystemEntry.getFilename().equals(filename)){
                    fileSystemEntry.setCrc(crc);
                    return;
                }
            }
            this.fileSystemEntryList.add(new FileSystemEntry(userId, filename, crc));
        }
    }
}
