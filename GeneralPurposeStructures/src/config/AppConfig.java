package config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * <ul>
 * 	<li> Clasa de configurare a aplicatiei.</li>
 * 	<li> Principalul scop este de a incarca parametrii de configurare ai aplicatiei <strong>port-uri, adrese, etc</strong> Nu va fi instantiata, se doreste un acces unic si global.</li>
 * 	<li> Asadar, toate metodele vor fi statice.</li>
 * </ul>
 */
public class AppConfig {
    /**
     * <ul>
     * 	<li> Dictionarul in care se vor incarca parametrii de configurare.</li>
     * 	<li> Avem nevoie de o singura instanta, deci trebuie sa fiei static.</li>
     * 	<li> Cheie : numele atributului.</li>
     * 	<li> Valoare : valoaera atributului.</li>
     * </ul>
     */
    private static HashMap<String, String> appConfigData = new HashMap<String, String>();

    /**
     * <ul>
     * 	<li> Functia de citire a parametrilor de configurare.</li>
     * 	<li> Parcurge fisierul de configurare si extrage toti parametrii.</li>
     * 	<li> Ignora liniile goale.</li>
     * 	<li> Acceseaza si prelucreaza obiectul cu parametrii de configurare, deci va fi statica.</li>
     * </ul>
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

    /**
     * <ul>
     * 	<li> Getter pentru valoarea unui parametru.</li>
     * 	<li>  Daca fisierul de configurare nu este citit, se va citi inainte.</li>
     * </ul>
     * @param paramName Parametrul cautat
     * @return Valoarea parametrului cautat
     */
    public static String getParam(String paramName){
        if(!appConfigData.containsKey(paramName))
            readConfig();
        return appConfigData.get(paramName);
    }
}
