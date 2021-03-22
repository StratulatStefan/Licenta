package client_manager.data;

import java.io.Serializable;

public abstract class ClientManagerRequest implements Serializable {
    private String userId;
    private String filename;

    public void setFilename(String filename) {
        this.filename = filename;
    }
    public String getFilename() {
        return filename;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUserId() {
        return userId;
    }

}
