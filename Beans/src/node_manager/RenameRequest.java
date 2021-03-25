package node_manager;

public class RenameRequest extends EditRequest {
    private String newname;

    public String getNewname() {
        return newname;
    }
    public void setNewname(String newname) {
        this.newname = newname;
    }
}
