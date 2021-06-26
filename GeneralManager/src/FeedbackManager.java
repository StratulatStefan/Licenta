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

/**
 * <ul>
 * 	<li>Clasa descrie tot comportamentul mecanismului de feedback.</li>
 * 	<li> Acesta are rolul de a astepta pentru mesaje de feedback de la intermediar in urma anumitor operatii solicitate.</li>
 * 	<li> In cazul operatiei de adaugare a unui nou fisier, se va incepe inregistrarea completa a fisierului.</li>
 * </ul>
 */
public class FeedbackManager implements Runnable{
    /**
     * Lista de feedback, ce va contine mesajele de feedback, pe masura ce sunt primite.
     */
    private final List<NewFileRequestFeedback> feedbackList;
    /**
     * Dimensiunea pachetului de date ce va alcatui mesajul de feedback.
     */
    private static int bufferSize = Integer.parseInt(AppConfig.getParam("buffersize"));
    /**
     * Port-ul de feedback.
     */
    private static int feedbackPort = Integer.parseInt(AppConfig.getParam("feedbackPort"));

    /**
     * Constructorul clasei, care va instantia lista de feedback.
     */
    public FeedbackManager(){
        this.feedbackList = new ArrayList<NewFileRequestFeedback>();
    }

    /**
     * <ul>
     * 	<li>Functie care returneaza <strong>Runnable</strong>-ul, in cadrul caruia se va realiza citirea mesajului de feedback de la client si
     *      inregistrarea acestuia in tabela de feedback.</li>
     * </ul>
     * @param frontendSocket Socket-ul pe care se va realiza comunicarea cu clientul
     */
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

    /**
     * <ul>
     * 	<li>Functia apelata de managerul conexiunii cu clientul, prin care se va solicita obtinerea feedback-ului primit de la client.</li>
     * 	<li>Functia se va apela in mod repetat, pana se va gasi feedback-ul.</li>
     * </ul>
     */
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

    /**
     * <ul>
     * 	<li>Supraincarcarea functiei <strong>run</strong>, care va defini comportamentul managerului de feedback.</li>
     * 	<li>Se va instantia un <strong>ServerSocket</strong>, pe care se va astepta conexiuni de la clienti.</li>
     * 	<li> La conectarea cu clientul, se va porni un nou thread, unde se va interpreta si salva feedback-ul acestuia.</li>
     * </ul>
     */
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

