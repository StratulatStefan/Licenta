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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ManagerComplexeResponse response = (ManagerComplexeResponse) FrontendManager.managerOperationRequest(requestObject);
                    if(response.getException() != null)
                        throw new Exception(response.getException());
                    template.convertAndSend("/topic/" + topic, response.getResponse());
                }
                catch (Exception exception){
                    template.convertAndSend("/topic/" + topic,new HashMap<String, String>(){{put("exception", exception.getMessage());}});
                }
            }
        }).start();
    }

    @Scheduled(fixedRate = 4000)
    public void sendContent(){
        //fetchContentAndSend(new GetContentTableRequest(), "content");
        //fetchContentAndSend(new GetNodesStorageQuantityRequest(), "nodes");
        //fetchContentAndSend(new GetStorageStatusRequest(), "storage");
        //fetchContentAndSend(new GetReplicationStatusRequest(), "replication");
        //fetchContentAndSend(new GetConnectionTableRequest(), "connection");
    }
}
