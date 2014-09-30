package com.fererlab.db;

import com.fererlab.dto.Model;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * acm
 */
public class EM {

    private static EntityManager entityManager = null;
    private static EntityManagerFactory entityManagerFactory;
    private static String persistenceUN = null;

    public static void start(String persistenceUnitName) {
        persistenceUN = persistenceUnitName;
        if (entityManager == null) {
            createEntityManager();
        }
    }

    private static void createEntityManager() {
        entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUN);
        entityManager = entityManagerFactory.createEntityManager();
    }

    public static void stop() {
        if (entityManager != null) {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
            if (entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
            }
        }
    }

    public static EntityManager getEntityManager() {
        if(entityManager == null){
            createEntityManager();
        }
        return entityManager;
    }

    public static <T extends Model> T find(Class<T> type, Object id) {
        return getEntityManager().find(type, id);
    }

    public static <T extends Model> void persist(T t) {
        if (!getEntityManager().getTransaction().isActive()) {
            getEntityManager().getTransaction().begin();
            getEntityManager().persist(t);
            getEntityManager().getTransaction().commit();
        } else {
            getEntityManager().persist(t);
        }
    }

    public static <T extends Model> void remove(T t) {
        if (!getEntityManager().getTransaction().isActive()) {
            getEntityManager().getTransaction().begin();
            getEntityManager().remove(t);
            getEntityManager().getTransaction().commit();
        } else {
            getEntityManager().remove(t);
        }
    }

    public static <T extends Model> T merge(T t) {
        if (!getEntityManager().getTransaction().isActive()) {
            getEntityManager().getTransaction().begin();
            t = getEntityManager().merge(t);
            getEntityManager().getTransaction().commit();
            return t;
        } else {
            return getEntityManager().merge(t);
        }
    }
}
