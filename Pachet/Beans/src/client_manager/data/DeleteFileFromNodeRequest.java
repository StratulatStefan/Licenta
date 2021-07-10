package client_manager.data;

/**
 * <ul>
 *  <li>Clasa folosita pentru a reprezenta cererile dintre client si managerul general,
 *      pentru eliminarea unui fisier de pe unul dintre nodurile interne.</li>
 *  <li>Fiind o cerere de eliminare, pentru a pastra o ierarhie logica a tipurilor de cerere,
 *      va mosteni clasa <strong>DeleteFileRequest</strong></li>
 * </ul>
 */
public class DeleteFileFromNodeRequest extends DeleteFileRequest{
    /**
     * Adresa de pe care se doreste a fi sters fisierul.
     */
    private String address;


    /**
     * Constructor vid
     */
    public DeleteFileFromNodeRequest(){
        super();
    }


    /**
     * Getter pentru adresa nodului.
     */
    public String getAddress() {
        return address;
    }
    /**
     * Setter pentru adresa nodului.
     */
    public void setAddress(String address) {
        this.address = address;
    }
}
