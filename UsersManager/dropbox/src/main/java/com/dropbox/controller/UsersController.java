package com.dropbox.controller;

import com.dropbox.interfaces.UserDao;
import com.dropbox.jwt.AuthorizationService;
import com.dropbox.model.User;
import com.dropbox.services.ResponseHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping(value = "/api/user")
public class UsersController {
    @Autowired
    private UserDao userDao;

    AuthorizationService authorizationService = new AuthorizationService();

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
    @RequestMapping(value = "/search/{id}", method = RequestMethod.GET)
    ResponseEntity<User> getUserDataById(@PathVariable int id,
                                         @RequestHeader("Authorization") String authorizationValue) {
        try {
            User user = userDao.getUserById(id);
            return new ResponseEntity<User>(user, HttpStatus.OK);
        } catch (NullPointerException nullPointerException) {
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    ResponseEntity<User> getUserData(@RequestParam(name="email", required = false, defaultValue = "") String email,
                                     @RequestHeader("Authorization") String authorizationValue) {
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
            User user = null;
            if(!email.equals("")) {
                user = userDao.getUserByUsername(email);
            }
            else{
                user = userDao.getUserById(userId);
            }
            if(user == null)
                throw new Exception(String.format("User not found!", email));
            user.setPassword("");
            return new ResponseEntity<User>(user, HttpStatus.OK);
        } catch (Exception nullPointerException) {
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/{field}", method = RequestMethod.GET)
    ResponseEntity<User> getUserFieldById(@PathVariable String field,
                                          @RequestHeader("Authorization") String authorizationValue) {
        int id = -1;
        try {
            AuthorizationService.UserTypes allowedUserTypes[] = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ALL};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
            id = Integer.parseInt((String)userData.get("sub"));
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }
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
            if(field.equals("role")){
                String userType = userDao.getUserType(id);
                Map<String, Object> statusResponse = ResponseHandlerService.buildCustomResponse("role", userType);
                return new ResponseEntity(statusResponse, HttpStatus.OK);
            }
            throw new Exception("Invalid field value!");
        } catch (Exception nullPointerException) {
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    ResponseEntity<Map<String, Object>> login(@RequestBody HashMap<String, String> loginCredentials){
        try {
            Map<String, String> userIdentity = userDao.login(loginCredentials.get("username"), loginCredentials.get("password"));
            Map<String, Object> response = ResponseHandlerService.buildCustomResponse("jwt", userIdentity.get("jwt"));
            response.put("name", userIdentity.get("name"));
            return new ResponseEntity(response, HttpStatus.OK);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * ============== UPDATE ==============
     */
    @RequestMapping(value = "s", method = RequestMethod.PUT)
    ResponseEntity<Map<String, String>> updateUser(@RequestBody HashMap<String, Object> updateValue,
                                                   @RequestHeader("Authorization") String authorizationValue){
        int id = -1;
        try {
            AuthorizationService.UserTypes allowedUserTypes[] = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ALL};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
            id = Integer.parseInt((String)userData.get("sub"));
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }
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
                else if(key.equals("type")){
                    userDao.updateType(id, (String)updateValue.get(key));
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
    ResponseEntity<Map<String, String>> deleteUser(@PathVariable int id,
                                                   @RequestHeader("Authorization") String authorizationValue) throws Exception {
        try {
            AuthorizationService.UserTypes allowedUserTypes[] = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ALL};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }

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
