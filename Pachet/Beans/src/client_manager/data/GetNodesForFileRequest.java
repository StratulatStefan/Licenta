package client_manager.data;

/**
 * <ul>
 *  <li>Clasa folosita pentru a reprezenta cererile dintre client si managerul general,
 *      prin care se solicita adresele tuturor nodurilor care stocheaza un anumit fisier</li>
 *  <li>Cererea este relevanta in contextul interfetei utilizatorului, in momentul in care, pentru
 *      un anumit fisier, administratorul doreste sa vada toate nodurile care detin o replica a fisierului </li>
 * </ul>
 */
public class GetNodesForFileRequest extends ClientManagerRequest{
    /**
     * Constructor vid
     */
    public GetNodesForFileRequest(){
        super();
    }
}
