package com.dropbox.frontend_proxy_ui;

import client_manager.ManagerResponse;
import client_manager.data.ClientManagerRequest;
import client_manager.data.DeleteFileRequest;
import client_manager.data.NewFileRequest;
import client_manager.data.RenameFileRequest;
import com.dropbox.frontend_proxy_ui.proxy.FeedbackManager;
import com.dropbox.frontend_proxy_ui.proxy.FileSender;
import communication.Serializer;
import log.ProfiPrinter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import os.FileSystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

@SpringBootApplication
public class FrontendProxyUiApplication {
	public static FeedbackManager feedbackManager = new FeedbackManager();

	public static void main(String[] args) throws IOException {
		feedbackManager = new FeedbackManager();
		new Thread(feedbackManager).start();

		SpringApplication.run(FrontendProxyUiApplication.class, args);
	}

}
