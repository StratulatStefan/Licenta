package node_manager;

import java.io.Serializable;

public class FeedbackResponse implements Serializable {
    /** -------- Atribute -------- **/
    /**
     * Flag pentru succesul operatiei
     */
    private boolean success;

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
}
