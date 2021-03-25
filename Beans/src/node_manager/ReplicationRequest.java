package node_manager;
import java.util.ArrayList;
import java.util.List;

public class ReplicationRequest extends EditRequest {
    private ArrayList<String> destionationAddress;

    public List<String> getDestionationAddress() {
        return destionationAddress;
    }
    public void setDestionationAddress(List<String> destionationAddress) {
        this.destionationAddress = new ArrayList<>(destionationAddress);
    }
}
