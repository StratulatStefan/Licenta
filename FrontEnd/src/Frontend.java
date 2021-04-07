import java.io.*;
import java.net.Socket;

import client_manager.ManagerResponse;
import client_manager.data.ClientManagerRequest;
import client_manager.data.DeleteFileRequest;
import client_manager.data.NewFileRequest;
import client_manager.data.RenameFileRequest;
import communication.Serializer;
import os.FileSystem;

public class Frontend {
    private static final int bufferSize = 1024;

    private static String generalManagerAddress = "127.0.0.1";

    private static int generalManagerPort = 8081;

    public static FeedbackManager feedbackManager = new FeedbackManager();

    public static String managerOperationRequest(ClientManagerRequest clientRequest) throws NullPointerException, IOException, ClassNotFoundException {
        String[] fname = clientRequest.getFilename().split("/");
        clientRequest.setFilename(fname[fname.length - 1]);

        String op = "";
        Class operation = clientRequest.getClass();
        if(operation == NewFileRequest.class){
            op = "[NEW FILE]";
        }
        else if(operation == DeleteFileRequest.class){
            op = "[DELETE FILE]";
        }
        else if(operation == RenameFileRequest.class){
            op = "[RENAME FILE]";
        }

        DataInputStream socketInputStream = null;
        DataOutputStream socketOutputStream = null;
        Socket generalManagerSocket = null;
        try {
            generalManagerSocket = new Socket(generalManagerAddress, generalManagerPort);

            socketOutputStream = new DataOutputStream(generalManagerSocket.getOutputStream());
            System.out.println("Trimit cerere pentru " + op + " ...");
            socketOutputStream.write(Serializer.serialize(clientRequest));

            socketInputStream = new DataInputStream(generalManagerSocket.getInputStream());
            byte[] buffer = new byte[bufferSize];
            String response = null;
            while(socketInputStream.read(buffer, 0, bufferSize) > 0){
                ManagerResponse userResponse = (ManagerResponse)Serializer.deserialize(buffer);
                response = userResponse.getResponse();
                break;
            }

            socketInputStream.close();
            socketOutputStream.close();
            generalManagerSocket.close();
            return response;
        }
        catch (NullPointerException | ClassNotFoundException exception){
            socketInputStream.close();
            socketOutputStream.close();
            generalManagerSocket.close();
            if(exception.getClass() == NullPointerException.class){
                throw exception;
            }
            System.out.println("Eroare de IO la socketOutputStream : " + exception.getMessage());
        }
        return null;
    }

    public static void mainActivity(ClientManagerRequest requestData){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String fullPath = requestData.getFilename();
                    String response = managerOperationRequest(requestData);

                    Class<? extends ClientManagerRequest> operation = requestData.getClass();
                    if(operation == NewFileRequest.class){
                        String token = response;
                        System.out.println("New file request : " + requestData.getFilename() + " -> " + token);
                        requestData.setFilename(fullPath);
                        long start = System.currentTimeMillis();
                        FileSender.sendFile(requestData.getUserId(), token, requestData.getFilename());
                        long timeElapsed = System.currentTimeMillis() - start;
                        long fileCRC = FileSystem.calculateCRC(requestData.getFilename());
                        FileSender.waitForFeedback(requestData.getUserId(), token, requestData.getFilename(), timeElapsed, fileCRC);
                    }
                    else if(operation == DeleteFileRequest.class){
                        System.out.println("Delete file request : " + requestData.getFilename() + " -> " + response);
                    }
                    else if(operation == RenameFileRequest.class){
                        System.out.println("Rename file request : " + requestData.getFilename() + " -> " + response);
                    }
                }
                catch (Exception exception){
                    System.out.println("Exceptie : " + exception.getMessage());
                }
            }
        }).start();

    }

    public static void main(String[] args) throws IOException {
        feedbackManager = new FeedbackManager();
        new Thread(feedbackManager).start();

        NewFileRequest newFileRequest = new NewFileRequest();
        newFileRequest.setUserId("1");
        String filepath = "D:/Facultate/Licenta/test_files/lab06.py";
        //String filepath = "D:/Facultate/Licenta/test_files/curs.rar";
        newFileRequest.setFilename(filepath);
        newFileRequest.setFilesize(FileSystem.getFileSize(filepath));
        newFileRequest.setCrc(FileSystem.calculateCRC(filepath));
        newFileRequest.setUserType("STANDARD");
        mainActivity(newFileRequest);

        newFileRequest = new NewFileRequest();
        newFileRequest.setUserId("2");
        filepath = "D:/Facultate/Licenta/test_files/curs.rar";
        newFileRequest.setFilename(filepath);
        newFileRequest.setFilesize(FileSystem.getFileSize(filepath));
        newFileRequest.setCrc(FileSystem.calculateCRC(filepath));
        newFileRequest.setUserType("STANDARD");
        mainActivity(newFileRequest);

        newFileRequest = new NewFileRequest();
        newFileRequest.setUserId("3");
        filepath = "D:/Facultate/Licenta/test_files/sss.pdf";
        newFileRequest.setFilename(filepath);
        newFileRequest.setFilesize(FileSystem.getFileSize(filepath));
        newFileRequest.setCrc(FileSystem.calculateCRC(filepath));
        newFileRequest.setUserType("STANDARD");
        mainActivity(newFileRequest);




        DeleteFileRequest deleteFileRequest = new DeleteFileRequest();
        //deleteFileRequest.setFilename("D:/Facultate/Licenta/test_files/sss.pdf");
        deleteFileRequest.setFilename("D:/Facultate/Licenta/test_files/lab06.py");
        deleteFileRequest.setUserId("1");
        //mainActivity(deleteFileRequest);




        RenameFileRequest renameFileRequest = new RenameFileRequest();
        renameFileRequest.setFilename("curs1.rar");
        renameFileRequest.setUserId("1");
        renameFileRequest.setNewName("curs2.rar");
        //mainActivity(renameFileRequest);




        //userId = "1";
        //replication_factor = 1;
        //filename = "D:/Facultate/Licenta/test_files/Resurse-lab 02-20201012.zip";
        //mainActivity(userId, filename, filesize, replication_factor);

        //userId = "2";
        //replication_factor = 1;
        //filename = "D:/Facultate/Licenta/test_files/Dangerous.mp3";
        //mainActivity(userId, filename, filesize, replication_factor);
    }
}
