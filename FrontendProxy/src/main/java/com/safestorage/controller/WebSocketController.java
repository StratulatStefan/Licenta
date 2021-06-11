package com.safestorage.controller;

import client_manager.ManagerComplexeResponse;
import client_manager.data.*;
import com.safestorage.proxy.FrontendManager;
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

    private final static List<Object> topics = Arrays.asList(new String[]{
            "content", "nodes", "storage", "replication", "connection"
    });
    private final static List<Object> requests = Arrays.asList(new Class[]{
            GetContentTableRequest.class, GetNodesStorageQuantityRequest.class, GetStorageStatusRequest.class,
            GetReplicationStatusRequest.class, GetConnectionTableRequest.class
    });

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
            try {
                ClientManagerRequest request = (ClientManagerRequest) ((Class) requests.get(topics.indexOf(topic))).newInstance();
                fetchContentAndSend(request,(String)topic);
            }
            catch (Exception exception){
                System.out.println("Cannot instantiate class : " + exception.getMessage());
            }
        }
        /*fetchContentAndSend(new GetContentTableRequest(),"content");
        fetchContentAndSend(new GetNodesStorageQuantityRequest(),"nodes");
        fetchContentAndSend(new GetStorageStatusRequest(),"storage");
        fetchContentAndSend(new GetReplicationStatusRequest(),"replication");
        fetchContentAndSend(new GetConnectionTableRequest(),"connection");*/
    }
}
