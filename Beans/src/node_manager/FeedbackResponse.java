package node_manager;

import java.io.Serializable;

/**
 * Clasa care va descrie statusul unei operatiuni solicitate de nodul general, ca urmare a altei cereri primite de la client
 * Acest obiect va incapsula statusul operatiilor de stergere/redenumire, astfel incat sa se poata trimite inapoi la frontend statusul operatiei solicitate
 */
public class FeedbackResponse implements Serializable {
    /** -------- Atribute -------- **/
    /**
     * Flag pentru succesul operatiei
     */
    private boolean success;
    /**
     * Mesaj auxiliar despre statusul operatiei
     */
    private String status;


    /** -------- Gettere & Settere -------- **/
    /**
     * Getter pentru flag-ul de succes
     */
    public boolean isSuccess() {
        return success;
    }
    /**
     * Setter pentru flag-ul de succes.
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

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
