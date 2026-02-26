package com.EMS.dao;

import com.EMS.entity.Department;
import com.EMS.entity.Employee;
import com.EMS.entity.Position;
import com.EMS.util.HibernateUtil;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeDAO {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeDAO.class);

    private Employee mapRow(ResultSet resultSet) throws Exception {
        Employee employee = new Employee();
        employee.setEmployeeId(resultSet.getInt("employee_id"));
        employee.setFirstName(resultSet.getString("first_name"));
        employee.setLastName(resultSet.getString("last_name"));
        employee.setEmail(resultSet.getString("email"));
        employee.setPhone(resultSet.getString("phone"));
        employee.setHireDate(resultSet.getDate("hire_date").toLocalDate());
        employee.setSalary(resultSet.getBigDecimal("salary"));
        employee.setStatus(Employee.EmployeeStatus.valueOf(resultSet.getString("status")));

        Department d = new Department();
        d.setDepartmentId(resultSet.getInt("department_id"));
        d.setDepartmentName(resultSet.getString("department_name"));
        employee.setDepartment(d);

        Position position = new Position();
        position.setPositionId(resultSet.getInt("position_id"));
        position.setTitle(resultSet.getString("title"));
        position.setPayGrade(resultSet.getString("pay_grade"));
        employee.setPosition(position);

        return employee;
    }

    private static final String BASE_SELECT =
            "SELECT e.employee_id, e.first_name, e.last_name, e.email, e.phone, " +
                    "e.hire_date, e.salary, e.status, " +
                    "d.department_id, d.department_name, " +
                    "p.position_id, p.title, p.pay_grade " +
                    "FROM employees e " +
                    "JOIN departments d ON e.department_id = d.department_id " +
                    "JOIN positions p ON e.position_id = p.position_id ";

    public List<Employee> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.doReturningWork(connection -> {
                List<Employee> list = new ArrayList<>();
                String sql = BASE_SELECT + "WHERE e.status = 'ACTIVE' ORDER BY e.last_name, e.first_name";
                try (PreparedStatement ps = connection.prepareStatement(sql);
                     ResultSet resultSet = ps.executeQuery()) {
                    while (resultSet.next()) {
                        try {
                            list.add(mapRow(resultSet));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                return list;
            });
        } catch (Exception e) {
            logger.error("Error fetching employees", e);
            throw new RuntimeException("Failed to fetch employees", e);
        }
    }

    public Optional<Employee> findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.doReturningWork(connection -> {
                String sql = BASE_SELECT + "WHERE e.employee_id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) return Optional.of(mapRow(rs));
                        return Optional.<Employee>empty();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (Exception e) {
            logger.error("Error fetching employee by id: {}", id, e);
            throw new RuntimeException("Failed to fetch employee", e);
        }
    }

    public List<Employee> search(String name, String department, String position, LocalDate hireDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.doReturningWork(connection -> {
                StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE e.status = 'ACTIVE' ");
                List<Object> params = new ArrayList<>();

                if (name != null && !name.isBlank()) {
                    sql.append("AND (LOWER(e.first_name) LIKE ? OR LOWER(e.last_name) LIKE ?) ");
                    params.add("%" + name.toLowerCase() + "%");
                    params.add("%" + name.toLowerCase() + "%");
                }
                if (department != null && !department.isBlank()) {
                    sql.append("AND LOWER(d.department_name) LIKE ? ");
                    params.add("%" + department.toLowerCase() + "%");
                }
                if (position != null && !position.isBlank()) {
                    sql.append("AND LOWER(p.title) LIKE ? ");
                    params.add("%" + position.toLowerCase() + "%");
                }
                if (hireDate != null) {
                    sql.append("AND e.hire_date = ? ");
                    params.add(Date.valueOf(hireDate));
                }
                sql.append("ORDER BY e.last_name, e.first_name");

                try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
                    for (int i = 0; i < params.size(); i++) {
                        ps.setObject(i + 1, params.get(i));
                    }
                    List<Employee> list = new ArrayList<>();
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) list.add(mapRow(rs));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return list;
                }
            });
        } catch (Exception e) {
            logger.error("Error searching employees", e);
            throw new RuntimeException("Failed to search employees", e);
        }
    }

    public Employee save(Employee employee) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.doReturningWork(connection -> {
                String sql = "INSERT INTO employees (first_name, last_name, email, phone, department_id, position_id, hire_date, salary, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";
                try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, employee.getFirstName());
                    ps.setString(2, employee.getLastName());
                    ps.setString(3, employee.getEmail());
                    ps.setString(4, employee.getPhone());
                    ps.setInt(5, employee.getDepartment().getDepartmentId());
                    ps.setInt(6, employee.getPosition().getPositionId());
                    ps.setDate(7, Date.valueOf(employee.getHireDate()));
                    ps.setBigDecimal(8, employee.getSalary());
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) employee.setEmployeeId(keys.getInt(1));
                    }
                }
                return employee;
            });
        } catch (Exception e) {
            logger.error("Error saving employee", e);
            throw new RuntimeException("Failed to save employee", e);
        }
    }

    public Employee update(Employee employee) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.doReturningWork(connection -> {
                String sql = "UPDATE employees SET first_name=?, last_name=?, email=?, phone=?, " +
                        "department_id=?, position_id=?, hire_date=?, salary=? " +
                        "WHERE employee_id=?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, employee.getFirstName());
                    ps.setString(2, employee.getLastName());
                    ps.setString(3, employee.getEmail());
                    ps.setString(4, employee.getPhone());
                    ps.setInt(5, employee.getDepartment().getDepartmentId());
                    ps.setInt(6, employee.getPosition().getPositionId());
                    ps.setDate(7, Date.valueOf(employee.getHireDate()));
                    ps.setBigDecimal(8, employee.getSalary());
                    ps.setInt(9, employee.getEmployeeId());
                    ps.executeUpdate();
                }
                return employee;
            });
        } catch (Exception e) {
            logger.error("Error updating employee", e);
            throw new RuntimeException("Failed to update employee", e);
        }
    }

    public void deleteById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.doWork(connection -> {
                String sql = "DELETE FROM employees WHERE employee_id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
            });
        } catch (Exception e) {
            logger.error("Error deleting employee id: {}", id, e);
            throw new RuntimeException("Failed to delete employee", e);
        }
    }

    public boolean emailExists(String email, Integer excludeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.doReturningWork(connection -> {
                String sql = excludeId != null
                        ? "SELECT COUNT(*) FROM employees WHERE LOWER(email) = LOWER(?) AND employee_id <> ?"
                        : "SELECT COUNT(*) FROM employees WHERE LOWER(email) = LOWER(?)";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, email);
                    if (excludeId != null) ps.setInt(2, excludeId);
                    try (ResultSet rs = ps.executeQuery()) {
                        return rs.next() && rs.getInt(1) > 0;
                    }
                }
            });
        } catch (Exception e) {
            logger.error("Error checking email existence", e);
            throw new RuntimeException("Failed to check email", e);
        }
    }
}