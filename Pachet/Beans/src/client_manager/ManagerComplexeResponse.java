package client_manager;

import client_manager.ManagerResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <ul>
 *  <li>Clasa care va reprezenta o cerere de la nodul general catre aplicatia de tip client</li>
 *  <li>Se mosteneste clasa <strong>ManagerResponse</strong> atat din punct de vedere logic, dorind sa se
 *      evidentieze o clasa de tip raspuns, dar si pentru a pastra elementele de tip mesaj de exceptie</li>
 *  <li>Caracteristic acestui raspuns este modul in care este reprezentat corpul raspunsului. Acesta este
 *      reprezentat sub forma unei liste de obiecte. Se foloseste generic <strong>Object</strong> pentru
 *      a sugera ca se poate inlocui cu orice structura de date complexa</li>
 * </ul>
 */
public class ManagerComplexeResponse extends ManagerResponse {
    /**
     * Corpul raspunsului nodului general
     */
    private List<Object> response;


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


    /**
     * Setter pentru raspunsul nodului general.
     */
    public void setResponse(List<Object> response) {
        this.response = response;
    }
    /**
     * <ul>
     *  <li>Corpul mesajului va fi reprezentat sub forma unei liste. Acesta metoda
     *      are rolul de a adauga un element in lista.</li>
     *  <li>Obiectul ce va fi adaugat in lista are un format specific (structura de date de tip HashMap)</li>
     * </ul>
     * @param response Obiectul ce va fi adaugat lista.
     */
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
