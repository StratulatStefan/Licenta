package com.rest_api.sql_handler;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

/**
 * <ul>
 * 	<li>Clasa ce faciliteaza crearea de obiecte de tip <strong>EntityManager</strong>, care vor gestiona comunicarea dintre aplicatie si serverul de baze de date.</li>
 * </ul>
 */
public class EntityManagerHandler {
    /**
     * <ul>
     * 	<li>Crearea obiectului <strong>EntityManager</strong> se face cu ajutorul fabricii de entitati gestionate de modulul de persistenta <strong>Persistence</strong>.</li>
     * </ul>
     */
    public static EntityManager getEntityManager(){
        System.out.println("Initializing the entity manager for SQL");
        return Persistence.createEntityManagerFactory("com.rest_api.sql").createEntityManager();
    }
}
