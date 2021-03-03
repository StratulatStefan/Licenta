import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class Frontend {
    private static final int bufferSize = 1024;

    private static String generalManagerAddress = "127.0.0.1";

    private static int generalManagerPort = 8081;

    private static Socket generalManagerSocket;

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

    private static void sendFile(Socket socket, String token, String filename){
        try {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            File file = new File(filename);
            FileHeader fileHeader = new FileHeader();
            fileHeader.setFilename(filename);
            fileHeader.setToken(token);
            fileHeader.setFilesize(file.length());
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

    public static String getToken(String filesize, int replication_factor){
        String message = "newfile " + filesize + "|" + "replication " + replication_factor + "";
        try {
            Socket generalManagerSocket = new Socket(generalManagerAddress, generalManagerPort);

            DataOutputStream socketOutputStream = new DataOutputStream(generalManagerSocket.getOutputStream());
            System.out.println("Trimit cerere pentru token...");
            socketOutputStream.write(message.getBytes());

            DataInputStream socketInputStream = new DataInputStream(generalManagerSocket.getInputStream());
            byte[] buffer = new byte[bufferSize];
            int read = 0;
            String token = null;
            while((read = socketInputStream.read(buffer, 0, bufferSize)) > 0){
                token = new String(buffer, StandardCharsets.UTF_8).substring(0, read);
                break;
            }

            socketInputStream.close();
            socketOutputStream.close();
            generalManagerSocket.close();
            return token;
        }
        catch (IOException exception){
            System.out.println("Eroare de IO la socketOutputStream : " + exception.getMessage());
        }
        return "";
    }

    public static void mainActivity(String filename, int replication_factor){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String token = getToken("142141", replication_factor);
                    System.out.println(filename + " -> " + token);
                    String destinationAddress = getDestinationIpAddress(token);
                    Socket socket = new Socket(destinationAddress, 8081);
                    sendFile(socket, token, filename);
                }
                catch (Exception exception){
                    System.out.println("Ceva exceptie : " + exception.getMessage());
                }
            }
        }).start();
    }

    public static void main(String[] args){
           mainActivity("D:/Facultate/Licenta/Dropbox/FrontEnd/src/test_files/sss.pdf", 5);
           //mainActivity("D:/Facultate/Licenta/Dropbox/FrontEnd/src/test_files/Dangerous.mp3", 1);
            //mainActivity("D:/Facultate/Licenta/Dropbox/FrontEnd/src/test_files/Resurse-lab 02-20201012.zip", 2);
    }
}
