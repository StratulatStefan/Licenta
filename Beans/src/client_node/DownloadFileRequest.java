package client_node;

import client_manager.data.ClientManagerRequest;

import java.io.Serializable;

public class DownloadFileRequest implements Serializable {
    private String userId;
    private String filename;

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
}
