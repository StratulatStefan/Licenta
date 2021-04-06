package node_manager;

import java.io.Serializable;

public class FeedbackResponse implements Serializable {
    private boolean success;
    private String status;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
