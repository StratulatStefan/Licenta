package com.dropbox.interfaces;

import com.dropbox.model.User;
import com.dropbox.model.UserType;

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
