package client_manager.data;

/**
 * <ul>
 *  <li>Clasa folosita pentru a reprezenta cererile dintre client si managerul general,
 *      prin care se solicita tabela capacitatilor de stocare, reprezentând cantatile de stocare ocupate
 *      de fiecare nod.</li>
 *  <li>Clasa nu are nevoie de niciun membru, iar membrii mosteniti din clasa <strong>ClientManagerRequest</strong>
 *      nu vor fi initializati.</li>
 * </ul>
 */
public class GetNodesStorageQuantityRequest extends ClientManagerRequest {
    /**
     * Constructor vid
     */
    public GetNodesStorageQuantityRequest(){
        super();
    }
}
