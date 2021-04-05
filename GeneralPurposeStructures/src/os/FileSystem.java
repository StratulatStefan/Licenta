package os;

import data.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public static void deleteFile(String filepath){
        File file = new File(filepath);
        if(file.delete()){
            System.out.println("Fisierul a fost eliminat cu succes!");
        }
        else{
            System.out.println("Fisierul nu poate fi eliminat");
        }
    }

    /**
     * Functie care realizeaza redenumirea unui fisier.
     */
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
     * Functie care calculeaza CRC-ul unui fisier
     */
    public static long calculateCRC(String filename){
        final long[] crcValue = new long[]{0};
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run(){
                try {
                    InputStream inputStream = new BufferedInputStream(new FileInputStream(filename));
                    CRC32 crc = new CRC32();
                    int count;
                    byte[] buffer = new byte[1024 * 8];
                    while ((count = inputStream.read(buffer)) != -1)
                        crc.update(buffer, 0, count);
                    inputStream.close();
                    crcValue[0] = crc.getValue();
                }
                catch (IOException exception){
                    System.out.println("IOException la calculateCRC : " + exception.getMessage());
                }
            }
        });
        thread.start();
        try {
            if (thread.isAlive())
                thread.join();
        }
        catch (InterruptedException exception){
            System.out.println("calculateCRC thread interrupt..");
        }
        return crcValue[0];
    }
}
