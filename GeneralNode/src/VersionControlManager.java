import data.Time;
import log.ProfiPrinter;
import model.VersionData;
import os.FileSystem;

import java.io.IOException;
import java.util.List;
import java.io.File;

public class VersionControlManager {
    private static String baseFilepath;

    // fisierul de config asta
    private final static String extension = ".metadata";


    public VersionControlManager(String address){
        baseFilepath = String.format("D:\\Facultate\\Licenta\\Storage\\%s\\", address);
    }


    public void registerFileVersion(String userId, String filename, long crc, String description){
        String filepath = baseFilepath + userId + "\\" + filename;
        if(!FileSystem.checkFileExistance(filepath)){
            ProfiPrinter.PrintException("Fisierul " + filename + " nu a fost salvat inca; Nu putem scrie fisierul de metadate");
            return;
        }
        String metadataFilePath = filepath.substring(0, filepath.lastIndexOf(".")) + extension;
        VersionData versionData = new VersionData();
        if(!FileSystem.checkFileExistance(metadataFilePath)){
            FileSystem.createFile(metadataFilePath);
            versionData.setVersionNumber(1);
            versionData.setHash(crc);
            System.out.println("Inregistram prima versiune a fisierului " + filename);
        }
        else{
            VersionData lastVersionOfFile = getLastVersionOfFile(metadataFilePath);
            versionData.setVersionNumber(lastVersionOfFile.getVersionNumber() + 1);
            if(crc == -1){
                versionData.setHash(lastVersionOfFile.getHash());
            }
            else{
                versionData.setHash(crc);
            }
            System.out.println("Inregistram o noua versiune a fisierului " + filename);
        }
        versionData.setTimestamp(Time.getCurrentTimeWithFormat());
        versionData.setDescription(description);
        FileSystem.appendToFile(metadataFilePath, versionData.toString() + "\n");
    }


    public VersionData getLastVersionOfFile(String metadataFilePath){
        try {
            List<String> metadataContent = FileSystem.getFileLines(metadataFilePath);
            String lastRegister = metadataContent.get(metadataContent.size() - 1);
            return new VersionData(lastRegister);

        }
        catch (IOException exception){
            ProfiPrinter.PrintException("Eroare la extragerea continutului fisierului de metadate " + metadataFilePath);
            return null;
        }
    }

    public VersionData getLastVersionOfFile(String userId, String filename){
        String filepath = baseFilepath + userId + "\\" + filename;
        String metadataFilePath = filepath.substring(0, filepath.lastIndexOf(".")) + extension;
        return getLastVersionOfFile(metadataFilePath);
    }

}
