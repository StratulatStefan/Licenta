package com.dropbox.frontend_proxy_ui.controller;

import client_manager.ManagerComplexeResponse;
import client_manager.data.*;
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

    void fetchContentAndSend(ClientManagerRequest requestObject, String topic){
        try {
            ManagerComplexeResponse response = (ManagerComplexeResponse) FrontendManager.managerOperationRequest(requestObject);
            if(response.getException() != null)
                throw new Exception(response.getException());
            this.template.convertAndSend("/topic/" + topic, response.getResponse());
        }
        catch (Exception exception){
            this.template.convertAndSend("/topic/" + topic,new HashMap<String, String>(){{put("exception", exception.getMessage());}});
        }
    }

    @Scheduled(fixedRate = 2000)
    public void sendContent(){
        fetchContentAndSend(new GetContentTableRequest(), "content");
    }

    @Scheduled(fixedRate = 2000)
    public void sendNodes(){
        fetchContentAndSend(new GetNodesStorageQuantityRequest(), "nodes");
    }

    @Scheduled(fixedRate = 2000)
    public void sendStorageTable(){
        fetchContentAndSend(new GetStorageStatusRequest(), "storage");
    }

    @Scheduled(fixedRate = 2000)
    public void sendReplication(){
        fetchContentAndSend(new GetReplicationStatusRequest(), "replication");
    }

    @Scheduled(fixedRate = 2000)
    public void sendConnectionTable(){
        fetchContentAndSend(new GetConnectionTableRequest(), "connection");
    }

}
