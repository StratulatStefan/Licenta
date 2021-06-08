package com.dropbox.frontend_proxy_ui.proxy;

import client_manager.ManagerComplexeResponse;
import client_manager.ManagerResponse;
import client_manager.ManagerTextResponse;
import client_manager.data.*;
import communication.Serializer;
import model.FileAttributes;
import org.springframework.beans.factory.annotation.Value;
import os.FileSystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FrontendManager {
    @Value("${bufferSize}")
    private static int bufferSize;

    @Value("${generalManagerAddress}")
    private static String generalManagerAddress;

    @Value("${generalManagerPort}")
    private static int generalManagerPort;

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
        else if(operation == GetContentTableRequest.class){
            op = "[GET CONTENT TABLE]";
        }
        else if(operation == GetNodesForFileRequest.class){
            op = "[GET NODES FOR FILE OF USER]";
        }
        else if(operation == GetNodesStorageQuantityRequest.class){
            op = "[GET NODES STORAGE QUANTITY TABLE]";
        }
        else if(operation == GetStorageStatusRequest.class){
            op = "[GET STORAGE STATUS TABLE]";
        }
        else if(operation == GetReplicationStatusRequest.class){
            op = "[GET REPLICATION STATUS TABLE]";
        }
        else if(operation == GetConnectionTableRequest.class) {
            op = "[GET CONNECTION TABLE]";
        }
        else if(operation == DeleteFileFromNodeRequest.class){
            op = "[DELETE FILE FROM INTERNAL NODE]";
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
            List<Class<? extends ClientManagerRequest>> complexeGetOperations = Arrays.asList(
                    GetUserFiles.class,
                    GetUserFileHistory.class,
                    GetContentTableRequest.class,
                    GetNodesForFileRequest.class,
                    GetNodesStorageQuantityRequest.class,
                    GetStorageStatusRequest.class,
                    GetReplicationStatusRequest.class,
                    GetConnectionTableRequest.class
            );
            while(socketInputStream.read(buffer, 0, bufferSize) > 0){
                boolean found_complexe = false;
                for(Class<? extends ClientManagerRequest> complexeOp : complexeGetOperations){
                    if(complexeOp == operation){
                        userResponse = (ManagerComplexeResponse) Serializer.deserialize(buffer);
                        found_complexe = true;
                        break;
                    }
                }
                if(!found_complexe){
                    userResponse = (ManagerTextResponse) Serializer.deserialize(buffer);
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
                        FileSender.waitForFeedback(requestData.getUserId(), token, requestData.getFilename(), timeElapsed, fileCRC);
                        FileSystem.deleteFile(requestData.getFilename());
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
}
