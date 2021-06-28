package com.rest_api.interfaces;

import com.rest_api.model.Log;


import java.util.HashMap;
import java.util.List;

/**
 * <ul>
 * 	<li>Interfata care expune toate operatiile de tip CRUD specifice obiectului <strong>Log</strong>.</li>
 * </ul>
 */
public interface LogDao {
    /**
     * ============== CREATE ==============
     */
    /**
     * Crearea si persistarea unui nou eveniment, in baza de date.
     * @param log Obiectul de tip eveniment.
     * @return Statusul adaugarii.
     */
    int insertLogRegister(Log log) throws Exception;

    /**
     * ============== RETRIEVE ==============
     */
    /**
     * <ul>
     * 	<li>Extragerea unui eveniment pe baza identificatorului unic.</li>
     * </ul>
     */
    Log getLogRegisterById(int registerId) throws NullPointerException;
    /**
     * <ul>
     * 	<li>Extragerea unui eveniment pe baza mai multor criterii</li>
     * </ul>
     */
    List<Log> getLogRegistersByCriteria(HashMap<String, Object> criteria) throws NullPointerException;

    /**
     * ============== DELETE ==============
     */
    /**
     * <ul>
     * 	<li>Eliminarea unui eveniment pe baza identificatorului unic.</li>
     * </ul>
     */
    void deleteLogRegister(int registerId) throws Exception;
    /**
     * <ul>
     * 	<li>Eliminarea unuia sau mai multor evenimente, pe baza unor criterii</li>
     * </ul>
     */
    void deleteLogRegisterByCriteria(HashMap<String, Object> criteria) throws Exception;
    /**
     * <ul>
     * 	<li>Eliminarea tuturor evenimentelor.</li>
     * </ul>
     */
    void deleteAll() throws Exception;

}
