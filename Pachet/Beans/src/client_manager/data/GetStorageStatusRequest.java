package client_manager.data;

/**
 * <ul>
 *  <li>Clasa folosita pentru a reprezenta cererile dintre client si managerul general,
 *      prin care se solicita tabela de status, reprezentând centralizarea tuturor hearbeat-urilor primite de la
 *      nodurile interne, sub forma obiectelor <strong>NodeBeat</strong>, si organizarea acestora pe liste de
 *      utilizatori si fisiere ale acestora</li>
 *  <li>Clasa nu are nevoie de niciun membru, iar membrii mosteniti din clasa <strong>ClientManagerRequest</strong>
 *      nu vor fi initializati.</li>
 * </ul>
 */
public class GetStorageStatusRequest extends ClientManagerRequest{
    /**
     * Constructor vid
     */
    public GetStorageStatusRequest(){
        super();
    }
}
