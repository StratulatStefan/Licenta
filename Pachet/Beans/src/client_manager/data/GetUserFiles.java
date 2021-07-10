package client_manager.data;

/**
 * <ul>
 *  <li>Clasa folosita pentru a reprezenta cererile dintre client si managerul general,
 *      prin care se solicita detalii despre toate fisierele unui utilizator</li>
 *  <li>Clasa are nevoie doar id-ul utilizatorului, care va fi mostenit din clasa <strong>ClientManagerRequest</strong>
 *  </li>
 * </ul>
 */
public class GetUserFiles extends ClientManagerRequest {
    /**
     * Constructor vid
     */
    public GetUserFiles(){
        super();
    }
}
