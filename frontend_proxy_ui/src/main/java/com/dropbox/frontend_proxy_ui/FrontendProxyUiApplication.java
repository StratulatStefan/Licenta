package com.dropbox.frontend_proxy_ui;

import com.dropbox.frontend_proxy_ui.proxy.FeedbackManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.io.IOException;

@Import(WebSocketConfig.class)
@SpringBootApplication
public class FrontendProxyUiApplication{
	public static FeedbackManager feedbackManager = new FeedbackManager();

	public static void main(String[] args) throws IOException {
		feedbackManager = new FeedbackManager();
		new Thread(feedbackManager).start();
		SpringApplication.run(FrontendProxyUiApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowCredentials(true).allowedOrigins("*").allowedMethods("*");
			}
		};
	}

}
