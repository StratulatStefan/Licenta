package com.rest_api.services;

import com.rest_api.interfaces.LogDao;
import com.rest_api.model.Log;
import com.rest_api.sql_handler.MySQLManager;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <ul>
 * 	<li>Serviciul care expune toate operatiile de tip CRUD specifice obiectului <strong>Log</strong>.</li>
 * </ul>
 */
@Service
public class LogDaoService implements LogDao {
    /**
     * Obiectul de tip <strong>EntityManager</strong> care va asigura comunicarea cu serverul de baze de date.
     * Obiectul va expune toate metodele necesare persistarii, accesari si prelucrarii inregistrarilor din tabela <strong>Log.</strong>
     */
    private MySQLManager<Log> mySQLManager = new MySQLManager<Log>();

    /**
     * ============== CREATE ==============
     */
    /**
     * Crearea si persistarea unui nou eveniment, in baza de date.
     * @param log Obiectul de tip eveniment.
     * @return Statusul adaugarii.
     */
    @Override
    public int insertLogRegister(Log log) throws Exception {
        mySQLManager.insert(log);
        return log.getRegisterId();
    }

    /**
     * ============== RETRIEVE ==============
     */
    /**
     * <ul>
     * 	<li>Extragerea unui eveniment pe baza identificatorului unic.</li>
     * </ul>
     */
    @Override
    public Log getLogRegisterById(int registerId) throws NullPointerException {
        Log logRegister = mySQLManager.findByPrimaryKey(Log.class, registerId);
        if(logRegister == null)
            throw new NullPointerException("Log with id " + registerId + " not found!");
        return logRegister;
    }

    /**
     * <ul>
     * 	<li>Extragerea unui eveniment pe baza mai multor criterii</li>
     * </ul>
     */
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
    /**
     * <ul>
     * 	<li>Eliminarea unui eveniment pe baza identificatorului unic.</li>
     * </ul>
     */
    @Override
    public void deleteLogRegister(int registerId) throws Exception {
        int deleteStatus = mySQLManager.remove(Log.class, registerId);
        if(deleteStatus == 1)
            throw new NullPointerException("Log register with id " + registerId + " not found!");
    }

    /**
     * <ul>
     * 	<li>Eliminarea unuia sau mai multor evenimente, pe baza unor criterii</li>
     * </ul>
     */
    @Override
    public void deleteLogRegisterByCriteria(HashMap<String, Object> criteria) throws Exception {
        List<Log> candidateRegisters = getLogRegistersByCriteria(criteria);
        for(int registerId : candidateRegisters.stream().map(Log::getRegisterId).collect(Collectors.toList())){
            deleteLogRegister(registerId);
        }
    }

    /**
     * <ul>
     * 	<li>Eliminarea tuturor evenimentelor.</li>
     * </ul>
     */
    @Override
    public void deleteAll(){
        mySQLManager.removeAll(Log.class);
    }
}
