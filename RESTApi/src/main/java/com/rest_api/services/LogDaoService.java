package com.rest_api.services;

import com.rest_api.interfaces.LogDao;
import com.rest_api.model.Log;
import com.rest_api.sql_handler.MySQLManager;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LogDaoService implements LogDao {
    private MySQLManager<Log> mySQLManager = new MySQLManager<Log>();
    /**
     * ============== CREATE ==============
     */
    @Override
    public int insertLogRegister(Log log) throws Exception {
        mySQLManager.insert(log);
        return log.getRegisterId();
    }

    /**
     * ============== RETRIEVE ==============
     */
    @Override
    public Log getLogRegisterById(int registerId) throws NullPointerException {
        Log logRegister = mySQLManager.findByPrimaryKey(Log.class, registerId);
        if(logRegister == null)
            throw new NullPointerException("Log with id " + registerId + " not found!");
        return logRegister;
    }

    @Override
    public List<Log> getLogRegistersByCriteria(HashMap<String, Object> criteria) throws NullPointerException {
        List<Log> logRegisters =  null;
        if(criteria.size() == 0){
            logRegisters = mySQLManager.findlAll(Log.class);
        }
        else{
            logRegisters = mySQLManager.findByCriteria(Log.class, criteria)
                    .stream()
                    .map(register -> (Log)register)
                    .collect(Collectors.toList());
        }
        if(logRegisters.size() == 0)
            throw new NullPointerException("No valid log register found!");
        if(criteria.containsKey("date1")) {
            logRegisters = logRegisters.stream()
                    .filter(x -> (x.getRegister_date().after((Date) criteria.get("date1"))))
                    .collect(Collectors.toList());
        }
        if(criteria.containsKey("date2")) {
            logRegisters = logRegisters.stream()
                    .filter(x -> (x.getRegister_date().before((Date) criteria.get("date2"))))
                    .collect(Collectors.toList());
        }
        return logRegisters;
    }


    /**
     * ============== DELETE ==============
     */
    @Override
    public void deleteLogRegister(int registerId) throws Exception {
        int deleteStatus = mySQLManager.remove(Log.class, registerId);
        if(deleteStatus == 1)
            throw new NullPointerException("Log register with id " + registerId + " not found!");
    }

    @Override
    public void deleteLogRegisterByCriteria(HashMap<String, Object> criteria) throws Exception {
        List<Log> candidateRegisters = getLogRegistersByCriteria(criteria);
        for(int registerId : candidateRegisters.stream().map(Log::getRegisterId).collect(Collectors.toList())){
            deleteLogRegister(registerId);
        }
    }

    @Override
    public void deleteAll(){
        mySQLManager.removeAll(Log.class);
    }
}
