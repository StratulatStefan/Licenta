package com.safestorage.proxy;

import client_node.NewFileRequestFeedback;
import communication.Serializer;
import config.AppConfig;

import java.io.DataInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * <ul>
 * 	<li>Clasa care va gestiona tot procesul de feedback.</li>
 * 	<li> Acest proces va rula pe un <strong>thread</strong> separat, motiv pentru care, clasa va mosteni <strong>Runnable</strong>.</li>
 * 	<li>Va expune toate functionalitatile necesare primirii de feedback de la nodurile interne.</li>
 * </ul>
 */
public class FeedbackManager implements Runnable{
    /**
     * Dimensiunea unui pachet de date primit pe canalul de comunicatie, in comunicarea cu nodul intern.
     */
    private int bufferSize   = Integer.parseInt(AppConfig.getParam("buffersize"));
    /**
     * Adresa serverului la care se vor realiza conexiuni cu nodurile interne.
     */
    private String address   = AppConfig.getParam("address");
    /**
     * Portul serverului la care se vor realiza conexiuni cu nodurile interne.
     */
    private int feedbackport = Integer.parseInt(AppConfig.getParam("feedbackport"));

    /**
     * <ul>
     * 	<li>Fiecare cerere va fi precedata de o bucla de asteptare a feedback-ului de la nodurilor interne.</li>
     * 	<li>In aceasta lista cu inregistrari de tip <strong>NewFileRequestFeedback</strong> vor fi salvate feedback-urile nodurilor interne,
     pe masura ce acestea sunt receptionate.</li>
     * 	<li> Dupa ce sunt receptionate toate feedback-urile asteptate, lista va fi golita.</li>
     * </ul>
     */
    private final List<NewFileRequestFeedback> feedbackList;

    /**
     * Constructorul clasei care va instantia lista de feedback.
     */
    public FeedbackManager(){
        this.feedbackList = new ArrayList<NewFileRequestFeedback>();
    }

    /**
     * <ul>
     * 	<li>Functia de tratare a comunicarii cu un nod intern.</li>
     * 	<li> Comunicarea se va realiza pe un <strong>thread</strong> separat pentru a nu bloca comunicarea cu celelalte noduri.</li>
     * 	<li>Se va prelua mesajul de tip <strong>NewFileRequestFeedback</strong> si se va salva in lista de feedback.</li>
     * </ul>
     * @param frontendSocket Socket-ul conexiunii cu clientul.
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
                            System.out.println("Adaugam feedback-ul in lista!");
                            feedbackList.add((NewFileRequestFeedback) Serializer.deserialize(buffer));
                            System.out.println(feedbackList.size());
                        }
                        break;
                    }
                    dataInputStream.close();
                    frontendSocket.close();
                }
                catch (Exception exception){
                    System.out.println("Exceptie la thread-ul de feedback : " + exception.getMessage());
                }
            }
        };
    }

    /**
     * <ul>
     * 	<li>Functie apelata in urma procesului de stocare a unui fisier, prin care se solicita feedback-ul de la nodurile interne.</li>
     * 	<li> Se va cauta in listade feedback o inregistrare care identifica in mod unic un fisier al unui utilizator.</li>
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
     * 	<li>Supraincarcarea functiei <strong>run</strong>, care va defini comportamentul care va fi rulat pe un <strong>thread</strong> separat.</li>
     * 	<li>Se va instantia un <strong>ServerSocket</strong>, pe care se vor astepta conexiuni de la nodurile interne, in cadrul unei bucle.</li>
     * 	<li>Pe masura ce nodurile interne realizeaza conexiunea, se porneste un nou thread pe care se va realiza comunicarea cu acesta.</li>
     * </ul>
     */
    @Override
    public void run() {
        try {
            ServerSocket feedbackSocket = new ServerSocket();
            feedbackSocket.bind(new InetSocketAddress(address, feedbackport));
            while(true) {
                Socket socket = feedbackSocket.accept();
                System.out.println("Feedback nou de la un nod!");
                new Thread(feedbackThread(socket)).start();
            }
        }
        catch (Exception exception){
            System.out.println("Exceptie la managerul de feedback :  " + exception.getMessage());
        }
    }
}
