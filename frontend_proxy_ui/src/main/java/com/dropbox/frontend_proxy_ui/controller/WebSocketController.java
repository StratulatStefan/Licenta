package com.dropbox.frontend_proxy_ui.controller;

import client_manager.ManagerComplexeResponse;
import client_manager.data.*;
import com.dropbox.frontend_proxy_ui.proxy.FrontendManager;
import org.assertj.core.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@RestController
public class WebSocketController {
    @Autowired
    private SimpMessagingTemplate template;

    private final static List<Object> topics = Arrays.asList(new String[]{"content", "nodes", "storage", "replication", "connection"});

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

    @Scheduled(fixedRate = 2000)
    public void sendContent(){
        for(Object topic : topics){
            fetchContentAndSend(new GetContentTableRequest(),(String)topic);
        }
    }
}
