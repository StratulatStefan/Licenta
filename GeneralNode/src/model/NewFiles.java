package model;

import java.util.ArrayList;
import java.util.List;

class FileEntry{
    private String userId;
    private String filename;

    public FileEntry(String userId, String filename){
        this.userId = userId;
        this.filename = filename;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}

public class NewFiles {
    private final List<FileEntry> fileEntries = new ArrayList<>();

    public void addNewFile(String userId, String filename){
        synchronized (this.fileEntries){
            if(!containsRegister(userId, filename))
                this.fileEntries.add(new FileEntry(userId, filename));
        }
    }

    public boolean containsRegister(String userId, String filename){
        synchronized (this.fileEntries){
            for(FileEntry fileEntry : this.fileEntries){
                if(fileEntry.getUserId().equals(userId) && fileEntry.getFilename().equals(filename)){
                    return true;
                }
            }
            return false;
        }
    }

    public void clean(){
        synchronized (this.fileEntries){
            this.fileEntries.clear();
        }
    }
}
