package com.EMS.dao;

import com.EMS.entity.Position;
import com.EMS.util.HibernateUtil;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PositionDAO {

    private static final Logger logger = LoggerFactory.getLogger(PositionDAO.class);

    public List<Position> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.doReturningWork(connection -> {
                List<Position> list = new ArrayList<>();
                String sql = "SELECT position_id, title, pay_grade FROM positions ORDER BY title";
                try (PreparedStatement ps = connection.prepareStatement(sql);
                     ResultSet resultSet = ps.executeQuery()) {
                    while (resultSet.next()) {
                        Position p = new Position();
                        p.setPositionId(resultSet.getInt("position_id"));
                        p.setTitle(resultSet.getString("title"));
                        p.setPayGrade(resultSet.getString("pay_grade"));
                        list.add(p);
                    }
                }
                return list;
            });
        } catch (Exception e) {
            logger.error("Error fetching positions", e);
            throw new RuntimeException("Failed to fetch positions", e);
        }
    }

    public Optional<Position> findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.doReturningWork(connection -> {
                String sql = "SELECT position_id, title, pay_grade FROM positions WHERE position_id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    try (ResultSet resultSet = ps.executeQuery()) {
                        if (resultSet.next()) {
                            Position p = new Position();
                            p.setPositionId(resultSet.getInt("position_id"));
                            p.setTitle(resultSet.getString("title"));
                            p.setPayGrade(resultSet.getString("pay_grade"));
                            return Optional.of(p);
                        }
                        return Optional.<Position>empty();
                    }
                }
            });
        } catch (Exception e) {
            logger.error("Error fetching position by id: {}", id, e);
            throw new RuntimeException("Failed to fetch position", e);
        }
    }
}