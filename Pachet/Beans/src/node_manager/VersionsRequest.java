package node_manager;

/**
 * <ul>
 * 	<li>Clasa care va reprezenta o cerere dintre nodul general si nodurile interne, prin care se solicita
        informatii despre versiunile unui fisier.</li>
 * 	<li> Sunt furnizate identificatorul utilizatorului si numele fisierului.</li>
 * 	<li>Fiind trimisa intre nodul intern si nodul general, va mosteni clasa <strong>EditRequest</strong></li>
 * </ul>
 */
public class VersionsRequest extends EditRequest{
    /**
     * Constructor cu cele doua argumente necesare
     */
    public VersionsRequest(String user, String filename){
        super(user, filename);
    }

    /**
     * Constructor vid
     */
    public VersionsRequest(){super();}
}
