package com.pixels.steloklab.db;

import jakarta.persistence.*;
import java.util.List;

public class PlayerDao {

    private final EntityManagerFactory emf;

    public PlayerDao() {
        emf = Persistence.createEntityManagerFactory("stelokPU");
    }

    public PlayerEntity findByUsername(String username) {
        EntityManager em = emf.createEntityManager();
        try {
            List<PlayerEntity> result = em.createQuery(
                    "SELECT p FROM PlayerEntity p WHERE p.username = :u", PlayerEntity.class)
                    .setParameter("u", username)
                    .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }

    public List<PlayerEntity> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM PlayerEntity p ORDER BY p.wins DESC", PlayerEntity.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void save(PlayerEntity entity) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Ошибка сохранения игрока: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void incrementWins(String username) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            List<PlayerEntity> result = em.createQuery(
                    "SELECT p FROM PlayerEntity p WHERE p.username = :u", PlayerEntity.class)
                    .setParameter("u", username)
                    .getResultList();
            if (result.isEmpty()) {
                PlayerEntity p = new PlayerEntity(username);
                p.setWins(1);
                em.persist(p);
            } else {
                PlayerEntity p = result.get(0);
                p.setWins(p.getWins() + 1);
                em.merge(p);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Ошибка обновления побед: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void close() {
        if (emf != null && emf.isOpen()) emf.close();
    }
}
