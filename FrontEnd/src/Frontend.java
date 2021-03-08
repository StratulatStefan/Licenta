import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import communication.FileHeader;

public class Frontend {
    private static final int bufferSize = 1024;

    private static String generalManagerAddress = "127.0.0.1";

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
        if(validateToken(token))
            return token.replace(" ","").split("\\-")[0];
        return null;
    }

    private static void sendFile(Socket socket, String userId, String token, String filename){
        try {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            File file = new File(filename);
            FileHeader fileHeader = new FileHeader();
            fileHeader.setFilename(filename);
            fileHeader.setToken(token);
            fileHeader.setFilesize(file.length());
            fileHeader.setUserId(userId);
            byte[] binaryFile = new byte[bufferSize];
            byte[] header = fileHeader.toString().getBytes();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            int count;
            outputStream.write(header);
            while ((count = bufferedInputStream.read(binaryFile)) > 0) {
                outputStream.write(binaryFile, 0, count);
            }

            bufferedInputStream.close();
            outputStream.close();
            socket.close();
        }
        catch (IOException exception){
            System.out.println(exception.getMessage());
        }
    }

    public static String getToken(String userId, String filename, int filesize, int replication_factor){
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("user " + userId + "|");
        messageBuilder.append("operation newfile|");
        String[] fname = filename.split("/");
        messageBuilder.append("filename " + fname[fname.length - 1] + "|");
        messageBuilder.append("filesize " + filesize + "|");
        messageBuilder.append("replication_factor " + replication_factor);
        System.out.println(messageBuilder.toString());
        try {
            Socket generalManagerSocket = new Socket(generalManagerAddress, generalManagerPort);

            DataOutputStream socketOutputStream = new DataOutputStream(generalManagerSocket.getOutputStream());
            System.out.println("Trimit cerere pentru token...");
            socketOutputStream.write(messageBuilder.toString().getBytes());

            DataInputStream socketInputStream = new DataInputStream(generalManagerSocket.getInputStream());
            byte[] buffer = new byte[bufferSize];
            int read;
            String response = null;
            while((read = socketInputStream.read(buffer, 0, bufferSize)) > 0){
                response = new String(buffer, StandardCharsets.UTF_8).substring(0, read);
                break;
            }

            socketInputStream.close();
            socketOutputStream.close();
            generalManagerSocket.close();
            return response;
        }
        catch (IOException exception){
            System.out.println("Eroare de IO la socketOutputStream : " + exception.getMessage());
        }
        return null;
    }

    public static void mainActivity(String userId, String filename, int filesize, int replication_factor){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String response = getToken(userId, filename, filesize, replication_factor);
                    if(response.contains("FILE ALREADY EXISTS")){
                        System.out.println("File already exists!");
                    }
                    else {
                        String token = response;
                        System.out.println(filename + " -> " + token);
                        String destinationAddress = getDestinationIpAddress(token);
                        Socket socket = new Socket(destinationAddress, 8081);
                        sendFile(socket, userId, token, filename);
                    }
                }
                catch (Exception exception){
                    System.out.println("Ceva exceptie : " + exception.getMessage());
                }
            }
        }).start();
    }

    public static void main(String[] args){
        String userId = "3";
        String filename = "D:/Facultate/Licenta/test_files/sss.pdf";
        int filesize = 12;
        int replication_factor = 5;
        mainActivity(userId, filename, filesize, replication_factor);
           //mainActivity("D:/Facultate/Licenta/Dropbox/FrontEnd/src/test_files/Dangerous.mp3", 1);
            //mainActivity("D:/Facultate/Licenta/Dropbox/FrontEnd/src/test_files/Resurse-lab 02-20201012.zip", 2);
    }
}
