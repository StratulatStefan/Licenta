package client_manager;

import java.io.IOException;
import java.io.Serializable;

/**
 * Clasa care incapsuleaza obiectul ce va trimis ca raspuns de la managerul general catre client pentru fiecare
 * operatiune solicitata;
 * Va contine un raspuns (token-ul de noduri - cazul new_file sau status - cazul rename_file) si o exceptie generata
 * in cazul in care operatia esueaza.
 * Obiectul care va instantia aceasta clasa va fi trimis prin retea, deci va trebui sa fie serializabil.
 */
public class ManagerResponse implements Serializable {
    /** -------- Atribute -------- **/
    /**
     * Raspunsul afirmativ al nodului general. (succes)
     */
    private String response;
    /**
     * Raspuns negativ al nodului general. (exceptie sau eroare)
     */
    private String exception;

    /** -------- Constructori -------- **/
    /**
     * Constructorul vid.
     */
    public ManagerResponse(){};

    /**
     * Constructor care seteaza raspunsul; Tine locul setter-ului.
     */
    public ManagerResponse(String response){
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

    /**
     * Setter pentru mesajul de exceptie, generat de o eroare.
     */
    public void setException(String exception) {
        this.exception = exception;
    }
    /**
     * Getter pentru messajul de exceptie
     */
    public String getException(){return exception;}
}
