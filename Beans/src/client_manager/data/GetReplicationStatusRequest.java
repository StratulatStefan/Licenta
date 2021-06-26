package client_manager.data;

/**
 * <ul>
 *  <li>Clasa folosita pentru a reprezenta cererile dintre client si managerul general,
 *      prin care se solicita starea mecanismului de replicare, reprezentând o lista de mesaje despre starea fiecarui fisier
 *      prezent in sistem, din punct de vedere al integritatii si disponibilitatii</li>
 *  <li>Clasa nu are nevoie de niciun membru, iar membrii mosteniti din clasa <strong>ClientManagerRequest</strong>
 *      nu vor fi initializati.</li>
 * </ul>
 */
public class GetReplicationStatusRequest extends ClientManagerRequest{
    /**
     * Constructor vid
     */
    public GetReplicationStatusRequest(){
        super();
    }
}
