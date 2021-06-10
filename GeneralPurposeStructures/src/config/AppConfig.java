package config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Clasa de configurare a aplicatiei;
 * Principalul scop este de a incarca parametrii de configurare ai aplicatiei (port-uri, adrese, etc)
 * Nu va fi instantiata, se doreste un acces unic si global; Asadar, toate metodele vor fi statice
 */
public class AppConfig {
    /** -------- Atribute -------- **/
    /**
     * Dictionarul in care se vor incarca parametrii de configurare.
     * Avem nevoie de o singura instanta, deci trebuie sa fiei static.
     * Cheie : numele atributului
     * Valoare : valoaera atributului
     */
    private static HashMap<String, String> appConfigData = new HashMap<String, String>();


    /** -------- Functia de configurare -------- **/
    /**
     * Functia de citire a parametrilor de configurare;
     * Parcurge fisierul de configurare si extrage toti parametrii;
     * Ignora liniile goale.
     * Acceseaza si prelucreaza obiectul cu parametrii de configurare, deci va fi statica.
     */
    public static void readConfig(){
        BufferedReader reader;
        try{
            reader = new BufferedReader(new FileReader("safestorage.config"));
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
            System.out.println("Eroare la citirea fisierului de config : " + exception.getMessage());
        }
    }


    /** -------- Getter -------- **/
    /**
     * Getter pentru valoarea unui parametru.
     * @param paramName Parametrul cautat
     * @return Valoarea parametrului cautat
     */
    public static String getParam(String paramName){
        if(!appConfigData.containsKey(paramName))
            readConfig();
        return appConfigData.get(paramName);
    }
}
