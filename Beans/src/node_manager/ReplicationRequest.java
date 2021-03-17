package node_manager;

import java.io.Serializable;

public class ReplicationRequest implements Serializable {
    private String userId;

    private String filename;

    private String operation;

    private String destionationAddress;

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

    public String getDestionationAddress() {
        return destionationAddress;
    }
    public void setDestionationAddress(String destionationAddress) {
        this.destionationAddress = destionationAddress;
    }

    public String getOperation() {
        return operation;
    }
    public void setOperation(String operation) {
        this.operation = operation;
    }
}
