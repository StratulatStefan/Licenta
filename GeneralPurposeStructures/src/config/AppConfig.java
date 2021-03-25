package config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class AppConfig {
    private static HashMap<String, String> appConfigData = new HashMap<String, String>();

    public static void readConfig(){
        BufferedReader reader;
        try{
            reader = new BufferedReader(new FileReader("D:\\Facultate\\Licenta\\Dropbox\\dropbox.config"));
            String line;
            while((line = reader.readLine()) != null){
                if(!line.equals("")){
                    String[] data = line.split("=");
                    appConfigData.put(data[0], data[1]);
                }
            }
            reader.close();
        }
        catch (IOException exception){
            System.out.println("Eroare la citirea fisierului de config");
        }
    }

    public static String getParam(String paramName){
        return appConfigData.get(paramName);
    }
}
