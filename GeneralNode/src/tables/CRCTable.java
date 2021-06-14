package tables;

import model.FileSystemEntry;

import java.util.ArrayList;
import java.util.List;

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
