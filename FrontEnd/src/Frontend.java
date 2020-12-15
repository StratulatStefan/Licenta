import java.io.*;
import java.net.Socket;

public class Frontend {
    private static Thread sendFile(String filename, Socket socket){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                    File file = new File(filename);
                    FileHeader fileHeader = new FileHeader();
                    fileHeader.setFilename(filename);
                    fileHeader.setToken("afsaaerava54a4ava");
                    fileHeader.setFilesize(file.length());
                    byte[] binaryFile = new byte[1024];
                    byte[] header = fileHeader.toString().getBytes();
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                    int count;
                    outputStream.write(header);
                    while ((count = bufferedInputStream.read(binaryFile)) > 0) {
                        outputStream.write(binaryFile, 0, count);
                    }

                    bufferedInputStream.close();
                    outputStream.close();
                }
                catch (IOException exception){
                    System.out.println(exception.getMessage());
                }
            }
        });

    }
    public static void mainActivity(String filename){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket("127.0.0.1", 8081);
                    Thread t = sendFile(filename, socket);
                    t.start();
                    t.join();
                    socket.close();
                }
                catch (Exception exception){
                    System.out.println("Ceva exceptie : " + exception.getMessage());
                }
            }
        }).start();
    }
    public static void main(String[] args){
        mainActivity("D:/Facultate/Licenta/Dropbox/FrontEnd/src/sss.pdf");
        mainActivity("D:/Facultate/Licenta/Dropbox/FrontEnd/src/Curs_S12.rar");
        mainActivity("D:/Facultate/Licenta/Dropbox/FrontEnd/src/Resurse-lab 02-20201012.zip");
    }
}
