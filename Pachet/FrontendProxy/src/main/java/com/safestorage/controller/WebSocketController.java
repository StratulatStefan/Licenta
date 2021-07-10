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

/**
 * <ul>
 * 	<li>Clasa care expune componenta de tip controller pentru implementarea modelului <strong>publish-subscribe</strong>, in comunicarea cu clientul prin <strong>web-sockets</strong>.</li>
 * 	<li>Clasa va expune o serie de <strong>topic</strong>-uri, pe care va face <strong>publish</strong> la intervale regulate de tip.</li>
 * </ul>
 */
@RestController
public class WebSocketController {
    /**
     * <ul>
     * 	<li>Obiectul care va gestiona <strong>web-socket</strong>-ul.</li>
     * 	<li> Instantierea va fi realizata de <strong>SpringBoot</strong> cu ajutorul adnotarii <strong>@AutoWired</strong>.</li>
     * 	<li>Obiectul va facilita operatiile de <strong>publish</strong>.</li>
     * </ul>
     */
    @Autowired
    private SimpMessagingTemplate template;

    /**
     * Lista de topic-uri pe care sa va face <strong>publish</strong>.
     */
    private final static List<Object> topics = Arrays.asList(new String[]{
            "content", "nodes", "storage", "replication", "connection"
    });

    /**
     * <ul>
     * 	<li>Fiecare <strong>publish</strong> va implica, mai intai, efectuarea unei operatii de extragere a starii sistemului, de la componentele interne a sistemului.</li>
     * 	<li>Pentru a extrage starea, se va trimite o cerere ca nodul general.</li>
     * 	<li> Acest obiect contine lista de obiecte care vor reprezenta tipurile de cereri.</li>
     * 	<li>Se impune ca cererea sa se afle pe pozitia corespunzatoare a <strong>topic</strong>-ului din lista <strong>topics.</li>
     * 	<li></strong>.</li>
     * </ul>
     */
    private final static List<Object> requests = Arrays.asList(new Class[]{
            GetContentTableRequest.class, GetNodesStorageQuantityRequest.class, GetStorageStatusRequest.class,
            GetReplicationStatusRequest.class, GetConnectionTableRequest.class
    });

    /**
     * <ul>
     * 	<li>Functie apelata de functia <strong>sendContent</strong>, prin care se va realiza cererea de extragere a starii de la nodul general si apoi se va face <strong>publish</strong>.</li>
     * 	<li>Fiecare <strong>topic</strong> va fi tratat pe un <strong>thread</strong> diferit, astfel incat sa se evite blocajele.</li>
     * </ul>
     * @param requestObject Tipul obiectului care va reprezenta cererea de extragere a starii.
     * @param topic Topic-ul pe care va fi trimis raspunsul.
     */
    private void fetchContentAndSend(ClientManagerRequest requestObject, String topic){
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

    /**
     * <ul>
     * 	<li>Functia apelata la intervale regulate de timp <strong>4000 ms</strong>,
     prin care se face <strong>publish</strong> pe <strong>topic</strong>-urile definite anterior.</li>
     * 	<li>Fiecare publish va fi precedat de o cerere de extragere a starii de la nodul general.</li>
     * </ul>
     */
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
    }
}
