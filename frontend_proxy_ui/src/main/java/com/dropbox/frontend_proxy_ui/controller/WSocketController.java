package com.dropbox.frontend_proxy_ui.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.websocket.OnError;

@Controller
public class WSocketController {
    @GetMapping("/app/test")
    @SendTo("/topic/test")
    public String test() {
        System.out.println("Am primit mesaaaaaaj!!");
        return "saluuuuut";
    }

    @OnError
    public void onError(Throwable error)
    {
        System.out.println("Eroare : " + error.getMessage());
    }
}
