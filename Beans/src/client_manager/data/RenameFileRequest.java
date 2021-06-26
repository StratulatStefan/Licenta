package client_manager.data;

/**
 * <ul>
 *  <li>Clasa folosita pentru a reprezenta cererea de redenumire a unui fisier, trimisa de la client la managerul general.</li>
 *  <li>Va mosteni clasa ClientRequestManager, care incapsuleaza o cerere dintre client si managerul general.</li>
 *  <li>Pe langa atributele de baza ale unei cereri dintre client si managerul general, avem nevoie si de noul nume al fisierului.</li>
 * </ul>
 * **/
public class RenameFileRequest extends ClientManagerRequest{
    /**
     * Noul nume al fisierului.
     */
    private String newName;


    /**
     * Constructor vid
     */
    public RenameFileRequest(){
        super();
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
