package com.safestorage;

import com.safestorage.config.WebSocketConfig;
import com.safestorage.proxy.FeedbackManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * <ul>
 * 	<li>Functia care va contine clasa main.</li>
 * 	<li> Mai intai, se va atasa configuratia pentru websocket prin intermediul clasei de configuratie<strong>WebSocketConfig</strong>.</li>
 * 	<li>Totodata, se va initializa explicit componenta de controlare a feedback-ului.</li>
 * 	<li> In mod implicit, prin intermediul Spring, se va instantiaaplicatia.</li>
 * </ul>
 */
@Import(WebSocketConfig.class)
@EnableScheduling
@SpringBootApplication
public class FrontendProxyUiApplication {
	/**
	 * Obiectul care va asigura mecanismul de feedback, in comunicarea cu nodurile interne.
	 */
	public static FeedbackManager feedbackManager = new FeedbackManager();

	/**
	 * <ul>
	 * 	<li>Functia main care va porni mecanismul de feedback.</li>
	 * 	<li> Aplicatia va fi pornita cu ajutorul Spring.</li>
	 * </ul>
	 */
	public static void main(String[] args) {
		feedbackManager = new FeedbackManager();
		new Thread(feedbackManager).start();
		SpringApplication.run(FrontendProxyUiApplication.class, args);
	}
}
