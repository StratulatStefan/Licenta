package com.rest_api.sql_handler;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

public class EntityManagerHandler {
    private static EntityManager entityManager = null;

    public static EntityManager getEntityManager(){
        System.out.println("Initializing the entity manager for SQL");
        return Persistence.createEntityManagerFactory("com.rest_api.sql").createEntityManager();
    }
}
