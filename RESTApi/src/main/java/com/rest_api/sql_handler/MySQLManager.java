package com.rest_api.sql_handler;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.RollbackException;
import java.util.*;

/**
 * <ul>
 * 	<li>Clasa de tip <strong>wrapper</strong> pentru <strong>entityManager</strong>, care va gestiona conexiunea cu serverul de baze de date.</li>
 * 	<li> Se vor expune toate operatiile necesare persistarii, accesari si prelucrarii de date.</li>
 * 	<li>Clasa este parametrizabila intrucat, pentru fiecare tip de entitate, se va crea cate un entityManager.</li>
 * 	<li>Fiecare entitate este reprezentata la nivel obiectual prin intermediul clasei.</li>
 * </ul>
 */
public class MySQLManager<T> {
    /**
     * <ul>
     * 	<li>Obiectul de tip <strong>EntityManager</strong> care va gestiona conexiunea cu serverul de baze de date.</li>
     * 	<li>Obiectul va facilita efectuarea tuturor operatiilor cu baza de date.</li>
     * 	<li>Obiectul va fi <strong>final</strong> pentru a se putea asigura sincronizarea explicita.</li>
     * </ul>
     */
    final EntityManager entityManager;

    /**
     * <ul>
     * 	<li>Constructorul clasei, care va instantia obiectul de tip <strong>EntityManager</strong> cu ajutorul fabricii.</li>
     * </ul>
     */
    public MySQLManager(){
        this.entityManager = EntityManagerHandler.getEntityManager();
    }

    /**
     * <ul>
     * 	<li>Functie pentru persistarea unei inregistrari in tabela determinata de <strong>T</strong></li>
     * 	<li> Persistarea se va efectua in cadrul unei tranzactii.</li>
     * 	<li> La finalul tranzactiei se va face <strong>commit</strong>.</li>
     * 	<li> In cazul unei exceptii, se va face <strong>rollback</strong> pentru a readuce baza de date la starea anterioara tranzactiei.</li>
     * </ul>
     */
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

    /**
     * <ul>
     * 	<li>Functie pentru cautarea unei inregistrari in tabela determinata de <strong>T</strong>.</li>
     * 	<li> Se foloseste functia <strong>find</strong> a obiectului <strong>EntityManager</strong>.</li>
     * </ul>
     */
    public T findByPrimaryKey(Class<T> tableType, Object primaryKey){
        synchronized (this.entityManager) {
            return entityManager.find(tableType, primaryKey);
        }
    }

    /**
     * <ul>
     * 	<li>Functie pentru eliminarea unei inregistrari din tabela determinata de T.</li>
     * 	<li> Obiectul ce va fi eliminat va fi identificat pe baza cheii primare.</li>
     * 	<li> Inainte de eliminare se va cauta inregistrarea folosind functia <strong>findByPrimaryKey</strong>.</li>
     * 	<li> Odata gasita inregistrarea, se va elimina in cadrul unei tranzactii.</li>
     * </ul>
     */
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

    /**
     * <ul>
     * 	<li>Functie pentru eliminarea tuturor inregistrarilor dintr-o tabela, identificata prin <strong>Class<T></strong>.</li>
     * 	<li> Se va folosi un <strong>query</strong> specific si metoda <strong>executeUpdate</strong>.</li>
     * 	<li> Stergerea se va face in cadrul unei tranzactii, finalizata cu <strong>commit</strong> sau <strong>rollback</strong>.</li>
     * </ul>
     */
    public void removeAll(Class<T> tableType){
        synchronized (this.entityManager) {
            entityManager.getTransaction().begin();
            Query deleteQuery = entityManager.createQuery("DELETE from " + tableType.getSimpleName());
            deleteQuery.executeUpdate();
            entityManager.getTransaction().commit();
        }
    }

    /**
     * <ul>
     * 	<li>Functie pentru extragerea tututor inregistrarilor dintr-o tabela, determinata de <strong>Class<T></strong>.</li>
     * 	<li> Se va folosi un <strong>query</strong> specific si metoda <strong>getResultList</strong>.</li>
     * </ul>
     */
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

    /**
     * <ul>
     * 	<li>Functie pentru modificarea unei inregistrari din tabela determinata de <strong>Class<T></strong>.</li>
     * 	<li> In cazul in care inregistrarea nu exista, se va persista o inregistrare noua <strong>persist</strong>.</li>
     * 	<li> Daca exista, se va modifica cea existenta <strong>merge</strong>.</li>
     * 	<li> Operatiile se vor efectua in cadrul unei tranzactii, finalizata cu <strong>commit</strong> sau <strong>rollback</strong>.</li>
     * 	<li> Se va returna un status care sa indice tipul de operatie efectuata, persistare sau modificare.</li>
     * </ul>
     */
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

    /**
     * <ul>
     * 	<li>Functie pentru cautarea unei anumite inregistrari in tabela determinata de <strong>Class<T></strong>, pe baza unor criterii dispuse sub forma unui dictionar.</li>
     * 	<li> Se va crea un <strong>query</strong> specific de cautare.</li>
     * </ul>
     */
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
