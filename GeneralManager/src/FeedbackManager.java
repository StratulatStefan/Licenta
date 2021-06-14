import client_manager.data.NewFileRequest;
import client_node.NewFileRequestFeedback;
import communication.Serializer;
import config.AppConfig;
import logger.LoggerService;

import java.io.DataInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FeedbackManager implements Runnable{
    private final List<NewFileRequestFeedback> feedbackList;
    private static int bufferSize = Integer.parseInt(AppConfig.getParam("buffersize"));
    private static int feedbackPort = Integer.parseInt(AppConfig.getParam("feedbackPort"));

    public FeedbackManager(){
        this.feedbackList = new ArrayList<NewFileRequestFeedback>();
    }

    private Runnable feedbackThread(Socket frontendSocket){
        return new Runnable() {
            @Override
            public void run() {
                try {
                    DataInputStream dataInputStream = new DataInputStream(frontendSocket.getInputStream());
                    byte[] buffer = new byte[bufferSize];
                    while (dataInputStream.read(buffer, 0, bufferSize) > 0) {
                        synchronized (feedbackList) {
                            System.out.println("Am primit feedback si il adaugam in lista!");
                            feedbackList.add((NewFileRequestFeedback) Serializer.deserialize(buffer));
                        }
                        break;
                    }
                    dataInputStream.close();
                    frontendSocket.close();
                }
                catch (Exception exception){
                    LoggerService.registerError(GeneralManager.generalManagerIpAddress, "Exceptie la thread-ul de feedback : " + exception.getMessage());
                }
            }
        };
    }

    public NewFileRequestFeedback getFeedback(String userId, String filename){
        synchronized (this.feedbackList) {
            if(feedbackList.size() == 0)
                return null;
            for (NewFileRequestFeedback feedback : feedbackList) {
                if (feedback.getUserId().equals(userId) && feedback.getFilename().equals(filename)) {
                    this.feedbackList.remove(feedback);
                    return feedback;
                }
            }
            return null;
        }
    }

    @Override
    public void run() {
        try {
            ServerSocket feedbackSocket = new ServerSocket();
            feedbackSocket.bind(new InetSocketAddress(GeneralManager.generalManagerIpAddress, feedbackPort));
            while(true) {
                Socket socket = feedbackSocket.accept();
                System.out.println("Feedback nou de la frontend!");
                new Thread(feedbackThread(socket)).start();
            }
        }
        catch (Exception exception){
            LoggerService.registerError(GeneralManager.generalManagerIpAddress,
                    "Exceptie la managerul de feedback :  " + exception.getMessage());
        }
    }
}

