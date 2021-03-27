package client_manager.data;

/**
 * Clasa folosita pentru a incapsula cererea de redenumire a unui fisier, trimisa de la client la managerul general.
 * Va mosteni clasa ClientRequestManager, care incapsuleaza o cerere dintre client si managerul general.
 * Pe langa atributele de baza ale unei cereri dintre client si managerul general, avem nevoie si de noul nume al fisierului.
 * **/
public class RenameFileRequest extends ClientManagerRequest{
    /** -------- Atribute -------- **/
    /**
     * Noul nume al fisierului.
     */
    private String newName;


    /** -------- Gettere & Settere -------- **/
    /**
     * Setter pentru noul nume al fisierului.
     */
    public void setNewName(String newName) {
        this.newName = newName;
    }
    /**
     * Getter pentru noul nume al fisierului.
     */
    public String getNewName() {
        return newName;
    }
}
