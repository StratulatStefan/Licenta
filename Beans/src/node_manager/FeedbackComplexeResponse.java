package node_manager;

import java.util.List;

public class FeedbackComplexeResponse extends FeedbackResponse{
    public List<Object> response;

    public List<Object> getResponse() {
        return response;
    }
    public void setResponse(List<Object> response) {
        this.response = response;
    }
}
