package tables;

import model.FileSystemEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * <ul>
 * 	<li>Clasa care va defini suportul asincron pentru salvarea sumelor de control calculate in urma cererii nodului general.</li>
 * 	<li> Se vor asigura mecanisme de sincronizare, intrucat aceste actualizari efectuate la terminarea calculului se vor face in mod concurent.</li>
 * </ul>
 */
public class CRCTable {
    /**
     * <ul>
     * 	<li>Lista in care vor fi salvate inregistrarile fisierelor si ale sumelor de control.</li>
     * </ul>
     */
    private final List<FileSystemEntry> fileSystemEntryList = new ArrayList<>();

    /**
     * <ul>
     * 	<li>Functie de resetare a sumei de control a unui fisier.</li>
     * 	<li> La fiecare heartbeat, nodul intern va trimite si suma de control, care va fi preluata din aceasta lista.</li>
     * 	<li> Ca o conventie realizata intre nodul general si nodul intern, suma de control cu valoarea <strong>-1</strong> nu va fi considerata in verificarea inregritatii fisierului.</li>
     * 	<li> Astfel, imediat dupa trimiterea sumei de control proaspat calculate catre nodul general, inregistrarea va fi resetata.</li>
     * </ul>
     */
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

    /**
     * <ul>
     * 	<li>Functie pentru extragerea valorii sumei de control pentru un anumit fisier.</li>
     * 	<li> Se va returna <strong>-1</strong> daca fisierul nu a fost gasit.</li>
     * </ul>
     */
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

    /**
     * <ul>
     * 	<li>Functie pentru actualizarea unui element.</li>
     * 	<li> Actualizarea presupune schimbarea <strong>sumei de control</strong>.</li>
     * 	<li> Daca elementul nu exista, se va <strong>insera</strong> in lista.</li>
     * </ul>
     */
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
