package com.dropbox.frontend_proxy_ui.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileService {
    private String uploadDir = "D:\\Facultate\\Licenta\\Licenta\\frontend_proxy\\files";

    public void redirectToStorage(MultipartFile file) throws Exception{
        try{
            Path copyLocation = Paths.get(
                    uploadDir + File.separator + file.getOriginalFilename()
            );
            Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception e){
            throw new Exception("Could not store file " + file.getOriginalFilename()
                    + ". Please try again!"
            );
        }
    }
}
