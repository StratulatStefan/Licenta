package client_manager;

import java.io.Serializable;

/**
 * Clasa care contine token-ul ce va ingloba nodurile catre care se va trimite noul fisier
 */
public class Token implements Serializable {
    private String chain;

    public Token(String token){
        this.chain = token;
    }
    
    public String getToken() {
        return chain;
    }
    public void setToken(String chain) {
        this.chain = chain;
    }
}
