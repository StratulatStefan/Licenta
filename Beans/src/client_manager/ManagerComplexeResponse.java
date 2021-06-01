package client_manager;

import client_manager.ManagerResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ManagerComplexeResponse extends ManagerResponse {
    /** -------- Atribute -------- **/
    /**
     * Raspunsul afirmativ al nodului general. (succes)
     */
    private List<Object> response;

    /** -------- Constructori -------- **/
    /**
     * Constructorul vid.
     */
    public ManagerComplexeResponse(){};
    /**
     * Constructor care instantiaza raspunsul;
     */
    public ManagerComplexeResponse(List<Object> response){
        this.response = response;
    }

    /** -------- Gettere & Settere -------- **/
    /**
     * Setter pentru raspunsul nodului general.
     */
    public void setResponse(List<Object> response) {
        this.response = response;
    }

    public void addResponse(HashMap<String, Object> response){
        this.response = new ArrayList<>();
        this.response.add(response);
    }

    /**
     * Getter pentru raspunsul nodului general
     * Se incearca obtinerea raspunsului afirmativ; In cazul in care nu exista,
     * se va genera o exceptie de tip NullPointerException, care va contine mesajul de eroare.
     */
    public List<Object> getResponse() throws NullPointerException {
        if(response == null)
            throw new NullPointerException(this.getException());
        return response;
    }
}
