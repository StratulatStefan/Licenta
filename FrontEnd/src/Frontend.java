import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import client_manager.ClientManagerRequest;
import client_manager.Token;
import client_node.FileHeader;
import communication.Serializer;

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

    private static void sendFile(String destinationAddress, String userId, String token, String filename){
        try {
            Socket socket = new Socket(destinationAddress, generalManagerPort);
            File file = new File(filename);
            FileHeader fileHeader = new FileHeader();
            fileHeader.setFilename(filename);
            fileHeader.setToken(token);
            fileHeader.setFilesize(file.length());
            fileHeader.setUserId(userId);

            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            outputStream.write(Serializer.Serialize(fileHeader));

            byte[] binaryFile = new byte[bufferSize];
            int count;
            while ((count = inputStream.read(binaryFile)) > 0) {
                outputStream.write(binaryFile, 0, count);
            }

            inputStream.close();
            outputStream.close();
            socket.close();
        }
        catch (IOException exception){
            System.out.println("Exceptie la trimiterea unui nou fisier: " + exception.getMessage());
        }
    }

    public static String getToken(String userId, String filename, int filesize, int replication_factor){
        ClientManagerRequest newFileRequest = new ClientManagerRequest();
        newFileRequest.setUserId(userId);
        newFileRequest.setOperation("newfile");
        String[] fname = filename.split("/");
        newFileRequest.setFilename(fname[fname.length - 1]);
        newFileRequest.setFilesize(filesize);
        newFileRequest.setReplication_factor(replication_factor);
        try {
            Socket generalManagerSocket = new Socket(generalManagerAddress, generalManagerPort);

            DataOutputStream socketOutputStream = new DataOutputStream(generalManagerSocket.getOutputStream());
            System.out.println("Trimit cerere pentru token...");
            socketOutputStream.write(Serializer.Serialize(newFileRequest));

            DataInputStream socketInputStream = new DataInputStream(generalManagerSocket.getInputStream());
            byte[] buffer = new byte[bufferSize];
            String response = null;
            while(socketInputStream.read(buffer, 0, bufferSize) > 0){
                Token token = (Token)Serializer.Deserialize(buffer);
                try {
                    response = token.getToken();
                }
                catch (Exception exception){
                    response = exception.getMessage();
                }
                break;
            }

            socketInputStream.close();
            socketOutputStream.close();
            generalManagerSocket.close();
            return response;
        }
        catch (IOException | ClassNotFoundException exception){
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
                        sendFile(destinationAddress, userId, token, filename);
                    }
                }
                catch (Exception exception){
                    System.out.println("Ceva exceptie : " + exception.getMessage());
                }
            }
        }).start();
    }

    public static void main(String[] args){
        String userId = "1";
        String filename = "D:/Facultate/Licenta/test_files/sss.pdf";
        int filesize = 12;
        int replication_factor = 2;
        //mainActivity(userId, filename, filesize, replication_factor);

        userId = "1";
        replication_factor = 1;
        filename = "D:/Facultate/Licenta/test_files/Resurse-lab 02-20201012.zip";
        mainActivity(userId, filename, filesize, replication_factor);

        userId = "2";
        replication_factor = 1;
        filename = "D:/Facultate/Licenta/test_files/Dangerous.mp3";
        //mainActivity(userId, filename, filesize, replication_factor);
    }
}
