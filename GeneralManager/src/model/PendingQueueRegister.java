package model;

import data.Time;

public class PendingQueueRegister {
    private String userId;
    private String filename;
    private long timestamp;

    public PendingQueueRegister(String userId, String filename){
        this.userId = userId;
        this.filename = filename;
        this.timestamp = Time.getCurrentTimestamp();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
