package com.dropbox.frontend_proxy_ui.controller;

import client_manager.ManagerComplexeResponse;
import client_manager.data.GetContentTableRequest;
import com.dropbox.frontend_proxy_ui.proxy.FrontendManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;

@RestController
public class WebSocketController {
    @Autowired
    private SimpMessagingTemplate template;

    @Scheduled(fixedRate = 5000)
    public void sendContent(){
        try {
            GetContentTableRequest getContentTableRequest = new GetContentTableRequest();
            ManagerComplexeResponse response = (ManagerComplexeResponse) FrontendManager.managerOperationRequest(getContentTableRequest);
            if(response.getException() != null){
                throw new Exception(response.getException());
            }
            this.template.convertAndSend("/topic/content", response.getResponse());

        }
        catch (Exception exception){
            this.template.convertAndSend("/topic/content",new HashMap<String, String>(){{put("exception", exception.getMessage());}});
        }
    }

    @Scheduled(fixedRate = 5000)
    public void sendNodes(){
        this.template.convertAndSend("/topic/nodes","nodes");
    }

    @Scheduled(fixedRate = 5000)
    public void sendReplication(){
        this.template.convertAndSend("/topic/replication","replication");
    }

}
