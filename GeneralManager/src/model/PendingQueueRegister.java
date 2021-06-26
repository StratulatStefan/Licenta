package model;

import data.Time;

/**
 * Clasa care va descrie caracteristicile elementelor ce vor fi stocate in coada de asteptare.
 */
public class PendingQueueRegister {
    /**
     * Identificatorul unic al utilizatorului.
     */
    private String userId;
    /**
     * Numele fisierului
     */
    private String filename;
    /**
     * Momentul de timp la care elementul se va adauga in coada.
     */
    private long timestamp;


    /**
     * Constructorul cu parametri, care va initializa membrii clasei
     */
    public PendingQueueRegister(String userId, String filename){
        this.userId = userId;
        this.filename = filename;
        this.timestamp = Time.getCurrentTimestamp();
    }


    /**
     * Getter pentru numele fisierului.
     */
    public String getFilename() {
        return filename;
    }
    /**
     * Setter pentru numele fisierului.
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Getter pentru identificatorul utilizatorului.
     */
    public String getUserId() {
        return userId;
    }
    /**
     * Setter pentru identificatorul utilizatorului.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter pentru momentul de timp.
     */
    public long getTimestamp() {
        return timestamp;
    }
    /**
     * Setter pentru momentul de timp.
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
