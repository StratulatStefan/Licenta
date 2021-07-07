package client_manager.data;


/**
 * <ul>
 *  <li>Clasa folosita pentru a reprezenta cererea de actualizare a tipului corespunzator unui utilizator, trimisa de la client la managerul general.</li>
 *  <li>Va mosteni clasa <strong>ClientRequestManager</strong>, care va reprezenta o cerere dintre client si managerul general.<li>
 *  <li>Cererea de eliminare a fisierului are nevoie de id-ul utilizatorului si de noul factor de replicare<li>
 * </ul>
 * **/
public class UpdateUserTypeRequest extends ClientManagerRequest{
    /**
     * Noul factor de replicare
     */
    private String user_type;

    /**
     * Constructor vid
     */
    public UpdateUserTypeRequest(){super();}

    /**
     * Getter pentru noul tip al utilizatorului.
     */
    public String getUser_type() {
        return user_type;
    }

    /**
     * Setter pentru noul tip al utilizatorului.
     */
    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }
}
