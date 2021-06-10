package com.rest_api.interfaces;

import com.rest_api.model.UserType;

import java.util.List;

public interface UserTypeDao {
    /**
     * ============== CREATE ==============
     */
    void insertUserType(UserType userType) throws Exception;


    /**
     * ============== RETRIEVE ==============
     */
    List<UserType> getAllUserTypes();
    UserType getUserTypeData(String usertype);
    int getReplicationFactor(String usertype);
    long getAvailableStorage(String usertype);


    /**
     * ============== UPDATE ==============
     */
    void updateReplicationFactor(String userype, int new_replicationFactor);
    void updateAvailableStorage(String userype, long availableStorage);


    /**
     * ============== DELETE ==============
     */
    void deleteUserType(String usertype) throws NullPointerException;

}
