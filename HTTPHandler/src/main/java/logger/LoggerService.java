package logger;

import http.HttpConnectionService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class LoggerService {
    private static String loggerServerPath = "http://localhost:8085/api/log";

    private final static HttpConnectionService httpConnectionService = new HttpConnectionService();

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

    public static void registerSuccess(String nodeAddress, String messageDescription){
        System.out.println(messageDescription);
        log(nodeAddress, messageDescription, LogMsgType.SUCCESS);
    }

    public static void registerWarning(String nodeAddress, String messageDescription){
        System.out.println(messageDescription);
        log(nodeAddress, messageDescription, LogMsgType.WARNING);
    }

    public static void registerError(String nodeAddress, String messageDescription){
        System.out.println(messageDescription);
        log(nodeAddress, messageDescription, LogMsgType.ERROR);
    }
}
