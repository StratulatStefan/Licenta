package client_manager.data;

public class DeleteFileFromNodeRequest extends DeleteFileRequest{
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
