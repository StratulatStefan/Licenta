package os;

import data.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

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
     * Functie care creeaza un director.
     * @param path Calea noului director.
     */
    public static void createFile(String path){
        try {
            Files.createFile(Paths.get(path));
        }
        catch (IOException exception){
            System.out.println("Nu se poate crea fisierul de metadate " + path);
        }
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

    /**
     * Functie care realizeaza stergerea unui fisier.
     */
    public static int deleteFile(String filepath){
        File file = new File(filepath);
        if(file.delete()){
            System.out.println("Fisierul a fost eliminat cu succes!");
            return 1;
        }
        System.out.println("Fisierul nu poate fi eliminat");
        return 0;
    }

    /**
     * Functie care realizeaza redenumirea unui fisier.
     */
    public static int renameFile(String prevName, String newName){
        if(checkFileExistance(newName)){
            System.out.println("Nu se poate redenumi fisierul! Exista deja un fisier cu noul nume!");
           return 2;
        }

        try {
            Files.move(Paths.get(prevName), Paths.get(newName));
            return 1;
        }
        catch (IOException e){
            System.out.println("Exceptie la redenumirea fisierului");
            return 0;
        }
    }

    /**
     * Functie care determina dimensiunea unui fisier in bytes
     */
    public static long getFileSize(String filepath) throws IOException {
        if(checkFileExistance(filepath)){
            File file = new File(filepath);
            return entitySize(file);
        }
        throw new IOException("File not found!");
    }

    /**
     * Functie care determina dimensiunea in kilobytes a unei entitati a sistemului de fisiere
     * Ne referim la fisiere sau chiar foldere intregi
     * In cazul directoarelor, parcurgerea se face recursiv astfel incat sa analizam tot content-ul
     * @param directory Numele fisierului/directorului a carui dimensiune dorim sa o determinam.
     * @return Dimensiunea in kilobytes.
     */
    public static long entitySize(File directory) {
        if(directory.isFile()){
            return directory.length();
        }
        else if(directory.isDirectory()) {
            int length = 0;
            for (File file : directory.listFiles()) {
                if (file.isFile())
                    length += file.length();
                else
                    length += entitySize(file);
            }
            return length;
        }
        return -1;
    }

    /**
     * Functie care calculeaza CRC-ul unui fisier, avand ca parametru numele fisierului
     */
    public static long calculateCRC(String filename){
        try {
            InputStream inputStream = new BufferedInputStream(new FileInputStream(filename));
            CRC32 crc = new CRC32();
            int count;
            byte[] buffer = new byte[1024 * 8];
            while ((count = inputStream.read(buffer)) != -1)
                crc.update(buffer, 0, count);
            inputStream.close();
            return crc.getValue();
        }
        catch (IOException exception){
            System.out.println("IOException la calculateCRC : " + exception.getMessage());
        }
        return -1;
    }

    /**
     * Functie care calculeaza CRC-ul unui fisier, avand ca parametru stream-ul de date
     */
    public static long calculateCRC(InputStream fileInputStream){
        try {
            InputStream inputStream = new BufferedInputStream(fileInputStream);
            CRC32 crc = new CRC32();
            int count;
            byte[] buffer = new byte[1024 * 8];
            while ((count = inputStream.read(buffer)) != -1)
                crc.update(buffer, 0, count);
            inputStream.close();
            return crc.getValue();
        }
        catch (IOException exception){
            System.out.println("IOException la calculateCRC : " + exception.getMessage());
        }
        return -1;
    }

    /**
     * Functie care returneaza tot continutul unui fisier text; continutul este organizat sub forma de linii.
     * Se ignora liniile goale. ("")
     * @param filepath Calea catre fisierul care se doreste a fi citit.
     * @return Lista de linii cu continutul fisierului.
     * @throws IOException Exceptie generata daca nu putem deschide/inchide fisierul sau reader-ul
     */
    public static List<String> getFileLines(String filepath) throws IOException{
        File file = new File(filepath);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        List<String> fileLines = new ArrayList<>();
        String line;
        while((line = reader.readLine()) != null){
            if(line.equals(""))
                continue;
            fileLines.add(line);
        }
        reader.close();
        return fileLines;
    }

    /**
     * Functie care adayga continut text la un anumit fisier.
     * La final se adauga o noua linie (CRLF)
     * @param filepath Calea care fisierul care va fi moddificat
     * @param content Continutul care va fi adaugat
     */
    public static void appendToFile(String filepath, String content){
        try {
            Path path = Paths.get(filepath);
            Files.write(path, content.getBytes(), StandardOpenOption.APPEND);
        }
        catch (IOException exception){
            System.out.println("Nu se poate modifica fisierul de metadate " + filepath);
        }
    }

    /**
     * Functie care returneaza numele unui fisier cu extensia schimbata extensia.
     * Se poate furniza atat numele fisierului cat si calea absolut catre acel fisier.
     * Se foloseste cand dorim sa obtinem calea catre fisierul de metadate
     * exemplu: lab.py -> lab.metadata
     * @param filepath Numele fisierului sau calea absoluta catre fisier
     * @param newExtension noua extensia a fisierului
     * @return Numele fisierului cu extensia schimbata
     */
    public static String changeFileExtension(String filepath, String newExtension){
        return filepath.substring(0, filepath.lastIndexOf(".")) + newExtension;
    }
}
