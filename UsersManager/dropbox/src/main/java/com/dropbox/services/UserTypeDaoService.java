package com.dropbox.services;

import com.dropbox.interfaces.UserTypeDao;
import com.dropbox.model.UserType;
import com.dropbox.sql_handler.MySQLManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserTypeDaoService implements UserTypeDao {
    MySQLManager<UserType> mySQLManager = new MySQLManager<UserType>();

    /**
     * ============== CREATE ==============
     */
    @Override
    public void insertUserType(UserType userType) throws Exception{
        mySQLManager.insert(userType);
    }


    /**
     * ============== RETRIEVE ==============
     */
    @Override
    public List<UserType> getAllUserTypes() throws NullPointerException{
        List<UserType> userTypes = mySQLManager.findlAll(UserType.class);
        if(userTypes.size() == 0){
            throw new NullPointerException("No usertype found!");
        }
        return userTypes;
    }

    @Override
    public UserType getUserTypeData(String usertype) throws NullPointerException{
        UserType userType = (UserType)mySQLManager.findByPrimaryKey(UserType.class, usertype);
        if(userType == null)
            throw new NullPointerException(String.format("Usertype %s not found!", usertype));
        return userType;
    }

    @Override
    public int getReplicationFactor(String usertype) throws NullPointerException{
        UserType userType = (UserType)mySQLManager.findByPrimaryKey(UserType.class, usertype);
        return userType.getReplication_factor();
    }

    @Override
    public long getAvailableStorage(String usertype) throws NullPointerException{
        UserType userType = (UserType)mySQLManager.findByPrimaryKey(UserType.class, usertype);
        return userType.getAvailable_storage();
    }


    /**
     * ============== UPDATE ==============
     */
    @Override
    public void updateReplicationFactor(String userype, int new_replicationFactor) throws NullPointerException{
        UserType candidate = getUserTypeData(userype);
        candidate.setReplication_factor(new_replicationFactor);
        mySQLManager.update(candidate);
    }

    @Override
    public void updateAvailableStorage(String userype, long availableStorage) {
        UserType candidate = getUserTypeData(userype);
        candidate.setAvailable_storage(availableStorage);
        mySQLManager.update(candidate);
    }


    /**
     * ============== DELETE ==============
     */
    @Override
    public void deleteUserType(String usertype) throws NullPointerException {
        int deleteStatus = mySQLManager.remove(UserType.class, usertype);
        if(deleteStatus == 1)
            throw new NullPointerException("Usertype not found!");
    }

}
