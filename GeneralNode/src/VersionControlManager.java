import config.AppConfig;
import data.Time;
import logger.LoggerService;
import model.VersionData;
import os.FileSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <ul>
 * 	<li>Clasa care realizeaza gestiunea versiunilor fisierului.</li>
 * 	<li> Reprezinta singurul mecanism de acces la fisierele de metadate ce contin versiunile fisierului.</li>
 * </ul>
 */
public class VersionControlManager {
    /**
     * <ul>
     * 	<li>Calea in sistemul de fisiere, unde sunt stocate toate fisierele,
     *      grupate pe directoare a caror nume este determinat de identificatorul unic al fisierului.</li>
     * </ul>
     */
    private static String baseFilepath;
    /**
     * Extensia fisierului de metadate.
     */
    private final static String extension = AppConfig.getParam("metadataExtension");

    /**
     * <ul>
     * 	<li>Constructorul clasei.</li>
     * 	<li> Va crea calea de baza <strong>baseFilePath</strong> pe baza adresei nodului curent.</li>
     * </ul>
     */
    public VersionControlManager(String address){
        baseFilepath = AppConfig.getParam("storagePath") + address + "\\";
    }

    /**
     * <ul>
     * 	<li> Inregistrarea unei noi versiuni a unui fisier.</li>
     * 	<li> Sunt furnizate toate datele de identificare a fisierului si datele specifice noii versiuni, precum <strong>description</strong>.</li>
     * 	<li> Inregistrarea noii versiuni presupune scrierea acesteia in fisierul de metadate specific fisierului subiect.</li>
     * 	<li> Numarul versiunii va fi ales pe baza ultimei versiuni a fisierului <strong>nr.</li>
     * 	<li> ultimei versiuni + 1</strong>.</li>
     * 	<li> Se vor include si date temporale.</li>
     * </ul>
     */
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

    /**
     * <ul>
     * 	<li>Extragerea ultimei versiuni a unui fisier.</li>
     * 	<li> Se furnizeaza calea catre fisierul de metadate si este extrasa ultima linie din fisier.</li>
     * 	<li> Dupa extragere linia este parsata pentru a se obtine fiecare token.</li>
     * </ul>
     */
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

    /**
     * <ul>
     * 	<li>Extragerea ultimei versiuni a unui fisier.</li>
     * 	<li> Se furnizeaza identificatorul unic al utilizatorului si numele fisierului, pe baza carora se va construi calea catre fisier.</li>
     * 	<li> Ulterior, se va apela functia <strong>getLastVersionOfFile</strong> care are semnatura diferita.</li>
     * </ul>
     */
    public VersionData getLastVersionOfFile(String userId, String filename){
        String filepath = baseFilepath + userId + "\\" + filename;
        String metadataFilePath = filepath.substring(0, filepath.lastIndexOf(".")) + extension;
        return getLastVersionOfFile(metadataFilePath);
    }

    /**
     * <ul>
     * 	<li>Extragerea tuturor versiunilor unui fisier.</li>
     * 	<li> Se furnizeaza identificatorul unic al utilizatorului si numele fisierului.</li>
     * 	<li> Se creeaza calea catre fisierul de <strong>metadate</strong>.</li>
     * 	<li> Se extrage fiecare linie din fisiere, reprezentand cate o <strong>versiuni</strong>.</li>
     * 	<li> Se vor returna sub forma unei liste.</li>
     * </ul>
     */
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
