package com.rest_api.controller;

import com.rest_api.interfaces.InternalNodeDao;
import com.rest_api.model.InternalNode;
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
 * 	<li>Clasa de tip controller <strong>@RestController</strong> care expune toate metodele HTTP specifice reprezentarii obiectului <strong>InternalNode</strong>.</li>
 * 	<li> Se specifica si adresa <strong>@CrossOrigin</strong> : adresa aplicatiei client.</li>
 * 	<li> Toate cererile HTTP vor contine in URI baza <strong>/api/internalnode</strong>.</li>
 * </ul>
 */
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping(value = "/api/internalnode")
public class InternalNodeController {
    /**
     * <ul>
     * 	<li>Injectarea serviciului de tip <strong>InternalNodeDao</strong> care va expune toate metodele specifice prelucrarii obiectului.</li>
     * 	<li> Injectarea se va face in mod transparent de catre SpringBoot <strong>@Autowired</strong>.</li>
     * </ul>
     */
    @Autowired
    private InternalNodeDao internalNodeDao;

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
     * 	<li>Functia de mapare a cererii de adaugare a unui nou nod intern.</li>
     * 	<li>Operatia este disponibila doar pentru administratorul sistemului.</li>
     * </ul>
     * @param internalNode Obiectul de tip nod intern.
     * @param authorizationValue Header-ul de autorizare
     */
    @RequestMapping(value="", method = RequestMethod.PUT)
    ResponseEntity<Map<String, String>> insertInternalNode(@RequestBody InternalNode internalNode,
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
            internalNodeDao.insertInternalNode(internalNode);
            Map<String, String> successResponse = ResponseHandlerService.buildSuccessStatus("Internal node successfully added!");
            return new ResponseEntity<Map<String, String>>(successResponse, HttpStatus.CREATED);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity<Map<String, String>>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * <ul>
     * 	<li>Functia de mapare a cererii de extragere a tuturor nodurilor interne.</li>
     * 	<li>Operatia este disponibila doar pentru administratorul sistemului.</li>
     * </ul>
     * @param authorizationValue Header-ul de autorizare
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    ResponseEntity<List<InternalNode>> getAllInternalNodes(@RequestHeader("Authorization") String authorizationValue){
        try {
            AuthorizationService.UserTypes[] allowedUserTypes = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ADMIN};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }
        try{
            List<InternalNode> internalNodes = internalNodeDao.getAllInternalNodes();
            return new ResponseEntity<List<InternalNode>>(internalNodes, HttpStatus.OK);
        }
        catch (NullPointerException nullPointerException){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * <ul>
     * 	<li>Functia de mapare a cererii de extragere a unui nod intern pe baza adresei ip, furnizata ca parametru in query.</li>
     * 	<li>Operatia este disponibila doar pentru administratorul sistemului.</li>
     * </ul>
     * @param ipaddress Adresa IP a nodului cautat.
     * @param authorizationValue Header-ul de autorizare
     */
    @RequestMapping(value = "/{ipaddress}", method = RequestMethod.GET)
    ResponseEntity<InternalNode> getInternalNodeByAddress(@PathVariable String ipaddress,
                                                          @RequestHeader("Authorization") String authorizationValue) {
        try {
            AuthorizationService.UserTypes[] allowedUserTypes = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ADMIN};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }
        try {
            InternalNode internalNodes = internalNodeDao.getInternalNode(ipaddress);
            return new ResponseEntity<InternalNode>(internalNodes, HttpStatus.OK);
        } catch (NullPointerException nullPointerException) {
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * <ul>
     * 	<li>Functia de mapare a cererii de extragere a tuturor nodurilor interne dintr-o anumita locatie.</li>
     * 	<li> Operatia este disponibila doar pentru administratorul sistemului.</li>
     * </ul>
     * @param country Tara in care sunt cautate nodurile interne.
     * @param authorizationValue Header-ul de autorizare
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    ResponseEntity<List<InternalNode>> getInternalNodesByCountry(@RequestParam(name="country", defaultValue = "", required = true) String country,
                                                                 @RequestHeader("Authorization") String authorizationValue) {
        try {
            AuthorizationService.UserTypes[] allowedUserTypes = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ADMIN};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }
        try {
            List<InternalNode> internalNodes = internalNodeDao.getInternalNodesByCountry(country);
            return new ResponseEntity<List<InternalNode>>(internalNodes, HttpStatus.OK);
        } catch (NullPointerException nullPointerException) {
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * <ul>
     * 	<li>Functie de mapare a cererii de actualizare a unui nod intern.</li>
     * 	<li> Operatia este disponibila doar pentru administratorul sistemului.</li>
     * </ul>
     * @param ipaddress Adresa ip de identificare a nodului.
     * @param updateValue Dictionar ce contine campurile ce se doresc a fi actualizate, si noile valori.
     * @param authorizationValue Header-ul de autorizare
     */
    @RequestMapping(value = "/{ipaddress}", method = RequestMethod.PUT)
    ResponseEntity<Map<String, String>> updateInternalNode(@PathVariable String ipaddress,
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
                if(key.equals("status")){
                    internalNodeDao.updateInternalNodeStatus(ipaddress, (String) updateValue.get(key));
                }
                else if(key.equals("country")){
                    internalNodeDao.updateInternalNodeCountry(ipaddress, (String)updateValue.get(key));
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
     * 	<li>Functie de mapare a cererii de eliminare a unui nod intern.</li>
     * 	<li>Operatia este disponibila doar pentru administratorul sistemului.</li>
     * </ul>
     * @param ipaddress Adresa IP de identificare a nodului intern.
     * @param authorizationValue Header-ul de autorizare
     */
    @RequestMapping(value = "/{ipaddress}", method = RequestMethod.DELETE)
    ResponseEntity<Map<String, String>> deleteInternalNode(@PathVariable String ipaddress,
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
            internalNodeDao.deleteInternalNode(ipaddress);
            Map<String, String> statusResponse = ResponseHandlerService.buildSuccessStatus("Internal node successfully deleted!");
            return new ResponseEntity(statusResponse, HttpStatus.OK);
        }
        catch (Exception nullPointerException){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }
}
