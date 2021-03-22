package client_manager.data;

public class RenameFileRequest extends ClientManagerRequest{
    private String newName;

    public String getNewName() {
        return newName;
    }
    public void setNewName(String newName) {
        this.newName = newName;
    }
}
