import config.AppConfig;
import data.Time;
import logger.LoggerService;
import model.VersionData;
import os.FileSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VersionControlManager {
    private static String baseFilepath;

    private final static String extension = AppConfig.getParam("metadataExtension");
    private static String storagePath = AppConfig.getParam("storagePath");

    public VersionControlManager(String address){
        baseFilepath = storagePath + address + "\\";
    }

    public void registerFileVersion(String userId, String filename, long crc, String description){
        String filepath = baseFilepath + userId + "\\" + filename;
        if(!FileSystem.checkFileExistance(filepath)){
            LoggerService.registerError(GeneralNode.ipAddress,"Fisierul " + filename + " nu a fost salvat inca; Nu putem scrie fisierul de metadate");
            return;
        }
        String metadataFilePath = filepath.substring(0, filepath.lastIndexOf(".")) + extension;
        VersionData versionData = new VersionData();
        if(!FileSystem.checkFileExistance(metadataFilePath)){
            FileSystem.createFile(metadataFilePath);
            versionData.setVersionNumber(1);
            versionData.setHash(crc);
            LoggerService.registerSuccess(GeneralNode.ipAddress,"Inregistram prima versiune a fisierului " + filename);
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
            LoggerService.registerSuccess(GeneralNode.ipAddress,"Inregistram o noua versiune a fisierului " + filename);
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
            LoggerService.registerError(GeneralNode.ipAddress,"Eroare la extragerea continutului fisierului de metadate " + metadataFilePath);
            return null;
        }
    }

    public VersionData getLastVersionOfFile(String userId, String filename){
        String filepath = baseFilepath + userId + "\\" + filename;
        String metadataFilePath = filepath.substring(0, filepath.lastIndexOf(".")) + extension;
        return getLastVersionOfFile(metadataFilePath);
    }

    public List<VersionData> getVersionsForFile(String userId, String filename) throws IOException {
        String filepath = baseFilepath + userId + "\\" + filename;
        String metadataFilePath = filepath.substring(0, filepath.lastIndexOf(".")) + extension;
        List<VersionData> versionData = new ArrayList<VersionData>();
        for(String versionLine : FileSystem.getFileLines(metadataFilePath)){
            versionData.add(new VersionData(versionLine));
        }
        return versionData;
    }

}
