package client_manager.data;

/**
 * <ul>
 *  <li>Clasa folosita pentru a reprezenta cererile dintre client si managerul general,
 *      prin care se solicita adresa unuia dintre nodurile interne care ar putea furniza fisierul utilizatorului</li>
 * </ul>
 */
public class GetNodeForDownload extends ClientManagerRequest{
    /**
     * Constructor vid
     */
    public GetNodeForDownload(){
        super();
    }
}
