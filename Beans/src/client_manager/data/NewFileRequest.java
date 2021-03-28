package client_manager.data;

/**
 * Clasa folosita pentru a incapsula cererea de adaugare a unui fisier, trimisa de la client la managerul general.
 * Va mosteni clasa ClientRequestManager, care incapsuleaza o cerere dintre client si managerul general.
 * Pe langa atributele de baza ale unei cereri dintre client si managerul general, avem nevoie si de dimensiunea fisierului si
 * factorul de replicare.
 * **/
public class NewFileRequest extends ClientManagerRequest {
    /** -------- Atribute -------- **/
    /**
     * Dimensiunea fisierului
     */
    private long filesize;
    /**
     * Tipul utilizatorului.
     */
    private String userType;


    /** -------- Gettere & Settere -------- **/
    /**
     * Setter pentru dimensiunea fisierului.
     */
    public void setFilesize(long filesize){
        this.filesize = filesize;
    }
    /**
     * Getter pentru dimensiunea fisierului.
     */
    public long getFilesize() {
        return filesize;
    }

    /**
     * Setter pentru tipul utilizatorului
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }
    /**
     * Getter pentru tipul utilizatorului
     */
    public String getUserType() {
        return userType;
    }
}
