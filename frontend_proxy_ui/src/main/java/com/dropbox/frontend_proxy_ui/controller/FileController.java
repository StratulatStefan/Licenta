package com.dropbox.frontend_proxy_ui.controller;

import com.dropbox.frontend_proxy_ui.services.FileService;
import com.dropbox.frontend_proxy_ui.services.ResponseHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

// https://stackabuse.com/uploading-files-with-spring-boot/
@CrossOrigin(origins = "http://localhost:3000")
@Controller
public class FileController {

    @Autowired
    FileService fileService;

    @RequestMapping(path = "/proxy/upload", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> uploadFIle(@RequestParam("file") MultipartFile file,
                                           @RequestHeader("version_description") String descriptionValue,
                                           @RequestHeader("user_type") String userType){
        try {
            fileService.uploadFile(file, descriptionValue, userType);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity<Map<String, String>>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Map<String, String> successResponse =
                ResponseHandlerService.buildSuccessStatus("You successfully uploaded " + file.getOriginalFilename() + "!");

        return new ResponseEntity<Map<String, String>>(successResponse, HttpStatus.OK);
    }
}


