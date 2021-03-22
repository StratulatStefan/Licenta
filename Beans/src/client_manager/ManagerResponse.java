package client_manager;

import java.io.IOException;
import java.io.Serializable;

/**
 * Clasa care contine token-ul ce va ingloba nodurile catre care se va trimite noul fisier
 */
public class ManagerResponse implements Serializable {
    private String response;

    private String exception;

    public ManagerResponse(String response){
        this.response = response;
    }
    public ManagerResponse(){};

    public String getResponse() throws NullPointerException {
        if(response == null)
            throw new NullPointerException(this.getException());
        return response;
    }
    public void setResponse(String chain) {
        this.response = chain;
    }

    public String getException(){return exception;}
    public void setException(String exception) {
        this.exception = exception;
    }
}
