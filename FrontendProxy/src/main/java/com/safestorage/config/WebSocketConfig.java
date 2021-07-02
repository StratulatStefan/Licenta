package com.safestorage.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * <ul>
 * 	<li>Clasa de configurare a componentei de <strong>Web Sockets</strong>.</li>
 * 	<li> Pentru a realiza configurarea,se va mosteni clasa <strong>WebSocketMessageBrokerConfigurer</strong>.</li>
 * 	<li>Se va configura prefixul, endpoint-ul si adresele care se vor putea conecta la acest websocket.</li>
 * </ul>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    /**
     * <ul>
     * 	<li>Functia care va configura componenta de tip broker de mesaje.</li>
     * 	<li> Se va prefixul de tip aplicatie si prefixul de tip topic.</li>
     * </ul>
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * <ul>
     * 	<li>Functia care va configura end point-ul.</li>
     * 	<li> Se vor configura calea endpoint-ului si adresele permise.</li>
     * </ul>
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/wbsocket").setAllowedOrigins("http://localhost:5000");
    }
}
