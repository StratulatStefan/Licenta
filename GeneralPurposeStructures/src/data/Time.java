package data;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Clasa folosita pentru calcule/reprezentari cu timpi
 */
public class Time {
    /** -------- Atribute -------- **/
    /**
     * Timestamp-ul la care ne raportam, astfel incat sa nu obtinem o valoare foarte mare.
     */
    private static Timestamp baseTimestamp = Timestamp.valueOf("2021-01-01 00:00:00");

    /**
     * Timestamp-ul curent
     */
    private static Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

    /**
     * Timpul curent, formatat.
     */
    private static SimpleDateFormat formattedTime = new SimpleDateFormat("dd-MM-yyyy | hh:mm:ss");


    /** -------- Gettere -------- **/
    /**
     * Functie care returneaza timestamp-ul curent, raportat la timestampul de baza al clasei
     */
    public static long getCurrentTimestamp(){
        currentTimestamp.setTime(System.currentTimeMillis());
        return (currentTimestamp.getTime() - baseTimestamp.getTime()) / 1000;
    }

    /**
     * Functie care returneaza timpul in format natural (data si ora)
     * @return Timpul formatat
     */
    public static String getCurrentTimeWithFormat(){
        return "[" + formattedTime.format(getCurrentTimestamp() * 1000 + baseTimestamp.getTime()) + "]";
    }
}
