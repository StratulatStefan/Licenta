package node_manager;

/**
 * Clasa folosita pentru a incapsula cererea de redenumire a unui fisier, trimisa de la managerul general la nodul intern
 * Va mosteni clasa ClientRequestManager, care incapsuleaza o cerere dintre managerul general si nodul intern
 * Pe langa atributele de baza ale unei cereri dintre client si managerul general, avem nevoie si de noul nume al fisierului.
 * **/
public class RenameRequest extends EditRequest {
    /** -------- Atribute -------- **/
    /**
     * Noul nume al fisierului.
     */
    private String newName;

    public RenameRequest(String user, String filename, String newName, String description){
        super(user, filename);
        this.newName = newName;
        this.setDescription(description);

    }

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
