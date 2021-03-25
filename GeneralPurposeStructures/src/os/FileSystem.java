package os;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Clasa care adreseaza unele functionalitati disponibile la nivelul sistemului de operare.
 */
public class FileSystem {
    /**
     * Functie care verifica daca un fisier/director exista.
     * @param path Calea catre fisierul cautat.
     * @return True daca fisierul exista, False in caz contrar.
     */
    public static boolean checkFileExistance(String path){
        return Files.exists(Paths.get(path));
    }

    /**
     * Functie care creeaza un director.
     * @param path Calea noului director.
     * @throws IOException Exceptie generata daca directorul exista deja.
     */
    public static void createDir(String path) throws IOException {
        Files.createDirectories(Paths.get(path ));
    }

    /**
     * Functie care determina continutul unui director (fisiere si alte directoare)
     * @param path Calea catre directorul cautat.s
     * @return Continutul directorului sau null, in cazul in care acesta nu exista.
     */
    public static String[] getDirContent(String path){
        if(!checkFileExistance(path))
            return null;
        File directory = new File(path);
        return directory.list();
    }

    public static void deleteFile(String filepath){
        File file = new File(filepath);
        if(file.delete()){
            System.out.println("Fisierul a fost eliminat cu succes!");
        }
        else{
            System.out.println("Fisierul nu poate fi eliminat");
        }
    }

    public static void renameFile(String prevName, String newName){
        if(checkFileExistance(newName)){
           System.out.println("Nu se poate redenumi fisierul! Exista deja un fisier cu noul nume!");
           return;
        }

        try {
            Files.move(Paths.get(prevName), Paths.get(newName));
        }
        catch (IOException e){
            System.out.println("Exceptie la redenumirea fisierului");
        }
    }

}
