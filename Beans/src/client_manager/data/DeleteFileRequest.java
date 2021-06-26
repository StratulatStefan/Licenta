package client_manager.data;

/**
 * <ul>
 *  <li>Clasa folosita pentru a reprezenta cererea de eliminare a unui fisier, trimisa de la client la managerul general.</li>
 *  <li>Va mosteni clasa <strong>ClientRequestManager</strong>, care va reprezenta o cerere dintre client si managerul general.<li>
 *  <li>Cererea de eliminare a fisierului are nevoie doar de id-ul utilizatorului si de numele fisierului, atribute care
 *      deja se afla in clasa parinte; Asadar, aceasta clasa nu va avea niciun membru.<li>
 * </ul>
 * **/
public class DeleteFileRequest extends ClientManagerRequest {
    /**
     * Constructor vid
     */
    public DeleteFileRequest(){
        super();
    }
}
