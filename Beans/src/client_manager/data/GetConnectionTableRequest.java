package client_manager.data;

/**
 * <ul>
 *  <li>Clasa folosita pentru a reprezenta cererile dintre client si managerul general,
 *      prin care se solicita tabela de conexiuni, reprezentând toate nodurile interne conectate.</li>
 *  <li>Clasa nu are nevoie de niciun membru, iar membrii mosteniti din clasa <strong>ClientManagerRequest</strong>
 *      nu vor fi initializati.</li>
 * </ul>
 */
public class GetConnectionTableRequest extends ClientManagerRequest{
    /**
     * Constructor vid
     */
    public GetConnectionTableRequest(){
        super();
    }
}
