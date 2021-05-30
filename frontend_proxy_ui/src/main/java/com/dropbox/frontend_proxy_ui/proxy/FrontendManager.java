package com.dropbox.frontend_proxy_ui.proxy;

import client_manager.ManagerComplexeResponse;
import client_manager.ManagerResponse;
import client_manager.ManagerTextResponse;
import client_manager.data.*;
import communication.Serializer;
import log.ProfiPrinter;
import os.FileSystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class FrontendManager {
    private static final int bufferSize = 1024;
    private static String generalManagerAddress = "127.0.0.1";
    private static int generalManagerPort = 8081;

    public static ManagerResponse managerOperationRequest(ClientManagerRequest clientRequest) throws NullPointerException, IOException, ClassNotFoundException {
        String op = "";
        Class operation = clientRequest.getClass();
        if(operation == GetUserFiles.class){
            op = "[GET USER FILES]";
        }
        else if(operation == GetUserFileHistory.class){
            op = "[GET USER FILE HISTORY]";
        }
        else if(operation == GetNodeForDownload.class){
            op = "[GET NODE CANDIDATE FOR DOWNLOAD]";
        }
        else {
            String[] fname = clientRequest.getFilename().split("\\\\");
            clientRequest.setFilename(fname[fname.length - 1]);

            if (operation == NewFileRequest.class) {
                op = "[NEW FILE]";
            } else if (operation == DeleteFileRequest.class) {
                op = "[DELETE FILE]";
            } else if (operation == RenameFileRequest.class) {
                op = "[RENAME FILE]";
            }
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
            ManagerResponse userResponse = null;
            while(socketInputStream.read(buffer, 0, bufferSize) > 0){
                if(operation == GetUserFiles.class){
                    userResponse = (ManagerComplexeResponse) Serializer.deserialize(buffer);
                }
                else if(operation == GetUserFileHistory.class){
                    userResponse = (ManagerComplexeResponse) Serializer.deserialize(buffer);
                }
                else {
                    userResponse = (ManagerTextResponse) Serializer.deserialize(buffer);
                    int x = 0;
                }
                break;
            }

            socketInputStream.close();
            socketOutputStream.close();
            generalManagerSocket.close();
            return userResponse;
        }
        catch (NullPointerException | ClassNotFoundException exception){
            socketInputStream.close();
            socketOutputStream.close();
            generalManagerSocket.close();
            if(exception.getClass() == NullPointerException.class){
                throw exception;
            }
            ProfiPrinter.PrintException("Eroare de IO la socketOutputStream : " + exception.getMessage());
        }
        return null;
    }

    public static void mainActivity(ClientManagerRequest requestData){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String fullPath = requestData.getFilename();
                    ManagerResponse response = managerOperationRequest(requestData);

                    Class<? extends ClientManagerRequest> operation = requestData.getClass();
                    if(operation == NewFileRequest.class){
                        String token = ((ManagerTextResponse)response).getResponse();
                        System.out.println("New file request : " + requestData.getFilename() + " -> " + token);
                        requestData.setFilename(fullPath);
                        long start = System.currentTimeMillis();
                        FileSender.sendFile(requestData, token);
                        long timeElapsed = System.currentTimeMillis() - start;
                        long fileCRC = FileSystem.calculateCRC(requestData.getFilename());
                        FileSystem.deleteFile(requestData.getFilename());
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
                    ProfiPrinter.PrintException("Exceptie : " + exception.getMessage());
                }
            }
        }).start();

    }

}
