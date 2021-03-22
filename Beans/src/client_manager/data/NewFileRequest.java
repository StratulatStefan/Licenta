package client_manager.data;


public class NewFileRequest extends ClientManagerRequest {
    private int filesize;
    private int replication_factor;

    public void setFilesize(int filesize){
        this.filesize = filesize;
    }
    public int getFilesize() {
        return filesize;
    }

    public void setReplication_factor(int replication_factor) {
        this.replication_factor = replication_factor;
    }
    public int getReplication_factor() {
        return replication_factor;
    }
}
