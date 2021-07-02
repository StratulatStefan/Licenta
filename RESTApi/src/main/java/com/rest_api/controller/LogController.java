package com.rest_api.controller;

import com.rest_api.interfaces.LogDao;
import com.rest_api.model.Log;
import com.rest_api.services.ResponseHandlerService;
import jwt.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <ul>
 * 	<li>Clasa de tip controller <strong>@RestController</strong> care expune toate metodele HTTP specifice reprezentarii obiectului <strong>Log</strong>.</li>
 * 	<li> Se specifica si adresa <strong>@CrossOrigin</strong> : adresa aplicatiei client.</li>
 * 	<li> Toate cererile HTTP vor contine in URI baza <strong>/api/log</strong>.</li>
 * </ul>
 */
@CrossOrigin(origins = "http://localhost:5000")
@RestController
@RequestMapping(value = "/api/log")
public class LogController {
    /**
     * <ul>
     * 	<li>Injectarea serviciului de tip <strong>LogDao</strong> care va expune toate metodele specifice prelucrarii obiectului.</li>
     * 	<li> Injectarea se va face in mod transparent de catre SpringBoot <strong>@Autowired</strong>.</li>
     * </ul>
     */
    @Autowired
    private LogDao logDao;

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
     * 	<li>Functie de mapare a cererii de jurnalizare a unui eveniment</li>
     * </ul>
     * @param log Obiectul de tip eveniment ce se doreste a fi jurnalizat.
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    ResponseEntity<Map<String, String>> insertLogRegister(@RequestBody Log log){
        try {
            int logRegisterId = logDao.insertLogRegister(log);
            Map<String, String> successResponse = ResponseHandlerService.buildSuccessStatus("User successfully added!");
            successResponse.put("register_id", String.format("%d", logRegisterId));
            return new ResponseEntity<Map<String, String>>(successResponse, HttpStatus.CREATED);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity<Map<String, String>>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * <ul>
     * 	<li>Functie de mapare a cererii de extragere a unui eveniment, pe baza identificatorului unic,</li>
     * 	<li>Operatia este disponibila doar pentru administratorul sistemului.</li>
     * </ul>
     * @param id Identificatorul unic al unui eveniment.
     * @param authorizationValue Header-ul de autorizare.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    ResponseEntity<Log> getLogRegisterById(@PathVariable int id,
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
            Log log = logDao.getLogRegisterById(id);
            return new ResponseEntity<Log>(log, HttpStatus.OK);
        } catch (NullPointerException nullPointerException) {
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * <ul>
     * 	<li>Functie de mapare a cererii de extragere a unor evenimente pe baza anumitor criterii.</li>
     * 	<li>Operatia este disponibila doar pentru administratorul sistemului.</li>
     * 	<li><strong>! Functionalitatea specifica extragerii pe baza datelor calendaristice nu este completa !</strong></li>
     * </ul>
     * @param nodeAddress Adresa IP a nodului la nivelul caruia a fost generat evenimentul.
     * @param messageType Tipul evenimentului.
     * @param date1 Limita stanga a intervalul de referinta calendaristic.
     * @param date2 Limita dreapta a intervalul de referinta calendaristic.
     * @param authorizationValue Header-ul de autorizare.
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    ResponseEntity<List<Log>> getLogRegisterByCriteria(@RequestParam(name = "node_address", required = false, defaultValue = "") String nodeAddress,
                                                       @RequestParam(name = "message_type", required = false, defaultValue = "") String messageType,
                                                       @RequestParam(name = "date1", required = false, defaultValue = "") String date1,
                                                       @RequestParam(name = "date2", required = false, defaultValue = "") String date2,
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
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            HashMap<String, Object> criterias = new HashMap<>(){{
                if(!nodeAddress.equals(""))
                    put("node_address", nodeAddress);
                if(!messageType.equals(""))
                    put("message_type", messageType);
                if(!date1.equals(""))
                    put("date1", dateFormat.parse(date1));
                if(!date2.equals(""))
                    put("date1", dateFormat.parse(date2));
            }};
            List<Log> log = logDao.getLogRegistersByCriteria(criterias);
            return new ResponseEntity<List<Log>>(log, HttpStatus.OK);
        } catch (NullPointerException nullPointerException) {
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
        catch (ParseException exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus("Could not parse given date");
            return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * <ul>
     * 	<li>Functie de mapare a cererii de eliminare a unei inregistrari pe baza identificatorului unic.</li>
     * 	<li>Operatia este disponibila doar pentru administratorul sistemului.</li>
     * </ul>
     * @param id Identificatorul unic al unui eveniment.
     * @param authorizationValue Header-ul de autorizare.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    ResponseEntity<Map<String, String>> deleteLogRegister(@PathVariable int id,
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
            logDao.getLogRegisterById(id);
            Map<String, String> statusResponse = ResponseHandlerService.buildSuccessStatus("Log register successfully deleted!");
            return new ResponseEntity(statusResponse, HttpStatus.OK);
        }
        catch (Exception nullPointerException){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * <ul>
     * 	<li>Functie de mapare a cererii de eliminare a unei inregistrari pe baza anumitor criterii.</li>
     * 	<li>Operatia este disponibila doar pentru administratorul sistemului.</li>
     * </ul>
     * @param nodeAddress Adresa IP a nodului la nivelul caruia a fost generat evenimentul.
     * @param messageType Tipul evenimentului.
     * @param date1 Limita stanga a intervalul de referinta calendaristic.
     * @param date2 Limita dreapta a intervalul de referinta calendaristic.
     * @param authorizationValue Header-ul de autorizare.
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    ResponseEntity<Map<String, String>> deleteLogRegByCriteria(@RequestParam(name = "node_address", required = false, defaultValue = "") String nodeAddress,
                                                          @RequestParam(name = "message_type", required = false, defaultValue = "") String messageType,
                                                          @RequestParam(name = "date1", required = false, defaultValue = "") String date1,
                                                          @RequestParam(name = "date2", required = false, defaultValue = "") String date2,
                                                          @RequestHeader("Authorization") String authorizationValue) {
        try {
            AuthorizationService.UserTypes[] allowedUserTypes = new AuthorizationService.UserTypes[]{AuthorizationService.UserTypes.ADMIN};
            Map<String, Object> userData = authorizationService.userAuthorization(authorizationValue, allowedUserTypes);
        }
        catch (Exception exception){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(exception.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd/hh:mm");
            HashMap<String, Object> criterias = new HashMap<>(){{
                if(!nodeAddress.equals(""))
                    put("node_address", nodeAddress);
                if(!messageType.equals(""))
                    put("message_type", messageType);
                if(!date1.equals(""))
                    put("date1", dateFormat.parse(date1));
                if(!date2.equals(""))
                    put("date1", dateFormat.parse(date2));
            }};
            if(criterias.size() == 0){
                logDao.deleteAll();
            }
            else {
                logDao.deleteLogRegisterByCriteria(criterias);
            }
            Map<String, String> statusResponse = ResponseHandlerService.buildSuccessStatus("Log registers successfully deleted!");
            return new ResponseEntity(statusResponse, HttpStatus.OK);
        }
        catch (Exception nullPointerException){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }
}
