package com.dropbox.frontend_proxy_ui.controller;

import client_manager.ManagerComplexeResponse;
import client_manager.ManagerTextResponse;
import com.dropbox.frontend_proxy_ui.jwt.AuthorizationService;
import com.dropbox.frontend_proxy_ui.model.UploadPendingQueue;
import com.dropbox.frontend_proxy_ui.services.FileService;
import com.dropbox.frontend_proxy_ui.services.ResponseHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// https://stackabuse.com/uploading-files-with-spring-boot/
@CrossOrigin(origins = "http://localhost:3000")
@Controller
public class FileController {
    AuthorizationService authorizationService = new AuthorizationService();

    public static UploadPendingQueue uploadPendingQueue = new UploadPendingQueue();

    @Autowired
    FileService fileService;

    @RequestMapping(path = "/proxy/upload", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> uploadFIle(@RequestParam("file") MultipartFile file,
                                           @RequestHeader("Authorization") String authorizationValue,
                                           @RequestHeader("version_description") String descriptionValue,
                                           @RequestHeader("available_storage") long availableStorage,
                                           @RequestHeader("user_type") String userType){

        int userId = -1;
        try {
            AuthorizationService.UserTypes allowedUserTypes[] = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ALL};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
            userId = Integer.parseInt((String)userData.get("sub"));
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            String filepath = fileService.uploadFile(file, userId, descriptionValue, userType, availableStorage);
            while(!uploadPendingQueue.containsRegister(String.format("%d", userId), filepath));
            uploadPendingQueue.popFromQueue();
            Map<String, String> successResponse =
                    ResponseHandlerService.buildSuccessStatus("You successfully uploaded " + file.getOriginalFilename() + "!");
            return new ResponseEntity<Map<String, String>>(successResponse, HttpStatus.OK);

        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity<Map<String, String>>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(path = "/proxy/files", method = RequestMethod.GET)
    public ResponseEntity<List<HashMap<String, Object>>> getUserFiles(@RequestHeader("Authorization") String authorizationValue){
        int userId = -1;
        try {
            AuthorizationService.UserTypes allowedUserTypes[] = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ALL};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
            userId = Integer.parseInt((String)userData.get("sub"));
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity(fileService.getUserFiles(userId).getResponse(), HttpStatus.OK);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(path = "/proxy/history", method = RequestMethod.GET)
    public ResponseEntity<List<HashMap<String, Object>>> getUserFileHistory(@RequestParam(name="filename", required = false, defaultValue = "") String filename,
                                                                            @RequestHeader("Authorization") String authorizationValue){
        int userId = -1;
        try {
            AuthorizationService.UserTypes allowedUserTypes[] = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ALL};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
            userId = Integer.parseInt((String)userData.get("sub"));
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity(fileService.getUserFileHistory(userId, filename).getResponse(), HttpStatus.OK);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(path = "/proxy/{filename}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteFile(@PathVariable String filename,
                                             @RequestBody HashMap<String, String> description,
                                             @RequestHeader("Authorization") String authorizationValue){
        int userId = -1;
        try {
            AuthorizationService.UserTypes allowedUserTypes[] = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ALL};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
            userId = Integer.parseInt((String)userData.get("sub"));
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            String status = fileService.deleteFile(userId, filename, description.get("description")).getResponse();
            return new ResponseEntity(ResponseHandlerService.buildSuccessStatus(status), HttpStatus.OK);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(path = "/proxy/{filename}", method = RequestMethod.PUT)
    public ResponseEntity<String> renameFile(@PathVariable String filename,
                                             @RequestBody HashMap<String, String> data,
                                             @RequestHeader("Authorization") String authorizationValue){
        int userId = -1;
        try {
            AuthorizationService.UserTypes allowedUserTypes[] = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ALL};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
            userId = Integer.parseInt((String)userData.get("sub"));
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            String status = fileService.renameFile(userId, filename, data.get("newname"), data.get("description")).getResponse();
            return new ResponseEntity(ResponseHandlerService.buildSuccessStatus(status), HttpStatus.OK);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(path = "/proxy/{filename}", method = RequestMethod.GET)
    public ResponseEntity<String> downloadFile(@PathVariable String filename,
                                                     @RequestHeader("Authorization") String authorizationValue){
        int userId = -1;
        try {
            AuthorizationService.UserTypes allowedUserTypes[] = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ALL};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
            userId = Integer.parseInt((String)userData.get("sub"));
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity(ResponseHandlerService.buildSuccessStatus(fileService.downloadFile(userId, filename)), HttpStatus.OK);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}


