package client_manager;

/**
 * <ul>
 *  <li>Clasa care va reprezenta o cerere de la nodul general catre aplicatia de tip client</li>
 *  <li>Se mosteneste clasa <strong>ManagerResponse</strong> atat din punct de vedere logic, dorind sa se
 *      evidentieze o clasa de tip raspuns, dar si pentru a pastra elementele de tip mesaj de exceptie</li>
 *  <li>Caracteristic acestui raspuns este modul in care este reprezentat corpul raspunsului. Acesta este
 *      reprezentat sub forma unui mesaj de tip text.</li>
 * </ul>
 */
public class ManagerTextResponse extends ManagerResponse {
    /**
     * Raspunsul nodului general.
     */
    private String response;


    /**
     * Constructorul vid.
     */
    public ManagerTextResponse(){};
    /**
     * Constructor care instantiaza raspunsul;
     */
    public ManagerTextResponse(String response){
        this.response = response;
    }

    /**
     * Setter pentru raspunsul nodului general.
     */
    public void setResponse(String response) {
        this.response = response;
    }
    /**
     * Getter pentru raspunsul nodului general
     * Se incearca obtinerea raspunsului afirmativ; In cazul in care nu exista,
     * se va genera o exceptie de tip NullPointerException, care va contine mesajul de eroare.
     */
    public String getResponse() throws NullPointerException {
        if(response == null)
            throw new NullPointerException(this.getException());
        return response;
    }
}
