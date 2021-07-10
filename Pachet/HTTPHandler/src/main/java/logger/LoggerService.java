package logger;

import http.HttpConnectionService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <ul>
 * 	<li>Clasa care va expune functionalitatile necesare efectuarii cererilor de jurnalizare a evenimentelor din sistem.</li>
 * 	<li> Principalul obiectiv este trimiterea de cereri catre API-ul REST, care va realiza comunicarea cu serverul de baze de date si va persista evenimentele.</li>
 * </ul>
 */
public class LoggerService {
    /**
     * URL-ul catre API-ul de tip REST.
     */
    private static String loggerServerPath = "http://localhost:8085/api/log";

    /**
     * Instanta a obiectului care faciliteaza crearea si trimiterea de cereri HTTP.
     */
    private final static HttpConnectionService httpConnectionService = new HttpConnectionService();

    /**
     * <ul>
     * 	<li>Functie care defineste corpul cererii de serializare, prin includerea datelor despre <strong>nodul</strong> la nivelul caruia s-a generat, <strong>tipul</strong> evenimentului si <strong>descrierea</strong> evenimentul.</li>
     * 	<li>  Se trimite cererea catre <strong>API</strong>-ul <strong>REST</strong> folosind metoda HTTP <strong>POST</strong>.</li>
     * </ul>
     * @param nodeAddress Adresa nodului la nivelul caruia s-a generat evenimentul.
     * @param messageDescription Descrierea evenimentului.
     * @param messageType Tipul evenimentului.
     */
    private static void log(String nodeAddress, String messageDescription, LogMsgType messageType){
        Map<String, Object> logData = new HashMap<String, Object>(){{
            put("node_address", nodeAddress);
            put("message_type", messageType.toString());
            put("description", messageDescription);
        }};
        try{
            String logResponse = httpConnectionService.postRequest(loggerServerPath, logData);
        }
        catch (IOException exception) {
            System.out.println("Eroare la logging. : " + exception.getMessage());
        }
    }

    /**
     * <ul>
     * 	<li>Functie care realizeaza jurnalizarea unui eveniment de succes.</li>
     * </ul>
     * @param nodeAddress Adresa nodului la nivelul caruia s-a generat evenimentul.
     * @param messageDescription Descrierea evenimentului.
     */
    public static void registerSuccess(String nodeAddress, String messageDescription){
        System.out.println(messageDescription);
        log(nodeAddress, messageDescription, LogMsgType.SUCCESS);
    }

    /**
     * <ul>
     * 	<li>Functie care realizeaza jurnalizarea unui eveniment de atentionare.</li>
     * </ul>
     * @param nodeAddress Adresa nodului la nivelul caruia s-a generat evenimentul.
     * @param messageDescription Descrierea evenimentului.
     */
    public static void registerWarning(String nodeAddress, String messageDescription){
        System.out.println(messageDescription);
        log(nodeAddress, messageDescription, LogMsgType.WARNING);
    }

    /**
     * <ul>
     * 	<li>Functie care realizeaza jurnalizarea unui eveniment de eroare.</li>
     * </ul>
     * @param nodeAddress Adresa nodului la nivelul caruia s-a generat evenimentul.
     * @param messageDescription Descrierea evenimentului.
     */
    public static void registerError(String nodeAddress, String messageDescription){
        System.out.println(messageDescription);
        log(nodeAddress, messageDescription, LogMsgType.ERROR);
    }
}
