package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {
    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        Properties hibernateProps = new Properties();
        hibernateProps.put(Environment.URL, "jdbc:p6spy:mysql://localhost:32772/rpg");
        hibernateProps.put(Environment.USER, "root");
        hibernateProps.put(Environment.PASS, "root");

        hibernateProps.put(Environment.HBM2DDL_AUTO, "update");
        hibernateProps.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        hibernateProps.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");

        sessionFactory = new Configuration()
                .setProperties(hibernateProps)
                .addAnnotatedClass(Player.class)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        try (Session session = sessionFactory.openSession()) {
            String sql = "SELECT * FROM player LIMIT :pLimit OFFSET :pOffset";
            NativeQuery<Player> query = session.createNativeQuery(sql, Player.class);
            query.setParameter("pOffset", pageNumber * pageSize);
            query.setParameter("pLimit", pageSize);

            return query.list();
        }
    }

    @Override
    public int getAllCount() {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createNamedQuery("Player_getAllCount", Long.class).getSingleResult();
            return count.intValue();
        }
    }

    @Override
    public Player save(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(player);
            transaction.commit();

            return player;
        }
    }

    @Override
    public Player update(Player player) {
        Player updated;

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            updated = (Player) session.merge(player);
            transaction.commit();
        }

        return updated;
    }

    @Override
    public Optional<Player> findById(long id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.of(session.find(Player.class, id));
        }
    }

    @Override
    public void delete(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.remove(player);
            transaction.commit();
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}