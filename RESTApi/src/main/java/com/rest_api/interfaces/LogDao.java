package com.rest_api.interfaces;

import com.rest_api.model.Log;


import java.util.HashMap;
import java.util.List;

public interface LogDao {
    /**
     * ============== CREATE ==============
     */
    int insertLogRegister(Log log) throws Exception;


    /**
     * ============== RETRIEVE ==============
     */
    Log getLogRegisterById(int registerId) throws NullPointerException;
    List<Log> getLogRegistersByCriteria(HashMap<String, Object> criteria) throws NullPointerException;


    /**
     * ============== DELETE ==============
     */
    void deleteLogRegister(int registerId) throws Exception;
    void deleteLogRegisterByCriteria(HashMap<String, Object> criteria) throws Exception;
    void deleteAll() throws Exception;

}
