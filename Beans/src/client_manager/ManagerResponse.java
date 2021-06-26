package client_manager;

import java.io.Serializable;

/**
 * <ul>
 *  <li>Clasa care reprezinta obiectul ce va fi trimis ca raspuns de la managerul general catre client pentru fiecare
 *      operatiune solicitata;</li>
 *  <li>Va contine doar o exceptie generata in cazul in care operatia esueaza.</li>
 *  <li>Corpul raspunsului va fi creat si mostenirea acestei clase si adaugarea unui nou membru specific</li>
 *  <li>Obiectul care va instantia aceasta clasa va fi trimis prin retea, deci va trebui sa fie serializabil.</li>
 * </ul>
 */
public class ManagerResponse implements Serializable {
    /**
     * Raspuns negativ al nodului general. (exceptie sau eroare)
     */
    private String exception;


    /**
     * Constructorul vid.
     */
    public ManagerResponse(){};


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
