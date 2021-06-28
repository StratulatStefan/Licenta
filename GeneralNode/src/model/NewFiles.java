package model;

import java.util.ArrayList;
import java.util.List;

/**
 * <ul>
 * 	<li>Clasa care defineste lista fisierelor noi aparute in sistem.</li>
 * 	<li> Se va folosi pentru a identifica acele fisiere pentru care suma de control trebuie sa fie calculata imediat,
 *       ci nu la solicitarea nodului general.</li>
 * 	<li> Calcularea imediata a sumei de control se impune pentru a putea inregistra fisierul cu succes.</li>
 * </ul>
 */
public class NewFiles {
    /**
     * <ul>
     * 	<li>Lista de fisiere noi.</li>
     * 	<li> Va contine elemente de tipul <strong>FileSystemEntry</strong>, ce vor descrie atributele de baza ale unui fisier din perspectiva nodului intern.</li>
     * 	<li> Obiectul va fi <strong>final</strong> pentru a se putea asigura mecanismele explicite de sincronizare.</li>
     * </ul>
     */private final List<FileSystemEntry> fileEntries = new ArrayList<>();

    /**
     * <ul>
     * 	<li>Functie pentru adaugarea unui nou fisier in lista.</li>
     * 	<li> Adaugarea se va realiza daca fisierul nu exista deja.</li>
     * </ul>
     */
    public void addNewFile(String userId, String filename){
        synchronized (this.fileEntries){
            if(!containsRegister(userId, filename))
                this.fileEntries.add(new FileSystemEntry(userId, filename, -1));
        }
    }

    /**
     * <ul>
     * 	<li>Functie pentru verificarea unui fisier in lista.</li>
     * 	<li> Verificarea se va realiza pe baza identificatorului utilizatorului si a numelui fisierului</li>
     * </ul>
     */
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

    /**
     * <ul>
     * 	<li>Functie pentru curatarea listei de fisiere noi.</li>
     * 	<li> Curatarea se face in momentul in care fisierele au fost eliminate din memoria de stocare a nodului intern.</li>
     * 	<li> Nu ar trebui sa apara acest comportament.</li>
     * </ul>
     */
    public void clean(){
        synchronized (this.fileEntries){
            this.fileEntries.clear();
        }
    }
}
