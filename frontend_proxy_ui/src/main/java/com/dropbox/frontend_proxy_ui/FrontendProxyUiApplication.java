package com.dropbox.frontend_proxy_ui;

import com.dropbox.frontend_proxy_ui.config.WebSocketConfig;
import com.dropbox.frontend_proxy_ui.proxy.FeedbackManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@Import(WebSocketConfig.class)
@EnableScheduling
@SpringBootApplication
public class FrontendProxyUiApplication{
	public static FeedbackManager feedbackManager = new FeedbackManager();

	public static void main(String[] args) throws IOException {
		feedbackManager = new FeedbackManager();
		new Thread(feedbackManager).start();
		SpringApplication.run(FrontendProxyUiApplication.class, args);
	}
}
