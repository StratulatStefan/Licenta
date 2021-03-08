package os;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileSystem {
    public static boolean CheckFileExistance(String path){
        return Files.exists(Paths.get(path));
    }

    public static void CreateDir(String path) throws IOException {
        Files.createDirectories(Paths.get(path ));
    }

    public static String[] GetDirContent(String path){
        if(!CheckFileExistance(path))
            return null;
        File directory = new File(path);
        return directory.list();
    }
}
