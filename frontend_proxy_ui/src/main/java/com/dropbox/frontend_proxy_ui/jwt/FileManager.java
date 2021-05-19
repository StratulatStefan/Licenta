package com.dropbox.frontend_proxy_ui.jwt;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Nu este folosit !
 */
public class FileManager {
    public static String generateRandomFileName(){
        StringBuilder sBuilder = new StringBuilder();
        Random rand = new Random();
        for(int i = 0; i < 5; i++)
            sBuilder.append((char)('a' + rand.nextInt('z' - 'a')));
        sBuilder.append(".txt");
        return sBuilder.toString();
    }

    public static void writeToFile(String userData, String fileName){
        try {
            Path path = Paths.get("generated_users/");
            Files.createDirectories(path);

            FileWriter fileWriter = new FileWriter("generated_users/" + fileName);
            fileWriter.append(userData);
            fileWriter.close();
            System.out.println("User successfully written to file \"" + fileName + "\"!");
        }
        catch (IOException exception){
            System.out.println("Error occured while trying to handle current user (Writing to file)!");
        }
    }
}
