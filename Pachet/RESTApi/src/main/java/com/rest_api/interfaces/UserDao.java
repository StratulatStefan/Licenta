package com.rest_api.interfaces;

import com.rest_api.model.User;

import java.util.Map;

/**
 * <ul>
 * 	<li>Interfata care expune toate operatiile de tip CRUD specifice obiectului <strong>User</strong>.</li>
 * </ul>
 */
public interface UserDao {
    /**
     * ============== CREATE ==============
     */
    /**
     * <ul>
     * 	<li>Crearea si persistarea unui nou utilizator in baza de date.</li>
     * 	<li> Se va returna statusul operatiei de persistare.</li>
     * </ul>
     */
    int insertUser(User user) throws Exception;

    /**
     * ============== RETRIEVE ==============
     */
    /**
     * <ul>
     * 	<li>Autentificarea unui utilizator.</li>
     * 	<li> Autentificarea se realizeaza prin extragerea datelor utilizatorului din baza de date si compararea cu datele furnizate.</li>
     * 	<li> Daca datele corespund, autentificarea a reusit si se va intoarce <strong>JWT</strong>-ul catre client.</li>
     * 	<li> Autentificarea se baza <strong>email</strong>-ului si a <strong>parolei</strong>.</li>
     * </ul>
     */
    Map<String, String> login(String username, String password) throws Exception;
    /**
     * <ul>
     * 	<li>Extragerea unui utilizator pe baza identificatorului unic.</li>
     * </ul>
     */
    User getUserById(int id_user) throws NullPointerException;
    /**
     * <ul>
     * 	<li>Extragerea unui utilizator pe baza adresei de email.</li>
     * </ul>
     */
    User getUserByUsername(String email) throws Exception;
    /**
     * <ul>
     * 	<li>Extragerea locatiei utilizatorului.</li>
     * </ul>
     */
    String getUserCountry(int id_user) throws Exception;
    /**
     * <ul>
     * 	<li>Extragerea tipului utilizatorului.</li>
     * </ul>
     */
    String getUserType(int id_user) throws Exception;
    /**
     * <ul>
     * 	<li>Extragerea cantitatii de stocare disponibile utilizatorului.</li>
     * </ul>
     */
    long getUserStorageQuantity(int id_user) throws Exception;
    /**
     * <ul>
     * 	<li>Extragerea factorului de replicare.</li>
     * </ul>
     */
    int getReplicationFactor(int id_user) throws Exception;

    /**
     * ============== UPDATE ==============
     */
    /**
     * <ul>
     * 	<li>Actualizarea cantitatii de stocare disponibila.</li>
     * </ul>
     */
    void updateStorageQuantity(int id_user, int quantity) throws Exception;
    /**
     * <ul>
     * 	<li>Actualizarea locatiei utilizatorului.</li>
     * </ul>
     */
    void updateCountry(int id_user, String country) throws Exception;
    /**
     * <ul>
     * 	<li>Actualizarea parolei utilizatorului.</li>
     * </ul>
     */
    void updatePassword(int id_user, String password) throws Exception;
    /**
     * <ul>
     * 	<li>Actualizarea tipului utilizatorului.</li>
     * </ul>
     */
    void updateType(int id_user, String type) throws Exception;
    /**
     * <ul>
     * 	<li>Actualizarea numarului de fisiere ale utilizatorului.</li>
     * </ul>
     */
    void updateNumberOfFiles(int id_user, int count) throws Exception;

    /**
     * ============== DELETE ==============
     */
    /**
     * <ul>
     * 	<li>Eliminarea unui utilizator pe baza identificatorului unic.</li>
     * </ul>
     */
    void deleteUserById(int id_user) throws Exception;
}
