package com.dropbox.frontend_proxy_ui.services;

import client_manager.data.NewFileRequest;
import com.dropbox.frontend_proxy_ui.proxy.FrontendManager;
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

    public void uploadFile(MultipartFile file, int userId, String description, String userType) throws Exception {
        String filePath = persistFileToBuffer(file);

        NewFileRequest newFileRequest = new NewFileRequest();
        newFileRequest.setUserId(String.format("%d", userId));
        newFileRequest.setFilename(filePath);
        newFileRequest.setFilesize(file.getSize());
        newFileRequest.setCrc(FileSystem.calculateCRC(filePath));
        newFileRequest.setUserType(userType);
        newFileRequest.setDescription(description);

        FrontendManager.mainActivity(newFileRequest);
    }
}
