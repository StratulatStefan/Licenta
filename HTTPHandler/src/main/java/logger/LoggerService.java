package logger;

import config.AppConfig;
import http.HttpConnectionService;
import log.ProfiPrinter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class LoggerService {
    private static String loggerServerPath = AppConfig.getParam("loggerHTTPAddress");
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
            ProfiPrinter.PrintException("Eroare la logging.");
        }
    }

    public static void registerSuccess(String nodeAddress, String messageDescription){
        System.out.println(messageDescription);
        log(nodeAddress, messageDescription, LogMsgType.SUCCESS);
    }

    public static void registerWarning(String nodeAddress, String messageDescription){
        ProfiPrinter.PrintException(messageDescription);
        log(nodeAddress, messageDescription, LogMsgType.WARNING);
    }

    public static void registerError(String nodeAddress, String messageDescription){
        ProfiPrinter.PrintException(messageDescription);
        log(nodeAddress, messageDescription, LogMsgType.ERROR);
    }
}
