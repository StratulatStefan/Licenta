package log;

public class ProfiPrinter {
    private final static String ANSI_RED = "\u001B[31m";
    private final static String ANSI_RESET = "\u001B[0m";

    public static void PrintException(String message){
        System.out.println(ANSI_RED + message + ANSI_RESET);
    }
}
