package node_manager;

/**
 * <ul>
 *  <li>Clasa folosita pentru a reprezenta cererea de eliminare a unui fisier, trimisa de la managerul general la nodul intern.</li>
 *  <li>Va mosteni clasa ClientRequestManager, care incapsuleaza o cerere dintre managerul general si nodul intern.</li>
 *  <li>Cererea de eliminare a fisierului are nevoie doar de id-ul utilizatorului si de numele fisierului, atribute care
 *      deja se afla in clasa parinte; Asadar, aceasta clasa nu va avea niciun membru.</li>
 * </ul>
 * **/
public class DeleteRequest extends EditRequest {
    /**
     * Constructorul clasei; Apeleaza constructorul clasei parinte, furnizand cele doua argumente
     * generice fiecarei cereri;
     * @param user Id-ul utilizatoului
     * @param filename Numele fisierului utilizatorului
     */
    public DeleteRequest(String user, String filename){
        super(user, filename);
    }

    /**
     * Constructorul vid
     */
    public DeleteRequest(){super();}
}
