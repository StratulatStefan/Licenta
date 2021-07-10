package com.rest_api.interfaces;

import com.rest_api.model.UserType;

import java.util.List;

/**
 * <ul>
 * 	<li>Interfata care expune toate operatiile de tip CRUD specifice obiectului <strong>UserType</strong>.</li>
 * </ul>
 */
public interface UserTypeDao {
    /**
     * ============== CREATE ==============
     */
    /**
     * <ul>
     * 	<li>Crearea si persistarea unui nou tip de utilizator in baza de date.</li>
     * </ul>
     */
    void insertUserType(UserType userType) throws Exception;

    /**
     * ============== RETRIEVE ==============
     */
    /**
     * <ul>
     * 	<li>Extragerea tututor tipurilor de utilizator.</li>
     * </ul>
     */
    List<UserType> getAllUserTypes();
    /**
     * <ul>
     * 	<li>Extragerea datelor despre un anumit tip de utilizator,
     *      pe baza sirului de caractere ce identifica in mod unic tipul</li>
     * </ul>
     */
    UserType getUserTypeData(String usertype);
    /**
     * <ul>
     * 	<li>Extragerea factorului de replicare a unui anumit tip de utilizator,
     *      pe baza sirului de caractere ce identifica in mod unic tipul</li>
     * </ul>
     */
    int getReplicationFactor(String usertype);
    /**
     * <ul>
     * 	<li>Extragerea cantitatii de stocare disponibile unui anumit tip de utilizator,
     *      pe baza sirului de caractere ce identifica in mod unic tipul</li>
     * </ul>
     */
    long getAvailableStorage(String usertype);

    /**
     * ============== UPDATE ==============
     */
    /**
     * <ul>
     * 	<li>Actualizarea factorului de replicare,
     *      pe baza sirului de caractere ce identifica in mod unic tipul</li>
     * </ul>
     */
    void updateReplicationFactor(String userype, int new_replicationFactor);
    /**
     * <ul>
     * 	<li>Actualizarea cantitatii de stocare disponibile,
     *      pe baza sirului de caractere ce identifica in mod unic tipul</li>
     * </ul>
     */
    void updateAvailableStorage(String userype, long availableStorage);

    /**
     * ============== DELETE ==============
     */
    /**
     * <ul>
     * 	<li>Eliminarea unui anumit tip de utilizator,
     *      pe baza sirului de caractere ce identifica in mod unic tipul</li>
     * </ul>
     */
    void deleteUserType(String usertype) throws NullPointerException;

}
