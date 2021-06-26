package client_manager.data;

/**
 * <ul>
 *  <li>Clasa folosita pentru a reprezenta cererile dintre client si managerul general,
 *      prin care se solicita tabela de content, reprezentând toate fisierele existente in sistem, impreuna cu
 *      toate detaliile de versionare, integritate si disponibilitate</li>
 *  <li>Clasa nu are nevoie de niciun membru, iar membrii mosteniti din clasa <strong>ClientManagerRequest</strong>
 *      nu vor fi initializati.</li>
 * </ul>
 */
public class GetContentTableRequest extends ClientManagerRequest{
    /**
     * Constructor vid
     */
    public GetContentTableRequest(){
        super();
    }
}
