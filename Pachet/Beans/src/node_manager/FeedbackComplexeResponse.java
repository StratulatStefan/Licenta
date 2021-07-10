package node_manager;

import java.util.List;

/**
 * <ul>
 * 	<li>Clasa care va descrie statusul unei operatiuni solicitate de nodul general,
 * 	    ca urmare a altei cereri primite de la client.</li>
 * 	<li>Acest obiect va contne statusul operatiilor de stergere/redenumire,
 * 	    astfel incat sa se poata trimite inapoi la frontend statusul operatiei solicitate.</li>
 * 	<li>Rezultatul va fi reprezentat sub forma unei lista, care va putea contine diverse
 * 	    tipuri de obiecte (se foloseste generic tipul <strong>Object</strong></li>
 * </ul>
 */
public class FeedbackComplexeResponse extends FeedbackResponse{
    /**
     * Corpul raspunsului, reprezentat de o lista de obiecte.
     */
    public List<Object> response;


    /**
     * Getter pentru corpul raspunsului
     */
    public List<Object> getResponse() {
        return response;
    }
    /**
     * Setter pentru corpul raspunsului
     */
    public void setResponse(List<Object> response) {
        this.response = response;
    }
}
