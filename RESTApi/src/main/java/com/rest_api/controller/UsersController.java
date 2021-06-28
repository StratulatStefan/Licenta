package com.rest_api.controller;

import com.rest_api.interfaces.UserDao;
import com.rest_api.model.User;
import com.rest_api.services.ResponseHandlerService;
import jwt.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * <ul>
 * 	<li>Clasa de tip controller <strong>@RestController</strong> care expune toate metodele HTTP specifice reprezentarii obiectului <strong>User</strong>.</li>
 * 	<li> Se specifica si adresa <strong>@CrossOrigin</strong> : adresa aplicatiei client.</li>
 * 	<li> Toate cererile HTTP vor contine in URI baza <strong>/api/user</strong>.</li>
 * </ul>
 */
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping(value = "/api/user")
public class UsersController {
    /**
     * <ul>
     * 	<li>Injectarea serviciului de tip <strong>UserDao</strong> care va expune toate metodele specifice prelucrarii obiectului.</li>
     * 	<li> Injectarea se va face in mod transparent de catre SpringBoot <strong>@Autowired</strong>.</li>
     * </ul>
     */
    @Autowired
    private UserDao userDao;

    /**
     * <ul>
     * 	<li>Obiectul care gestioneaza autorizarea clientului.</li>
     * 	<li> Se verifica daca utilizatorul are rolul specific cererii.</li>
     * 	<li> Se va furniza header-ul de autorizare <strong>Bearer TOKEN</strong> si lista de utilizatori permisi ai cererii si,
     *       in urma decodarii <strong>JWT</strong>-ului se va decide daca se poate efectua operatia.</li>
     * 	<li> In caz contrar, se intoarce <strong>401 NOT AUTHORIZED</strong>.</li>
     * </ul>
     */
    private AuthorizationService authorizationService = new AuthorizationService();

    /**
     * <ul>
     * 	<li>Functie de mapare a cererii de adaugare a unui nou utilizator</li>
     * 	<li>Operatia este disponibila doar pentru administratorul sistemului.</li>
     * </ul>
     * @param user Obiectul ce contine caracteristicile obiectului
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
     * <ul>
     * 	<li>Functie de mapare a cererii de extragere a unui utilizator, pe baza identificatorului unic.</li>
     * 	<li>Operatia este disponibila doar pentru toti utilizatorii sistemului.</li>
     * </ul>
     * @param id Identificatorul unic al utilizatorului
     * @param authorizationValue Header-ul de autorizare.
     */
    @RequestMapping(value = "/search/{id}", method = RequestMethod.GET)
    ResponseEntity<User> getUserDataById(@PathVariable int id,
                                         @RequestHeader("Authorization") String authorizationValue) {
        int userId = -1;
        try {
            AuthorizationService.UserTypes[] allowedUserTypes = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ALL};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
            userId = Integer.parseInt((String)userData.get("sub"));
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            User user = userDao.getUserById(userId);
            return new ResponseEntity<User>(user, HttpStatus.OK);
        } catch (NullPointerException nullPointerException) {
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * <ul>
     * 	<li>Functie de mapare a cererii de extragere a unui utilizator pe baza adresei de email.</li>
     * 	<li>Operatia este disponibila doar pentru toti utilizatorii sistemului.</li>
     * </ul>
     * @param email Adresa email a utilizatorului.
     * @param authorizationValue Header-ul de autorizare.
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    ResponseEntity<User> getUserData(@RequestParam(name="email", required = false, defaultValue = "") String email,
                                     @RequestHeader("Authorization") String authorizationValue) {
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
            User user = null;
            if(!email.equals("")) {
                user = userDao.getUserByUsername(email);
            }
            else{
                user = userDao.getUserById(userId);
            }
            if(user == null)
                throw new Exception(String.format("User not found!", email));
            return new ResponseEntity<User>(User.usercopy(user), HttpStatus.OK);
        } catch (Exception nullPointerException) {
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * <ul>
     * 	<li>Functie de mapare a cererii de extragere a unui anumit atribut al utilizatorului</li>
     * 	<ul>
     * 	    <li>Tara : <strong>country</strong></li>
     * 	    <li>Factorul de replicare : <strong>replication_factor</strong></li>
     * 	    <li>Cantitatea de stocare disponibila : <strong>storage_quantity</strong></li>
     * 	    <li>Tipul utilizatorului: <strong>role</strong></li>
     * 	</ul>
     * 	<li>Operatia este disponibila doar pentru toti utilizatorii sistemului.</li>
     * </ul>
     * @param field
     * @param authorizationValue Header-ul de autorizare.
     */
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

    /**
     * <ul>
     * 	<li>Functie de mapare a cererii de autentificare a unui utilizator</li>
     * 	<li>Se va returna JWT-ul.</li>>
     * </ul>
     * @param loginCredentials Credentialele de autentificare (email si parola).
     */
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
     * <ul>
     * 	<li>Functie de mapare a cererii de actualizare a unui utilizator</li>
     * 	<li>Operatia este disponibila doar pentru toti utilizatorii sistemului.</li>
     * </ul>
     * @param updateValue Dictionar ce contine atributele ce se doresc a fi actualizate, impreuna cu noile valori.
     * @param authorizationValue Header-ul de autorizare.
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    ResponseEntity<Map<String, String>> updateUser(@RequestBody HashMap<String, Object>   updateValue,
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
     * <ul>
     * 	<li>Functie de mapare a cererii de actualizare a cantitatii de stocare disponibile pentru utilizator.</li>
     * 	<li>Operatia este disponibila doar pentru administratorul sistemului.</li>
     * </ul>
     * @param userId Identificatorul unic al utilizatorului
     * @param updateValue Noua valoare a cantitatii de stocare.
     */
    @RequestMapping(value = "/{userId}/storage", method = RequestMethod.PUT)
    ResponseEntity<Map<String, String>> updateUserStorage(@PathVariable int                    userId,
                                                          @RequestBody HashMap<String, Object> updateValue){
        try{
            for(String key : new ArrayList<>(updateValue.keySet())){
                if(key.equals("storage_quantity_release")){
                    userDao.updateStorageQuantity(userId, (int) updateValue.get(key));
                    userDao.updateNumberOfFiles(userId, -1);
                }
                else if(key.equals("storage_quantity_consumption")){
                    userDao.updateStorageQuantity(userId, -(int) updateValue.get(key));
                    userDao.updateNumberOfFiles(userId, +1);
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
     * <ul>
     * 	<li>Functie de mapare a cererii de eliminare a unui utilizator.</li>
     * 	<li>Operatia este disponibila doar pentru toti utilizatorii sistemului.</li>
     * </ul>
     * @param authorizationValue Header-ul de autorizare.
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    ResponseEntity<Map<String, String>> deleteUser(@RequestHeader("Authorization") String authorizationValue) throws Exception {
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

        try{
            userDao.deleteUserById(userId);
            Map<String, String> statusResponse = ResponseHandlerService.buildSuccessStatus("User successfully deleted!");
            return new ResponseEntity(statusResponse, HttpStatus.OK);
        }
        catch (Exception nullPointerException){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }
}
