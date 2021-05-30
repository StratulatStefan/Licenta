package client_manager;

public class ManagerTextResponse extends ManagerResponse {
    /** -------- Atribute -------- **/
    /**
     * Raspunsul afirmativ al nodului general. (succes)
     */
    private String response;

    /** -------- Constructori -------- **/
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

    /** -------- Gettere & Settere -------- **/
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
