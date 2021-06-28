package com.rest_api.controller;

import com.rest_api.interfaces.UserTypeDao;
import com.rest_api.model.UserType;
import com.rest_api.services.ResponseHandlerService;
import jwt.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <ul>
 * 	<li>Clasa de tip controller <strong>@RestController</strong> care expune toate metodele HTTP specifice reprezentarii obiectului <strong>UserType</strong>.</li>
 * 	<li> Se specifica si adresa <strong>@CrossOrigin</strong> : adresa aplicatiei client.</li>
 * 	<li> Toate cererile HTTP vor contine in URI baza <strong>/api/usertype</strong>.</li>
 * </ul>
 */
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping(value = "/api/usertype")
public class UserTypesController {
    /**
     * <ul>
     * 	<li>Injectarea serviciului de tip <strong>UserTypeDao</strong> care va expune toate metodele specifice prelucrarii obiectului.</li>
     * 	<li> Injectarea se va face in mod transparent de catre SpringBoot <strong>@Autowired</strong>.</li>
     * </ul>
     */
    @Autowired
    private UserTypeDao userTypeDao;

    /**
     * <ul>
     * 	<li>Obiectul care gestioneaza autorizarea clientului.</li>
     * 	<li> Se verifica daca utilizatorul are rolul specific cererii.</li>
     * 	<li> Se va furniza header-ul de autorizare <strong>Bearer TOKEN</strong> si lista de utilizatori permisi ai cererii si,
     *       in urma decodarii <strong>JWT</strong>-ului se va decide daca se poate efectua operatia.</li>
     * 	<li> In caz contrar, se intoarce <strong>401 NOT AUTHORIZED</strong>.</li>
     * </ul>
     */
    AuthorizationService authorizationService = new AuthorizationService();

    /**
     * <ul>
     * 	<li>Functie de mapare a cererii de adaugare a unui nou tip de utilizator.</li>
     * 	<li>Operatia este disponibila doar pentru administratorul sistemului.</li>
     * </ul>
     * @param userType Noul tip de utilizator.
     * @param authorizationValue Header-ul de autorizare.
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    ResponseEntity<Map<String, String>> insertUserType(@RequestBody UserType userType,
                                                       @RequestHeader("Authorization") String authorizationValue){
        try {
            AuthorizationService.UserTypes[] allowedUserTypes = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ADMIN};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }
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
     * <ul>
     * 	<li>Functie de mapare a cererii de extragere a unui tip de utilizator, pe baza identificatorului unic./li>
     * 	<li>Operatia este disponibila doar pentru toti utilizatorii sistemului.</li>
     * </ul>
     * @param type Identificatorul unic, reprezentand un sir de caractere cu numele tipului utilizatorului.
     * @param authorizationValue Header-ul de autorizare.
     */
    @RequestMapping(value = "/{type}", method = RequestMethod.GET)
    ResponseEntity<UserType> getUserType(@PathVariable String type,
                                         @RequestHeader("Authorization") String authorizationValue) {
        try {
            AuthorizationService.UserTypes[] allowedUserTypes = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ALL};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }
        try {
            UserType userType = userTypeDao.getUserTypeData(type);
            return new ResponseEntity<UserType>(userType, HttpStatus.OK);
        } catch (NullPointerException nullPointerException) {
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * <ul>
     * 	<li>Functie de mapare a cererii de extragere a unui atribut al tipului utilizatorului.</li>
     * 	<li>Operatia este disponibila doar pentru toti utilizatorii sistemului.</li>
     * 	<ul>
     * 	    <li>Factorul de replicare : <strong>replication_factor</strong></li>
     * 	    <li>Cantitatea de memorie totala disponibila : <strong>available_storage</strong></li>
     * 	</ul>
     * </ul>
     * @param type  Identificatorul unic, reprezentand un sir de caractere cu numele tipului utilizatorului.
     * @param field Atributul cautat.
     * @param authorizationValue Header-ul de autorizare.
     */
    @RequestMapping(value = "/{type}/{field}", method = RequestMethod.GET)
    ResponseEntity<UserType> getUserTypeField(@PathVariable String type,
                                              @PathVariable String field,
                                              @RequestHeader("Authorization") String authorizationValue){
        try {
            AuthorizationService.UserTypes[] allowedUserTypes = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ALL};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }
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

    /**
     * <ul>
     * 	<li>Functie de mapare a cererii de extragere a tuturor tipurilor de utilizatori.</li>
     * 	<li>Operatia este disponibila doar pentru toti utilizatorii sistemului.</li>
     * </ul>
     */
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
     * <ul>
     * 	<li>Functie de mapare a cererii de actualizare a unui tip de utilizator.</li>
     * 	<li>Operatia este disponibila doar pentru administratorul sistemului.</li>
     * </ul>
     * @param type Identificatorul unic, reprezentand un sir de caractere cu numele tipului utilizatorului.
     * @param updateValue Dictionar ce contine atributele ce trebuie actualizate si noile valori.
     * @param authorizationValue Header-ul de autorizare.
     */
    @RequestMapping(value = "/{type}", method = RequestMethod.PUT)
    ResponseEntity<Map<String, String>> updateUserType(@PathVariable String type,
                                                       @RequestBody HashMap<String, Object> updateValue,
                                                       @RequestHeader("Authorization") String authorizationValue){
        try {
            AuthorizationService.UserTypes[] allowedUserTypes = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ADMIN};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }
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
     * <ul>
     * 	<li>Functie de mapare a cererii de eliminare a unui tip de utilizator.</li>
     * 	<li>Operatia este disponibila doar pentru administratorul sistemului.</li>
     * </ul>
     * @param type Identificatorul unic, reprezentand un sir de caractere cu numele tipului utilizatorului.
     * @param authorizationValue Header-ul de autorizare.
     */
    @RequestMapping(value = "/{type}", method = RequestMethod.DELETE)
    ResponseEntity<Map<String, String>> deleteUserType(@PathVariable String type,
                                                       @RequestHeader("Authorization") String authorizationValue){
        try {
            AuthorizationService.UserTypes[] allowedUserTypes = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ADMIN};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }
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
