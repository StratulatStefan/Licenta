import client_node.FileHeader;
import communication.Serializer;

import java.io.*;
import java.net.Socket;

public class FileSender {
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
        if(validateToken(token))
            return token.replace(" ","").split("\\-")[0];
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
            System.out.println("Exceptie la trimiterea unui nou fisier: " + exception.getMessage());
        }
    }
}
