package com.dropbox.controller;

import com.dropbox.interfaces.UserTypeDao;
import com.dropbox.model.UserType;
import com.dropbox.services.ResponseHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/usertype")
public class UserTypesController {
    @Autowired
    private UserTypeDao userTypeDao;

    /**
     * ============== CREATE ==============
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    ResponseEntity<Map<String, String>> insertUserType(@RequestBody UserType userType){
        try {
            userTypeDao.insertUserType(userType);
            Map<String, String> successResponse = ResponseHandlerService.buildSuccessStatus("User type successfully added!");
            return new ResponseEntity<Map<String, String>>(successResponse, HttpStatus.CREATED);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity<Map<String, String>>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * ============== RETRIEVE ==============
     */
    @RequestMapping(value = "/{type}", method = RequestMethod.GET)
    ResponseEntity<UserType> getUserType(@PathVariable String type) {
        try {
            UserType userType = userTypeDao.getUserTypeData(type);
            return new ResponseEntity<UserType>(userType, HttpStatus.OK);
        } catch (NullPointerException nullPointerException) {
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/{type}/{field}", method = RequestMethod.GET)
    ResponseEntity<UserType> getUserTypeField(@PathVariable String type,
                                         @PathVariable String field){
        try{
            if(field.equals("replication_factor")){
                int replication_factor = userTypeDao.getReplicationFactor(type);
                Map<String, Object> statusResponse = ResponseHandlerService.buildCustomResponse("replication_factor", replication_factor);
                return new ResponseEntity(statusResponse, HttpStatus.OK);
            }
            if(field.equals("available_storage")){
                long replication_factor = userTypeDao.getAvailableStorage(type);
                Map<String, Object> statusResponse = ResponseHandlerService.buildCustomResponse("available_storage", replication_factor);
                return new ResponseEntity(statusResponse, HttpStatus.OK);
            }
            throw new NullPointerException("Invalid field value!");
        }
        catch (NullPointerException nullPointerException){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    ResponseEntity<List<UserType>> getAllUserTypes(){
        try{
            List<UserType> userTypes = userTypeDao.getAllUserTypes();
            return new ResponseEntity<List<UserType>>(userTypes, HttpStatus.OK);
        }
        catch (NullPointerException nullPointerException){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }


    /**
     * ============== UPDATE ==============
     */
    @RequestMapping(value = "/{type}", method = RequestMethod.PUT)
    ResponseEntity<Map<String, String>> updateUserType(@PathVariable String type, @RequestBody HashMap<String, Object> updateValue){
        try{
            for(String key : new ArrayList<>(updateValue.keySet())){
                if(key.equals("replication_factor")){
                    userTypeDao.updateReplicationFactor(type, (int)updateValue.get(key));
                }
                else if(key.equals("available_storage")){
                    userTypeDao.updateAvailableStorage(type, (long)updateValue.get(key));
                }
                else{
                    throw new NullPointerException("Invalid update field : " + key + "!");
                }
            }
            Map<String, String> statusResponse = ResponseHandlerService.buildSuccessStatus("Field successfully updated!");
            return new ResponseEntity(statusResponse, HttpStatus.OK);
        }
        catch (NullPointerException nullPointerException){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }


    /**
     * ============== DELETE ==============
     */
    @RequestMapping(value = "/{type}", method = RequestMethod.DELETE)
    ResponseEntity<Map<String, String>> deleteUserType(@PathVariable String type){
        try{
            userTypeDao.deleteUserType(type);
            Map<String, String> statusResponse = ResponseHandlerService.buildSuccessStatus("User type successfully deleted!");
            return new ResponseEntity(statusResponse, HttpStatus.OK);
        }
        catch (NullPointerException nullPointerException){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }
}