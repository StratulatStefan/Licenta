package com.safestorage;

import com.safestorage.config.WebSocketConfig;
import com.safestorage.proxy.FeedbackManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Import(WebSocketConfig.class)
@EnableScheduling
@SpringBootApplication
public class FrontendProxyUiApplication {
	public static FeedbackManager feedbackManager = new FeedbackManager();

	public static void main(String[] args) {
		feedbackManager = new FeedbackManager();
		new Thread(feedbackManager).start();
		SpringApplication.run(FrontendProxyUiApplication.class, args);
	}
}
