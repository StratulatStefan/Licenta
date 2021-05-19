package com.dropbox.frontend_proxy_ui.controller;

import com.dropbox.frontend_proxy_ui.jwt.AuthorizationService;
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

    AuthorizationService authorizationService = new AuthorizationService();

    @Autowired
    FileService fileService;

    @RequestMapping(path = "/proxy/upload", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> uploadFIle(@RequestParam("file") MultipartFile file,
                                           @RequestHeader("Authorization") String authorizationValue,
                                           @RequestHeader("version_description") String descriptionValue,
                                           @RequestHeader("user_type") String userType){

        int userId = -1;
        try {
            AuthorizationService.UserTypes allowedUserTypes[] = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ALL};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
            int x = 0;
            userId = Integer.parseInt((String)userData.get("sub"));
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            fileService.uploadFile(file, userId, descriptionValue, userType);
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


