package client_manager;

import java.io.Serializable;

/**
 * Clasa care contine token-ul ce va ingloba nodurile catre care se va trimite noul fisier
 */
public class Token implements Serializable {
    private String chain;

    private String exception;

    public Token(String token){
        this.chain = token;
    }
    public Token(){};

    public String getToken() throws Exception{
        if(chain == null)
            throw new Exception(this.getException());
        return chain;
    }
    public void setToken(String chain) {
        this.chain = chain;
    }

    public String getException(){return exception;}
    public void setException(String exception) {
        this.exception = exception;
    }
}
