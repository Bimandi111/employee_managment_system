package com.EMS.dao;

import com.EMS.entity.Department;
import com.EMS.util.HibernateUtil;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DepartmentDAO {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentDAO.class);

    public List<Department> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.doReturningWork(connection -> {
                List<Department> list = new ArrayList<>();
                String sql = "SELECT department_id, department_name, description FROM departments ORDER BY department_name";
                try (PreparedStatement ps = connection.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Department d = new Department();
                        d.setDepartmentId(rs.getInt("department_id"));
                        d.setDepartmentName(rs.getString("department_name"));
                        d.setDescription(rs.getString("description"));
                        list.add(d);
                    }
                }
                return list;
            });
        } catch (Exception e) {
            logger.error("Error fetching departments", e);
            throw new RuntimeException("Failed to fetch departments", e);
        }
    }

    public Optional<Department> findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.doReturningWork(connection -> {
                String sql = "SELECT department_id, department_name, description FROM departments WHERE department_id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            Department d = new Department();
                            d.setDepartmentId(rs.getInt("department_id"));
                            d.setDepartmentName(rs.getString("department_name"));
                            d.setDescription(rs.getString("description"));
                            return Optional.of(d);
                        }
                        return Optional.<Department>empty();
                    }
                }
            });
        } catch (Exception e) {
            System.out.println("Error fetching department by" +  id + e);
            throw new RuntimeException("Failed to fetch department", e);
        }
    }
}