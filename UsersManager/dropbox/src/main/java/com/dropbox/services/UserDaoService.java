package com.dropbox.services;

import com.dropbox.interfaces.UserDao;
import com.dropbox.interfaces.UserTypeDao;
import com.dropbox.jwt.AuthorizationService;
import com.dropbox.model.User;
import com.dropbox.model.UserType;
import com.dropbox.sql_handler.MySQLManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserDaoService implements UserDao {
    MySQLManager<User> mySQLManager = new MySQLManager<User>();

    @Autowired
    UserTypeDao userTypeDao;


    /**
     * ============== CREATE ==============
     */
    @Override
    public int insertUser(User user) throws Exception{
        User candidate = getUserByUsername(user.getEmail());
        if(candidate != null)
            throw new Exception(String.format("User with address %s already exists!", user.getEmail()));
        UserType userTypeData = userTypeDao.getUserTypeData(user.getType());
        user.setStorage_quantity(userTypeData.getAvailable_storage());
        mySQLManager.insert(user);
        return user.getId();
    }


    /**
     * ============== RETRIEVE ==============
     */
    @Override
    public User getUserById(int id_user) throws NullPointerException {
        User user = mySQLManager.findByPrimaryKey(User.class, id_user);
        if(user == null)
            throw new NullPointerException(String.format("User with id %d not found!", id_user));
        return user;
    }

    @Override
    public User getUserByUsername(String email) throws Exception {
        HashMap<String, Object> findCriteria = new HashMap<>(){{
            put("email", email);
        }};
        List<User> users =  mySQLManager.findByCriteria(User.class, findCriteria)
                .stream()
                .map(user -> (User)user)
                .collect(Collectors.toList());
        if(users.size() == 0)
            return null;
        return users.get(0);
    }

    @Override
    public String getUserCountry(int id_user) throws Exception {
        return getUserById(id_user).getCountry();
    }

    @Override
    public String getUserType(int id_user) throws Exception {
        return getUserById(id_user).getType();
    }

    @Override
    public long getUserStorageQuantity(int id_user) throws Exception {
        return getUserById(id_user).getStorage_quantity();
    }

    @Override
    public int getReplicationFactor(int id_user) throws Exception {
        return userTypeDao.getReplicationFactor(getUserById(id_user).getType());
    }

    @Override
    public Map<String, String> login(String username, String password) throws Exception{
        User candidateUser = getUserByUsername(username);
        if(candidateUser == null)
            throw new Exception("User with email " + username + " not found!");
        if(!candidateUser.getPassword().equals(password))
            throw new Exception("Wrong password!");
        return new HashMap<String, String>(){{
            put("jwt", AuthorizationService.generateUserIdentity(candidateUser.getId(), username, candidateUser.getType()));
            put("name", candidateUser.getName());
        }};
    }


    /**
     * ============== UPDATE ==============
     */
    @Override
    public void updateStorageQuantity(int id_user, int quantity) throws Exception{
        User user = getUserById(id_user);
        long new_quantity = user.getStorage_quantity() + quantity;
        if(new_quantity <= 0){
            throw new Exception("No more storage quantity for user");
        }
        user.setStorage_quantity((long)new_quantity);
        mySQLManager.update(user);
    }

    @Override
    public void updateCountry(int id_user, String country) throws Exception{
        User user = getUserById(id_user);
        user.setCountry(country);
        mySQLManager.update(user);
    }

    @Override
    public void updatePassword(int id_user, String password) throws Exception{
        User user = getUserById(id_user);
        user.setPassword(password);
        mySQLManager.update(user);
    }

    @Override
    public void updateType(int id_user, String type) throws Exception{
        User user = getUserById(id_user);
        String currentType = user.getType();
        user.setType(type);
        long availableStorage = this.getUserStorageQuantity(id_user);

        long previousTotal = userTypeDao.getAvailableStorage(currentType);
        long newTotal = userTypeDao.getAvailableStorage(type);

        user.setStorage_quantity(availableStorage + newTotal - previousTotal);
        mySQLManager.update(user);
    }


    /**
     * ============== DELETE ==============
     */
    @Override
    public void deleteUserById(int id) throws Exception {
        int deleteStatus = mySQLManager.remove(User.class, id);
        if(deleteStatus == 1)
            throw new NullPointerException("User with id " + id +" not found!");
    }
}
