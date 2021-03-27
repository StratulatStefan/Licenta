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
    private int filesize;
    /**
     * Factorul de replicare al fisierului.
     */
    private int replication_factor;


    /** -------- Gettere & Settere -------- **/
    /**
     * Setter pentru dimensiunea fisierului.
     */
    public void setFilesize(int filesize){
        this.filesize = filesize;
    }
    /**
     * Getter pentru dimensiunea fisierului.
     */
    public int getFilesize() {
        return filesize;
    }

    /**
     * Setter pentru factorul de replicare
     */
    public void setReplication_factor(int replication_factor) {
        this.replication_factor = replication_factor;
    }
    /**
     * Getter pentru factorul de replicare
     */
    public int getReplication_factor() {
        return replication_factor;
    }
}
