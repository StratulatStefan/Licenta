import java.io.*;
import java.net.Socket;

public class Frontend {
    private static final int bufferSize = 1024;
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

    public static void mainActivity(String token, String filename){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
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
        String token = "127.0.0.1-127.0.0.2-127.0.0.3-127.0.0.4";
        mainActivity(token, "D:/Facultate/Licenta/Dropbox/FrontEnd/src/test_files/sss.pdf");
        token = "127.0.0.2-127.0.0.3-127.0.0.1";
        mainActivity(token, "D:/Facultate/Licenta/Dropbox/FrontEnd/src/test_files/Dangerous.mp3");
        token = "127.0.0.3-127.0.0.1-127.0.0.2";
        mainActivity(token, "D:/Facultate/Licenta/Dropbox/FrontEnd/src/test_files/Resurse-lab 02-20201012.zip");
    }
}
