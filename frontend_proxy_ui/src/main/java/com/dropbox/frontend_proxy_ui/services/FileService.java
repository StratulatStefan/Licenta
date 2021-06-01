package com.dropbox.frontend_proxy_ui.services;

import client_manager.ManagerTextResponse;
import client_manager.data.*;
import client_manager.ManagerComplexeResponse;
import com.dropbox.frontend_proxy_ui.proxy.FileSender;
import com.dropbox.frontend_proxy_ui.proxy.FrontendManager;
import node_manager.DeleteRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import os.FileSystem;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileService {
    private String uploadDir = "D:\\Facultate\\Licenta\\Licenta\\frontend_proxy_ui\\files";

    public String persistFileToBuffer(MultipartFile file) throws Exception{
        try{
            Path copyLocation = Paths.get(
                uploadDir + File.separator + file.getOriginalFilename()
            );
            Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);
            return uploadDir + "\\" + file.getOriginalFilename();
        }
        catch (Exception e){
            throw new Exception("Could not store file " + file.getOriginalFilename()
                    + ". Please try again!"
            );
        }
    }

    public String uploadFile(MultipartFile file, int userId, String description, String userType, long availableStorage) throws Exception {
        long filesize = file.getSize();
        if(filesize > availableStorage){
            throw new Exception("Not enough memory storage!");
        }
        String filePath = persistFileToBuffer(file);

        NewFileRequest newFileRequest = new NewFileRequest();
        newFileRequest.setUserId(String.format("%d", userId));
        newFileRequest.setFilename(filePath);
        newFileRequest.setFilesize(filesize);
        newFileRequest.setCrc(FileSystem.calculateCRC(filePath));
        newFileRequest.setUserType(userType);
        newFileRequest.setDescription(description);

        FrontendManager.mainActivity(newFileRequest);

        return filePath;
    }

    public ManagerComplexeResponse getUserFiles(int userId) throws IOException, ClassNotFoundException {
        GetUserFiles getUserFilesRequest = new GetUserFiles();
        getUserFilesRequest.setUserId(String.format("%d", userId));

        return (ManagerComplexeResponse) FrontendManager.managerOperationRequest(getUserFilesRequest);
    }

    public ManagerComplexeResponse getUserFileHistory(int userId, String filename) throws IOException, ClassNotFoundException {
        GetUserFileHistory getUserFileHistoryRequest = new GetUserFileHistory();
        getUserFileHistoryRequest.setUserId(String.format("%d", userId));
        getUserFileHistoryRequest.setFilename(filename);

        return (ManagerComplexeResponse) FrontendManager.managerOperationRequest(getUserFileHistoryRequest);
    }

    public ManagerTextResponse deleteFile(int userId, String filename, String description) throws IOException, ClassNotFoundException {
        DeleteFileRequest deleteFileRequest = new DeleteFileRequest();
        deleteFileRequest.setUserId(String.format("%d", userId));
        deleteFileRequest.setFilename(filename);
        deleteFileRequest.setDescription(description);

        return (ManagerTextResponse) FrontendManager.managerOperationRequest(deleteFileRequest);
    }

    public ManagerTextResponse renameFile(int userId, String filename, String newname, String description) throws IOException, ClassNotFoundException {
        RenameFileRequest renameFileRequest = new RenameFileRequest();
        renameFileRequest.setUserId(String.format("%d", userId));
        renameFileRequest.setFilename(filename);
        renameFileRequest.setNewName(newname);
        renameFileRequest.setDescription(description);

        return (ManagerTextResponse) FrontendManager.managerOperationRequest(renameFileRequest);
    }

    private ManagerTextResponse getNodeCandidateForFile(int userId, String filename) throws IOException, ClassNotFoundException {
        GetNodeForDownload nodeForDownloadRequest = new GetNodeForDownload();
        nodeForDownloadRequest.setUserId(String.format("%d", userId));
        nodeForDownloadRequest.setFilename(filename);

        return (ManagerTextResponse) FrontendManager.managerOperationRequest(nodeForDownloadRequest);

    }

    public String downloadFile(int userId, String filename) throws IOException, ClassNotFoundException {
        String nodeCandidate = this.getNodeCandidateForFile(userId, filename).getResponse();
        return FileSender.downloadFile(nodeCandidate, String.format("%d", userId), filename);
    }

    public ManagerComplexeResponse getNodesStoringUserFile(String user, String filename) throws IOException, ClassNotFoundException {
        GetNodesForFileRequest getNodesForFileRequest = new GetNodesForFileRequest();
        getNodesForFileRequest.setUserId(user);
        getNodesForFileRequest.setFilename(filename);

        return (ManagerComplexeResponse) FrontendManager.managerOperationRequest(getNodesForFileRequest);
    }
}
