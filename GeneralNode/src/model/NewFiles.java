package model;

import java.util.ArrayList;
import java.util.List;


public class NewFiles {
    private final List<FileSystemEntry> fileEntries = new ArrayList<>();

    public void addNewFile(String userId, String filename){
        synchronized (this.fileEntries){
            if(!containsRegister(userId, filename))
                this.fileEntries.add(new FileSystemEntry(userId, filename, -1));
        }
    }

    public boolean containsRegister(String userId, String filename){
        synchronized (this.fileEntries){
            for(FileSystemEntry fileEntry : this.fileEntries){
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
