package client_manager;

import java.io.Serializable;

/**
 * Clasa care incapsuleaza obiectul ce va fi trimis ca raspuns de la managerul general catre client pentru fiecare
 * operatiune solicitata;
 * Va contine un raspuns (token-ul de noduri (cazul new_file) sau status (cazul rename_file)) si o exceptie generata
 * in cazul in care operatia esueaza.
 * Obiectul care va instantia aceasta clasa va fi trimis prin retea, deci va trebui sa fie serializabil.
 */
public class ManagerResponse implements Serializable {
    /** -------- Atribute -------- **/
    /**
     * Raspuns negativ al nodului general. (exceptie sau eroare)
     */
    private String exception;

    /** -------- Constructori -------- **/
    /**
     * Constructorul vid.
     */
    public ManagerResponse(){};


    /** -------- Gettere & Settere -------- **/
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
