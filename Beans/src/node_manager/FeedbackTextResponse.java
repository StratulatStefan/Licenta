package node_manager;

import java.io.Serializable;

/**
 * Clasa care va descrie statusul unei operatiuni solicitate de nodul general, ca urmare a altei cereri primite de la client
 * Acest obiect va incapsula statusul operatiilor de stergere/redenumire, astfel incat sa se poata trimite inapoi la frontend statusul operatiei solicitate
 */
public class FeedbackTextResponse extends FeedbackResponse {
    /** -------- Atribute -------- **/
    /**
     * Mesaj auxiliar despre statusul operatiei
     */
    private String status;


    /** -------- Gettere & Settere -------- **/
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
