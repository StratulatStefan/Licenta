package com.dropbox.interfaces;

import com.dropbox.model.User;

public interface UserDao {
    /**
     * ============== CREATE ==============
     */
    int insertUser(User user) throws Exception;


    /**
     * ============== RETRIEVE ==============
     */
    User getUserById(int id_user) throws NullPointerException;
    User getUserByUsername(String email) throws Exception;
    String getUserCountry(int id_user) throws Exception;
    long getUserStorageQuantity(int id_user) throws Exception;
    int getReplicationFactor(int id_user) throws Exception;


    /**
     * ============== UPDATE ==============
     */
    void updateStorageQuantity(int id_user, int quantity) throws Exception;
    void updateCountry(int id_user, String country) throws Exception;
    void updatePassword(int id_user, String password) throws Exception;


    /**
     * ============== DELETE ==============
     */
    void deleteUserById(int id_user) throws Exception;
}