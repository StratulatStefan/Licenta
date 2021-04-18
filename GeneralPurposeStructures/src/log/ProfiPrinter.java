package log;

/**
 * Clasa pentru log beautify, astfel incat sa se evidentieze/diferentieze tipurile de mesaje
 */
public class ProfiPrinter {
    /** -------- Constante -------- **/
    /**
     * Codul pentru culoare Rosu
     */
    private final static String ANSI_RED = "\u001B[31m";
    /**
     * Codul de reset; se foloseste dupa fiecare folosire a unei culori, astfel incat sa revenim
     * la culoarea implicita folosita la consola.
     */
    private final static String ANSI_RESET = "\u001B[0m";


    /** -------- Metode -------- **/
    /**
     * Afisarea unui mesaj de eroare/exceptie;
     * Va fi evidentiat prin culoarea rosie
     * @param message Mesajul propriu-zis, ce va fi afisat.
     */
    public static void PrintException(String message){
        System.out.println(ANSI_RED + message + ANSI_RESET);
    }
}
