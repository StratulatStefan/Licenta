package client_manager;

import client_manager.ManagerResponse;

import java.util.HashMap;
import java.util.List;

public class ManagerComplexeResponse extends ManagerResponse {
    /** -------- Atribute -------- **/
    /**
     * Raspunsul afirmativ al nodului general. (succes)
     */
    private List<HashMap<String, Object>> response;

    /** -------- Constructori -------- **/
    /**
     * Constructorul vid.
     */
    public ManagerComplexeResponse(){};
    /**
     * Constructor care instantiaza raspunsul;
     */
    public ManagerComplexeResponse(List<HashMap<String, Object>> response){
        this.response = response;
    }

    /** -------- Gettere & Settere -------- **/
    /**
     * Setter pentru raspunsul nodului general.
     */
    public void setResponse(List<HashMap<String, Object>> response) {
        this.response = response;
    }
    /**
     * Getter pentru raspunsul nodului general
     * Se incearca obtinerea raspunsului afirmativ; In cazul in care nu exista,
     * se va genera o exceptie de tip NullPointerException, care va contine mesajul de eroare.
     */
    public List<HashMap<String, Object>> getResponse() throws NullPointerException {
        if(response == null)
            throw new NullPointerException(this.getException());
        return response;
    }
}
