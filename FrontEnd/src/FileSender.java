import client_node.FileHeader;
import client_node.NewFileRequestFeedback;
import communication.Serializer;
import log.ProfiPrinter;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class FileSender {
    private static final String ipAddress = "127.0.0.100";

    private static final int bufferSize = 1024;

    private static int generalManagerPort = 8081;


    private static boolean validateToken(String token) throws Exception{
        if(token.length() == 0)
            throw new Exception("Null token!");
        String[] tokenItems = token.split("\\-");
        for(String tokenItem : tokenItems){
            String[] values = tokenItem.split("\\.");
            if(values.length != 4)
                throw new Exception("Invalid token! The address is not a valid IP Address (invalid length!)");
            for(String value : values){
                try{
                    int parsedValue = Integer.parseInt(value);
                    if(parsedValue < 0 || parsedValue > 255)
                        throw new Exception("Invalid token! The address is not a valid IP Address (8 bit representation of values)");
                }
                catch (NumberFormatException exception) {
                    throw new Exception("Invalid token! The address is not a valid IP Address (it should only contain numbers!)");
                }
            }
        }
        return true;
    }

    private static String getDestinationIpAddress(String token) throws Exception{
        return getAddressesFromToken(token)[0];
    }

    private static String[] getAddressesFromToken(String token) throws Exception{
        if(validateToken(token))
            return token.replace(" ","").split("\\-");
        return null;
    }

    public static void sendFile(String userId, String token, String filename){
        try {
            String destinationAddress = getDestinationIpAddress(token);
            Socket socket = new Socket(destinationAddress, generalManagerPort);
            File file = new File(filename);
            FileHeader fileHeader = new FileHeader();
            fileHeader.setFilename(filename);
            fileHeader.setToken(token);
            fileHeader.setFilesize(file.length());
            fileHeader.setUserId(userId);

            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            outputStream.write(Serializer.serialize(fileHeader));

            byte[] binaryFile = new byte[bufferSize];
            int count;
            while ((count = inputStream.read(binaryFile)) > 0) {
                outputStream.write(binaryFile, 0, count);
            }

            inputStream.close();
            outputStream.close();
            socket.close();
        }
        catch (Exception exception){
            ProfiPrinter.PrintException("Exceptie la trimiterea unui nou fisier: " + exception.getMessage());
        }
    }

    public static void waitForFeedback(String userId, String token, String filename, long timeout, long CRC) throws InterruptedException {
        int total_nodes = 0;
        int received_nodes = 0;
        final int[] valid_nodes = {0};
        boolean another_exception = false;
        String[] fnamelist = filename.split("/");
        final String fname = fnamelist[fnamelist.length - 1];
        final List<Thread> threads = new ArrayList<>();
        try {
            final List<String> addresses = new LinkedList<String>(Arrays.asList(getAddressesFromToken(token)));
            total_nodes = addresses.size();
            while(received_nodes != total_nodes){
                received_nodes += 1;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            NewFileRequestFeedback feedback;
                            while ((feedback = Frontend.feedbackManager.getFeedback(userId, fname)) == null);
                            String nodeAddress = feedback.getNodeAddress();
                            String fileName = feedback.getFilename();
                            String userID = feedback.getUserId();
                            long crc = feedback.getCrc();
                            System.out.print(String.format("Feedback primit de la : [%s]\n", nodeAddress));
                            if (fileName.equals(fname) && userID.equals(userId) && CRC == crc) {
                                System.out.println(" >> [OK]");
                                valid_nodes[0] += 1;
                            } else {
                                System.out.println(" >> [INVALID]");
                            }
                        }
                        catch (Exception exception){
                            ProfiPrinter.PrintException("Exceptie la primirea feedback-ului! : " + exception.getMessage());
                        }
                    }
                });
                threads.add(thread);
                thread.start();
            }
        }
        catch (Exception exception){
            ProfiPrinter.PrintException("Exceptie : " + exception.getMessage());
            another_exception = true;
        }
        finally {
            System.out.println("Am primit feedback de la " + received_nodes + "/" + total_nodes);
            for(Thread thread : threads){
                if(thread.isAlive()){
                    thread.join();
                }
            }
            if(another_exception || received_nodes == 0 || valid_nodes[0] == 0) {
                sendFeedbackToGM(userId, fname, "ERROR");
                return;
            }
            if(valid_nodes[0] >= 1){
                System.out.println("Sending confirmation feedback to general manager");
                sendFeedbackToGM(userId, fname, "OK");
            }
        }
    }

    public static void sendFeedbackToGM(String userId, String filename, String status){
        new Thread(new Runnable() {
            @Override
            public void run() {
                NewFileRequestFeedback feedback = new NewFileRequestFeedback();
                feedback.setFilename(filename);
                feedback.setUserId(userId);
                feedback.setStatus(status);
                try{
                    Socket frontendSocket = new Socket("127.0.0.1", 8010);
                    DataOutputStream dataOutputStream = new DataOutputStream(frontendSocket.getOutputStream());

                    dataOutputStream.write(Serializer.serialize(feedback));

                    dataOutputStream.close();
                    frontendSocket.close();
                }
                catch (IOException exception){
                    ProfiPrinter.PrintException("Exceptie IO la sendFeedBackToGM : " + exception.getMessage());
                }
            }
        }).start();
    }
}
