package com.rest_api.services;

import com.rest_api.interfaces.UserTypeDao;
import com.rest_api.model.UserType;
import com.rest_api.sql_handler.MySQLManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <ul>
 * 	<li>Serviciul care expune toate operatiile de tip CRUD specifice obiectului <strong>UserType</strong>.</li>
 * </ul>
 */
@Service
public class UserTypeDaoService implements UserTypeDao {
    /**
     * Obiectul de tip <strong>EntityManager</strong> care va asigura comunicarea cu serverul de baze de date.
     * Obiectul va expune toate metodele necesare persistarii, accesari si prelucrarii inregistrarilor din tabela <strong>InternalNode.</strong>
     */
    MySQLManager<UserType> mySQLManager = new MySQLManager<UserType>();

    /**
     * ============== CREATE ==============
     */
    /**
     * <ul>
     * 	<li>Crearea si persistarea unui nou tip de utilizator in baza de date.</li>
     * </ul>
     */
    @Override
    public void insertUserType(UserType userType) throws Exception{
        mySQLManager.insert(userType);
    }


    /**
     * ============== RETRIEVE ==============
     */
    /**
     * <ul>
     * 	<li>Extragerea tututor tipurilor de utilizator.</li>
     * </ul>
     */
    @Override
    public List<UserType> getAllUserTypes() throws NullPointerException{
        List<UserType> userTypes = mySQLManager.findlAll(UserType.class);
        userTypes = userTypes.stream().filter(userType -> !userType.getUser_type().contains("ADMIN")).collect(Collectors.toList());
        if(userTypes.size() == 0){
            throw new NullPointerException("No usertype found!");
        }
        return userTypes;
    }

    /**
     * <ul>
     * 	<li>Extragerea datelor despre un anumit tip de utilizator,
     *      pe baza sirului de caractere ce identifica in mod unic tipul</li>
     * </ul>
     */
    @Override
    public UserType getUserTypeData(String usertype) throws NullPointerException{
        UserType userType = (UserType)mySQLManager.findByPrimaryKey(UserType.class, usertype);
        if(userType == null)
            throw new NullPointerException(String.format("Usertype %s not found!", usertype));
        return userType;
    }

    /**
     * <ul>
     * 	<li>Extragerea factorului de replicare a unui anumit tip de utilizator,
     *      pe baza sirului de caractere ce identifica in mod unic tipul</li>
     * </ul>
     */
    @Override
    public int getReplicationFactor(String usertype) throws NullPointerException{
        UserType userType = (UserType)mySQLManager.findByPrimaryKey(UserType.class, usertype);
        return userType.getReplication_factor();
    }

    /**
     * <ul>
     * 	<li>Extragerea cantitatii de stocare disponibile unui anumit tip de utilizator,
     *      pe baza sirului de caractere ce identifica in mod unic tipul</li>
     * </ul>
     */
    @Override
    public long getAvailableStorage(String usertype) throws NullPointerException{
        UserType userType = (UserType)mySQLManager.findByPrimaryKey(UserType.class, usertype);
        return userType.getAvailable_storage();
    }


    /**
     * ============== UPDATE ==============
     */
    /**
     * <ul>
     * 	<li>Actualizarea factorului de replicare,
     *      pe baza sirului de caractere ce identifica in mod unic tipul</li>
     * </ul>
     */
    @Override
    public void updateReplicationFactor(String userype, int new_replicationFactor) throws NullPointerException{
        UserType candidate = getUserTypeData(userype);
        candidate.setReplication_factor(new_replicationFactor);
        mySQLManager.update(candidate);
    }

    /**
     * <ul>
     * 	<li>Actualizarea cantitatii de stocare disponibile,
     *      pe baza sirului de caractere ce identifica in mod unic tipul</li>
     * </ul>
     */
    @Override
    public void updateAvailableStorage(String userype, long availableStorage) {
        UserType candidate = getUserTypeData(userype);
        candidate.setAvailable_storage(availableStorage);
        mySQLManager.update(candidate);
    }


    /**
     * ============== DELETE ==============
     */
    /**
     * <ul>
     * 	<li>Eliminarea unui anumit tip de utilizator,
     *      pe baza sirului de caractere ce identifica in mod unic tipul</li>
     * </ul>
     */
    @Override
    public void deleteUserType(String usertype) throws NullPointerException {
        int deleteStatus = mySQLManager.remove(UserType.class, usertype);
        if(deleteStatus == 1)
            throw new NullPointerException("Usertype not found!");
    }

}
