package node_manager;

/**
 * <ul>
 * 	<li>Clasa folosita pentru a reprezenta cererea de redenumire a unui fisier, trimisa de la managerul general la nodul intern.</li>
 * 	<li>Va mosteni clasa <strong>ClientRequestManager</strong>, care incapsuleaza o cerere dintre managerul general si nodul intern.</li>
 * 	<li>Pe langa atributele de baza ale unei cereri dintre client si managerul general, avem nevoie si de noul nume al fisierului.</li>
 * </ul>
 * **/
public class RenameRequest extends EditRequest {
    /**
     * Noul nume al fisierului.
     */
    private String newName;


    /**
     * Constructor vid.
     */
    public RenameRequest(){super();}
    /**
     * Constructorul clasei; Apeleaza constructorul clasei parinte, furnizand cele doua argumente
     * generice fiecarei cereri; Mai mult decat atat, adauga descrierea cererii, necesara la versionare
     * @param user Id-ul utilizatoului
     * @param filename Numele fisierului utilizatorului
     * @param description Descierea cererii de redenumire (motiv redenumire, etc)
     */
    public RenameRequest(String user, String filename, String newName, String description){
        super(user, filename);
        this.newName = newName;
        this.setDescription(description);

    }


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
