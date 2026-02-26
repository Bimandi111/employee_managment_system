package com.EMS.dao;

import com.EMS.entity.Department;
import com.EMS.entity.PastEmployee;
import com.EMS.entity.Position;
import com.EMS.util.HibernateUtil;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PastEmployeeDAO {

    private static final Logger logger = LoggerFactory.getLogger(PastEmployeeDAO.class);

    private PastEmployee mapRow(ResultSet rs) throws Exception {
        PastEmployee pastEmployee = new PastEmployee();
        pastEmployee.setPastEmployeeId(rs.getInt("past_employee_id"));
        pastEmployee.setOriginalEmployeeId(rs.getInt("original_employee_id"));
        pastEmployee.setFirstName(rs.getString("first_name"));
        pastEmployee.setLastName(rs.getString("last_name"));
        pastEmployee.setEmail(rs.getString("email"));
        pastEmployee.setPhone(rs.getString("phone"));
        pastEmployee.setHireDate(rs.getDate("hire_date").toLocalDate());
        pastEmployee.setSalary(rs.getBigDecimal("salary"));
        pastEmployee.setTerminationDate(rs.getDate("termination_date").toLocalDate());
        pastEmployee.setTerminationReason(rs.getString("termination_reason"));

        Department department = new Department();
        department.setDepartmentId(rs.getInt("department_id"));
        department.setDepartmentName(rs.getString("department_name"));
        pastEmployee.setDepartment(department);

        Position position = new Position();
        position.setPositionId(rs.getInt("position_id"));
        position.setTitle(rs.getString("title"));
        pastEmployee.setPosition(position);

        return pastEmployee;
    }

    public PastEmployee save(PastEmployee pe) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.doReturningWork(connection -> {
                String sql = "INSERT INTO past_employees (original_employee_id, first_name, last_name, email, phone, " +
                        "department_id, position_id, hire_date, salary, termination_date, termination_reason) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, pe.getOriginalEmployeeId());
                    ps.setString(2, pe.getFirstName());
                    ps.setString(3, pe.getLastName());
                    ps.setString(4, pe.getEmail());
                    ps.setString(5, pe.getPhone());
                    ps.setInt(6, pe.getDepartment().getDepartmentId());
                    ps.setInt(7, pe.getPosition().getPositionId());
                    ps.setDate(8, Date.valueOf(pe.getHireDate()));
                    ps.setBigDecimal(9, pe.getSalary());
                    ps.setDate(10, Date.valueOf(pe.getTerminationDate() != null ? pe.getTerminationDate() : java.time.LocalDate.now()));
                    ps.setString(11, pe.getTerminationReason());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) pe.setPastEmployeeId(keys.getInt(1));
                    }
                }
                return pe;
            });
        } catch (Exception e) {
            logger.error("Error saving past employee", e);
            throw new RuntimeException("Failed to archive employee", e);
        }
    }

    public List<PastEmployee> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.doReturningWork(connection -> {
                List<PastEmployee> list = new ArrayList<>();
                String sql = "SELECT pe.*, d.department_name, p.title, p.pay_grade " +
                        "FROM past_employees pe " +
                        "JOIN departments d ON pe.department_id = d.department_id " +
                        "JOIN positions p ON pe.position_id = p.position_id " +
                        "ORDER BY pe.termination_date DESC";
                try (PreparedStatement ps = connection.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return list;
            });
        } catch (Exception e) {
            logger.error("Error fetching past employees", e);
            throw new RuntimeException("Failed to fetch past employees", e);
        }
    }

    public Optional<PastEmployee> findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.doReturningWork(connection -> {
                String sql = "SELECT pe.*, d.department_name, p.title, p.pay_grade " +
                        "FROM past_employees pe " +
                        "JOIN departments d ON pe.department_id = d.department_id " +
                        "JOIN positions p ON pe.position_id = p.position_id " +
                        "WHERE pe.past_employee_id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            try {
                                return Optional.of(mapRow(rs));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return Optional.<PastEmployee>empty();
                    }
                }
            });
        } catch (Exception e) {
            logger.error("Error fetching past employee by id: {}", id, e);
            throw new RuntimeException("Failed to fetch past employee", e);
        }
    }
}