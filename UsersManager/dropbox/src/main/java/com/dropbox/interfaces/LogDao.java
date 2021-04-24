package com.dropbox.interfaces;

import com.dropbox.model.Log;

import java.sql.Date;
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
     * ============== UPDATE ==============
     */
    // -

    /**
     * ============== DELETE ==============
     */
    void deleteLogRegister(int registerId) throws Exception;
    void deleteLogRegisterByCriteria(HashMap<String, Object> criteria) throws Exception;

}
