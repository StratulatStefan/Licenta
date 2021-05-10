package com.dropbox.controller;

import com.dropbox.interfaces.InternalNodeDao;
import com.dropbox.model.InternalNode;
import com.dropbox.services.ResponseHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping(value = "/api/internalnode")

public class InternalNodeController {
    @Autowired
    private InternalNodeDao internalNodeDao;

    /**
     * ============== CREATE ==============
     */
    @RequestMapping(value="", method = RequestMethod.PUT)
    ResponseEntity<Map<String, String>> insertInternalNode(@RequestBody InternalNode internalNode){
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
     * ============== RETRIEVE ==============
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    ResponseEntity<List<InternalNode>> getAllUserTypes(){
        try{
            List<InternalNode> internalNodes = internalNodeDao.getAllInternalNodes();
            return new ResponseEntity<List<InternalNode>>(internalNodes, HttpStatus.OK);
        }
        catch (NullPointerException nullPointerException){
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/{ipaddress}", method = RequestMethod.GET)
    ResponseEntity<InternalNode> getInternalNodeByAddress(@PathVariable String ipaddress) {
        try {
            InternalNode internalNodes = internalNodeDao.getInternalNode(ipaddress);
            return new ResponseEntity<InternalNode>(internalNodes, HttpStatus.OK);
        } catch (NullPointerException nullPointerException) {
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    ResponseEntity<List<InternalNode>> getInternalNodesByCountry(@RequestParam(name="country", defaultValue = "", required = true) String country) {
        try {
            List<InternalNode> internalNodes = internalNodeDao.getInternalNodesByCountry(country);
            return new ResponseEntity<List<InternalNode>>(internalNodes, HttpStatus.OK);
        } catch (NullPointerException nullPointerException) {
            Map<String, String> errorResponse = ResponseHandlerService.buildErrorStatus(nullPointerException.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
        }
    }


    /**
     * ============== UPDATE ==============
     */
    @RequestMapping(value = "/{ipaddress}", method = RequestMethod.PUT)
    ResponseEntity<Map<String, String>> updateInternalNode(@PathVariable String ipaddress, @RequestBody HashMap<String, Object> updateValue){
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
     * ============== DELETE ==============
     */
    @RequestMapping(value = "/{ipaddress}", method = RequestMethod.DELETE)
    ResponseEntity<Map<String, String>> deleteUserType(@PathVariable String ipaddress){
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
