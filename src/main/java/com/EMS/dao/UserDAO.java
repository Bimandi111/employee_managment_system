package com.EMS.dao;

import com.EMS.entity.User;
import com.EMS.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    public Optional<User> findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            return session.doReturningWork(connection -> {
                String sql = "SELECT user_id, username, password_hash, role, is_active " +
                        "FROM users WHERE LOWER(username) = LOWER(?) AND is_active = 1";

                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, username);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            User user = new User();
                            user.setUserId(resultSet.getInt("user_id"));
                            user.setUsername(resultSet.getString("username"));
                            user.setPasswordHash(resultSet.getString("password_hash"));
                            user.setRole(User.Role.valueOf(resultSet.getString("role")));
                            user.setActive(resultSet.getBoolean("is_active"));
                            return Optional.of(user);
                        }
                        return Optional.<User>empty();
                    }
                }
            });

        } catch (Exception e) {
            logger.error("Error finding user by username: {}", username, e);
            throw new RuntimeException("Database error finding user", e);
        }
    }

    public User save(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
            return user;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Failed to save user: " + e.getMessage(), e);
        }
    }
}