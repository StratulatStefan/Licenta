package com.dropbox.controller;

import com.dropbox.interfaces.LogDao;
import com.dropbox.model.Log;
import com.dropbox.services.ResponseHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/log")
public class LogController {
    @Autowired
    private LogDao logDao;

    /**
     * ============== CREATE ==============
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
     * ============== RETRIEVE ==============
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    ResponseEntity<Log> getLogRegisterById(@PathVariable int id) {
        try {
            Log log = logDao.getLogRegisterById(id);
            return new ResponseEntity<Log>(log, HttpStatus.OK);
        } catch (NullPointerException nullPointerException) {
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    ResponseEntity<List<Log>> getLogRegisterByCriteria(@RequestParam(name = "node_address", required = false, defaultValue = "") String nodeAddress,
                                                       @RequestParam(name = "message_type", required = false, defaultValue = "") String messageType,
                                                       @RequestParam(name = "date1", required = false, defaultValue = "") String date1,
                                                       @RequestParam(name = "date2", required = false, defaultValue = "") String date2) {
        try {
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
     * ============== DELETE ==============
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    ResponseEntity<Map<String, String>> deleteLogRegister(@PathVariable int id){
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

    @RequestMapping(value = "", method = RequestMethod.DELETE)
    ResponseEntity<Map<String, String>> deleteLogRegByCriteria(@RequestParam(name = "node_address", required = false, defaultValue = "") String nodeAddress,
                                                          @RequestParam(name = "message_type", required = false, defaultValue = "") String messageType,
                                                          @RequestParam(name = "date1", required = false, defaultValue = "") String date1,
                                                          @RequestParam(name = "date2", required = false, defaultValue = "") String date2){
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
            logDao.deleteLogRegisterByCriteria(criterias);
            Map<String, String> statusResponse = ResponseHandlerService.buildSuccessStatus("Log registers successfully deleted!");
            return new ResponseEntity(statusResponse, HttpStatus.OK);
        }
        catch (Exception nullPointerException){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

}