package com.dropbox.frontend_proxy_ui;

import com.dropbox.frontend_proxy_ui.services.FeedbackManagerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FrontendProxyUiApplication {
	public static FeedbackManagerService feedbackManager = new FeedbackManagerService();

	public static void main(String[] args) {
		feedbackManager = new FeedbackManagerService();
		new Thread(feedbackManager).start();
		SpringApplication.run(FrontendProxyUiApplication.class, args);

	}

}
