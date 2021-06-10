package com.rest_api.sql_handler;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.RollbackException;
import java.util.*;

public class MySQLManager<T> {
    final EntityManager entityManager;

    public MySQLManager(){
        this.entityManager = EntityManagerHandler.getEntityManager();
    }

    public void insert(T object) throws Exception{
        synchronized (this.entityManager) {
            try {
                entityManager.getTransaction().begin();
                entityManager.persist(object);
                entityManager.getTransaction().commit();
            } catch (Exception exception) {
                entityManager.getTransaction().rollback();
                throw new Exception(exception.getLocalizedMessage());
            }
        }
    }

    public T findByPrimaryKey(Class<T> tableType, Object primaryKey){
        synchronized (this.entityManager) {
            return entityManager.find(tableType, primaryKey);
        }
    }

    public int remove(Class<T> tableType, Object criteria){
        synchronized (this.entityManager) {
            try {
                T candidate = findByPrimaryKey(tableType, criteria);
                entityManager.getTransaction().begin();
                entityManager.remove(candidate);
                entityManager.getTransaction().commit();
                return 0;
            } catch (Exception exception) {
                entityManager.getTransaction().rollback();
                return 1;
            }
        }
    }

    public void removeAll(Class<T> tableType){
        synchronized (this.entityManager) {
            entityManager.getTransaction().begin();
            Query deleteQuery = entityManager.createQuery("DELETE from " + tableType.getSimpleName());
            deleteQuery.executeUpdate();
            entityManager.getTransaction().commit();
        }
    }

    public List<T> findlAll(Class<T> tableType){
        synchronized (this.entityManager) {
            try {
                return entityManager.createQuery(String.format("Select u FROM %s u", tableType.getSimpleName())).getResultList();
            } catch (Exception sqlException) {
                System.out.println("SQL Exception : " + sqlException.getMessage());
                return new ArrayList<T>();
            }
        }
    }

    public int update(T object){
        synchronized (this.entityManager) {
            int status_code = 0;
            try {
                entityManager.getTransaction().begin();
                entityManager.persist(object);
                entityManager.getTransaction().commit();
            } catch (RollbackException exception) {
                status_code = 1;
                entityManager.getTransaction().rollback();
            } catch (EntityExistsException exception) {
                status_code = 2;
                entityManager.getTransaction().rollback();
                entityManager.getTransaction().begin();
                entityManager.merge(object);
                entityManager.getTransaction().commit();
            }
            return status_code;
        }
    }

    public List<Object> findByCriteria(Class<T> entityClass, Map<String, Object> conditions){
        synchronized (this.entityManager) {
            StringBuilder sqlQueryString = new StringBuilder();
            sqlQueryString.append(String.format("Select b FROM %s b WHERE", entityClass.getSimpleName()));
            for (Map.Entry<String, Object> condition : conditions.entrySet()) {
                try {
                    entityClass.getDeclaredField(condition.getKey());
                } catch (NoSuchFieldException exception) {
                    continue;
                }
                sqlQueryString.append(" b." + condition.getKey() + "=:" + condition.getKey()).append(" and");
            }

            String lastWord = sqlQueryString.substring(sqlQueryString.lastIndexOf(" ") + 1);
            String queryStr;
            if (lastWord.equals("and"))
                queryStr = sqlQueryString.substring(0, sqlQueryString.lastIndexOf(" "));
            else
                queryStr = sqlQueryString.toString();

            Query query = entityManager.createQuery(queryStr, entityClass);
            for (Map.Entry<String, Object> condition : conditions.entrySet()) {
                try {
                    entityClass.getDeclaredField(condition.getKey());
                } catch (NoSuchFieldException exception) {
                    continue;
                }
                query.setParameter(condition.getKey(), condition.getValue());
            }
            return query.getResultList();
        }
    }

}
