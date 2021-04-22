package com.dropbox.sql_handler;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.RollbackException;
import java.util.List;
import java.util.Map;

public class MySQLManager<T> {
    EntityManager entityManager;

    public MySQLManager(){
        this.entityManager = EntityManagerSingleton.getEntityManager();
    }

    public int insert(T object){
        int status_code = 0;
        try{
            entityManager.getTransaction().begin();
            entityManager.persist(object);
            entityManager.getTransaction().commit();
        }
        catch (Exception exception){
            entityManager.getTransaction().rollback();
            status_code = 1;
        }
        return status_code;
    }

    public T findByPrimaryKey(Class<T> tableType, Object primaryKey){
        return entityManager.find(tableType, primaryKey);
    }

    public int remove(Class<T> tableType, Object criteria){
        try {
            T candidate = findByPrimaryKey(tableType, criteria);
            entityManager.getTransaction().begin();
            entityManager.remove(candidate);
            entityManager.getTransaction().commit();
            return 0;
        }
        catch (Exception exception){
            entityManager.getTransaction().rollback();
            return  1;
        }
    }

    public List<T> findlAll(Class<T> tableType){
        return entityManager.createQuery(String.format("Select u FROM %s u", tableType.getSimpleName())).getResultList();
    }

    public int update(T object){
        int status_code = 0;
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(object);
            entityManager.getTransaction().commit();
        }
        catch(RollbackException exception){
            status_code = 1;
            entityManager.getTransaction().rollback();
        }
        catch(EntityExistsException exception){
            status_code = 2;
            entityManager.getTransaction().rollback();
            entityManager.getTransaction().begin();
            entityManager.merge(object);
            entityManager.getTransaction().commit();
        }
        return status_code;
    }

    public List<Object> findByCriteria(Class<T> entityClass, Map<String, Object> conditions) {
        StringBuilder sqlQueryString = new StringBuilder();
        sqlQueryString.append(String.format("Select b FROM %s b WHERE ", entityClass.getSimpleName()));
        int index = 0;
        for(Map.Entry<String, Object> condition : conditions.entrySet()){
            sqlQueryString.append("b." + condition.getKey() + "=:" + condition.getKey());
            if (index < conditions.size() - 1 ){
                sqlQueryString.append(" and ");
            }
            index = index + 1;
        }
        Query query = entityManager.createQuery(sqlQueryString.toString(), entityClass);
        for(Map.Entry<String, Object> condition : conditions.entrySet()){
            query.setParameter(condition.getKey(), condition.getValue());
        }
        return query.getResultList();
    }
}
