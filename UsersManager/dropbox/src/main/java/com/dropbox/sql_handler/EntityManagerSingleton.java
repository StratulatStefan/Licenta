package com.dropbox.sql_handler;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

public class EntityManagerSingleton {
    private static EntityManager entityManager = null;

    public static EntityManager getEntityManager(){
        if(entityManager == null){
            System.out.println("Initializing the entity manager for SQL");
            entityManager = Persistence.createEntityManagerFactory("com.dropbox.sql").createEntityManager();
        }
        else{
            System.out.println("Using the existent entity manager for SQL");
        }
        return entityManager;
    }
}
