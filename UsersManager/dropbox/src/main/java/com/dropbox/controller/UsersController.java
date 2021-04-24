package com.dropbox.controller;

import com.dropbox.interfaces.UserDao;
import com.dropbox.model.User;
import com.dropbox.services.ResponseHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/user")
public class UsersController {
    @Autowired
    private UserDao userDao;

    /**
     * ============== CREATE ==============
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    ResponseEntity<Map<String, String>> insertUser(@RequestBody User user){
        try {
            int user_id = userDao.insertUser(user);
            Map<String, String> successResponse = ResponseHandlerService.buildSuccessStatus("User successfully added!");
            successResponse.put("user_id", String.format("%d", user_id));
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
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    ResponseEntity<User> getUserDataById(@PathVariable int id) {
        try {
            User user = userDao.getUserById(id);
            return new ResponseEntity<User>(user, HttpStatus.OK);
        } catch (NullPointerException nullPointerException) {
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    ResponseEntity<User> getUserDataByUsername(@RequestParam(name="email", required = true, defaultValue = "") String email) {
        try {
            User user = userDao.getUserByUsername(email);
            if(user == null)
                throw new Exception(String.format("User with address %s not found!", email));
            return new ResponseEntity<User>(user, HttpStatus.OK);
        } catch (Exception nullPointerException) {
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/{id}/{field}", method = RequestMethod.GET)
    ResponseEntity<User> getUserFieldById(@PathVariable int id, @PathVariable String field) {
        try {
            if(field.equals("country")){
                String country = userDao.getUserCountry(id);
                Map<String, Object> statusResponse = ResponseHandlerService.buildCustomResponse("country", country);
                return new ResponseEntity(statusResponse, HttpStatus.OK);
            }
            if(field.equals("replication_factor")){
                int replication_factor = userDao.getReplicationFactor(id);
                Map<String, Object> statusResponse = ResponseHandlerService.buildCustomResponse("replication_factor", replication_factor);
                return new ResponseEntity(statusResponse, HttpStatus.OK);
            }
            if(field.equals("storage_quantity")){
                long storageQuantity = userDao.getUserStorageQuantity(id);
                Map<String, Object> statusResponse = ResponseHandlerService.buildCustomResponse("storage_quantity", storageQuantity);
                return new ResponseEntity(statusResponse, HttpStatus.OK);
            }
            throw new Exception("Invalid field value!");
        } catch (Exception nullPointerException) {
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }


    /**
     * ============== UPDATE ==============
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    ResponseEntity<Map<String, String>> updateUser(@PathVariable int id, @RequestBody HashMap<String, Object> updateValue){
        try{
            for(String key : new ArrayList<>(updateValue.keySet())){
                if(key.equals("country")){
                    userDao.updateCountry(id, (String) updateValue.get(key));
                }
                else if(key.equals("password")){
                    userDao.updatePassword(id, (String) updateValue.get(key));
                }
                else if(key.equals("storage_quantity_release")){
                    userDao.updateStorageQuantity(id, (int) updateValue.get(key));
                }
                else if(key.equals("storage_quantity_consumption")){
                    userDao.updateStorageQuantity(id, -(int) updateValue.get(key));
                }
                else{
                    throw new NullPointerException("Invalid update field : " + key + "!");
                }
            }
            Map<String, String> statusResponse = ResponseHandlerService.buildSuccessStatus("Field successfully updated!");
            return new ResponseEntity(statusResponse, HttpStatus.OK);
        }
        catch (Exception nullPointerException){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * ============== DELETE ==============
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    ResponseEntity<Map<String, String>> deleteUser(@PathVariable int id){
        try{
            userDao.deleteUserById(id);
            Map<String, String> statusResponse = ResponseHandlerService.buildSuccessStatus("User successfully deleted!");
            return new ResponseEntity(statusResponse, HttpStatus.OK);
        }
        catch (Exception nullPointerException){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }
}
