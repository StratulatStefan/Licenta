package com.rest_api.services;

import com.rest_api.interfaces.PasswordEncrypter;
import com.rest_api.interfaces.UserDao;
import com.rest_api.interfaces.UserTypeDao;
import com.rest_api.model.User;
import com.rest_api.model.UserType;
import com.rest_api.sql_handler.MySQLManager;
import jwt.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <ul>
 * 	<li>Serviciul care expune toate operatiile de tip CRUD specifice obiectului <strong>User</strong>.</li>
 * </ul>
 */
@Service
public class UserDaoService implements UserDao {
    /**
     * Cheia secreta de criptare/decriptare
     */
    private String secreyKey = "SafeStorageSecreyKey";

    /**
     * Obiectul de tip <strong>EntityManager</strong> care va asigura comunicarea cu serverul de baze de date.
     * Obiectul va expune toate metodele necesare persistarii, accesari si prelucrarii inregistrarilor din tabela <strong>InternalNode.</strong>
     */
    MySQLManager<User> mySQLManager = new MySQLManager<User>();

    /**
     * Obiectul de tip <strong>UserTypeDao</strong> care va expune metodele necesare accesari factorului de replicare.
     */
    @Autowired
    UserTypeDao userTypeDao;

    /**
     * Serviciul care va realiza criptarea si decriptarea parolei utilizatorului
     */
    @Autowired
    private PasswordEncrypter passwordEncrypter;


    /**
     * ============== CREATE ==============
     */
    /**
     * <ul>
     * 	<li>Crearea si persistarea unui nou utilizator in baza de date.</li>
     * 	<li> Se va returna statusul operatiei de persistare.</li>
     * </ul>
     */
    @Override
    public int insertUser(User user) throws Exception{
        User candidate = getUserByUsername(user.getEmail());
        if(candidate != null)
            throw new Exception(String.format("User with address %s already exists!", user.getEmail()));
        UserType userTypeData = userTypeDao.getUserTypeData(user.getType());
        user.setStorage_quantity(userTypeData.getAvailable_storage());
        user.setPassword(passwordEncrypter.encrypt(user.getPassword(), secreyKey));
        mySQLManager.insert(user);
        return user.getId();
    }


    /**
     * ============== RETRIEVE ==============
     */
    /**
     * <ul>
     * 	<li>Extragerea unui utilizator pe baza identificatorului unic.</li>
     * </ul>
     */
    @Override
    public User getUserById(int id_user) throws NullPointerException {
        User user = mySQLManager.findByPrimaryKey(User.class, id_user);
        if(user == null)
            throw new NullPointerException(String.format("User with id %d not found!", id_user));
        return user;
    }

    /**
     * <ul>
     * 	<li>Extragerea unui utilizator pe baza adresei de email.</li>
     * </ul>
     */
    @Override
    public User getUserByUsername(String email) throws Exception {
        HashMap<String, Object> findCriteria = new HashMap(){{
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

    /**
     * <ul>
     * 	<li>Extragerea locatiei utilizatorului.</li>
     * </ul>
     */
    @Override
    public String getUserCountry(int id_user) throws Exception {
        return getUserById(id_user).getCountry();
    }

    /**
     * <ul>
     * 	<li>Extragerea tipului utilizatorului.</li>
     * </ul>
     */
    @Override
    public String getUserType(int id_user) throws Exception {
        return getUserById(id_user).getType();
    }

    /**
     * <ul>
     * 	<li>Extragerea cantitatii de stocare disponibile utilizatorului.</li>
     * </ul>
     */
    @Override
    public long getUserStorageQuantity(int id_user) throws Exception {
        return getUserById(id_user).getStorage_quantity();
    }

    /**
     * <ul>
     * 	<li>Extragerea factorului de replicare.</li>
     * </ul>
     */
    @Override
    public int getReplicationFactor(int id_user) throws Exception {
        return userTypeDao.getReplicationFactor(getUserById(id_user).getType());
    }

    /**
     * <ul>
     * 	<li>Autentificarea unui utilizator.</li>
     * 	<li> Autentificarea se realizeaza prin extragerea datelor utilizatorului din baza de date si compararea cu datele furnizate.</li>
     * 	<li> Daca datele corespund, autentificarea a reusit si se va intoarce <strong>JWT</strong>-ul catre client.</li>
     * 	<li> Autentificarea se baza <strong>email</strong>-ului si a <strong>parolei</strong>.</li>
     * </ul>
     */
    @Override
    public Map<String, String> login(String username, String password) throws Exception{
        User candidateUser = getUserByUsername(username);
        if(candidateUser == null)
            throw new Exception("User with email " + username + " not found!");
        String decryptedPassword = passwordEncrypter.decrypt(candidateUser.getPassword(), secreyKey);
        if(!decryptedPassword.equals(password))
            throw new Exception("Wrong password!");
        return new HashMap<String, String>(){{
            put("jwt", AuthorizationService.generateUserIdentity(candidateUser.getId(), username, candidateUser.getType()));
            put("name", candidateUser.getName());
        }};
    }


    /**
     * ============== UPDATE ==============
     */
    /**
     * <ul>
     * 	<li>Actualizarea cantitatii de stocare disponibila.</li>
     * </ul>
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

    /**
     * <ul>
     * 	<li>Actualizarea locatiei utilizatorului.</li>
     * </ul>
     */
    @Override
    public void updateCountry(int id_user, String country) throws Exception{
        User user = getUserById(id_user);
        user.setCountry(country);
        mySQLManager.update(user);
    }

    /**
     * <ul>
     * 	<li>Actualizarea parolei utilizatorului.</li>
     * </ul>
     */
    @Override
    public void updatePassword(int id_user, String password) throws Exception{
        User user = getUserById(id_user);
        user.setPassword(password);
        mySQLManager.update(user);
    }

    /**
     * <ul>
     * 	<li>Actualizarea tipului utilizatorului.</li>
     * </ul>
     */
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
     * <ul>
     * 	<li>Actualizarea numarului de fisiere ale utilizatorului.</li>
     * </ul>
     */
    @Override
    public void updateNumberOfFiles(int id_user, int count) throws Exception {
        User user = getUserById(id_user);
        int currentNumberOfFiles = user.getNumber_of_file();

        user.setNumber_of_file(currentNumberOfFiles + count);
        mySQLManager.update(user);
    }

    /**
     * ============== DELETE ==============
     */
    /**
     * <ul>
     * 	<li>Eliminarea unui utilizator pe baza identificatorului unic.</li>
     * </ul>
     */
    @Override
    public void deleteUserById(int id) throws Exception {
        int deleteStatus = mySQLManager.remove(User.class, id);
        if(deleteStatus == 1)
            throw new NullPointerException("User with id " + id +" not found!");
    }
}
