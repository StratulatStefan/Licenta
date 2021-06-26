package node_manager;

import java.io.Serializable;

/**
 * <ul>
 * 	<li>Clasa care va descrie statusul unei operatiuni solicitate de nodul general, ca urmare a altei cereri primite de la client.</li>
 * 	<li>Acest obiect va contne statusul operatiilor de stergere/redenumire, astfel incat sa se poata trimite inapoi la frontend statusul operatiei solicitate.</li>
 * 	<li>Rezultatul va fi reprezentat sub forma unui sir de caractere.</li>
 * </ul>
 */
public class FeedbackTextResponse extends FeedbackResponse {
    /**
     * Mesaj despre statusul operatiei
     */
    private String status;


    /**
     * Setter pentur mesajul de status
     */
    public void setStatus(String status) {
        this.status = status;
    }
    /**
     * Getter pentru mesajul de status
     */
    public String getStatus() {
        return status;
    }
}
