package com.EMS.service;

import com.EMS.dao.DepartmentDAO;
import com.EMS.dao.EmployeeDAO;
import com.EMS.dao.PastEmployeeDAO;
import com.EMS.dao.PositionDAO;
import com.EMS.entity.Department;
import com.EMS.entity.Employee;
import com.EMS.entity.PastEmployee;
import com.EMS.entity.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final PastEmployeeDAO pastEmpDAO = new PastEmployeeDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final PositionDAO positionDAO = new PositionDAO();

    public Employee createEmployee(Employee employee) {
        if (employeeDAO.emailExists(employee.getEmail(), null))
            throw new IllegalArgumentException("An employee with email '" + employee.getEmail() + "' already exists.");
        Department dept = departmentDAO.findById(employee.getDepartment().getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found."));
        Position pos = positionDAO.findById(employee.getPosition().getPositionId())
                .orElseThrow(() -> new IllegalArgumentException("Position not found."));
        employee.setDepartment(dept);
        employee.setPosition(pos);
        return employeeDAO.save(employee);
    }

    public List<Employee> getAllEmployees() {
        return employeeDAO.findAll();
    }

    public Employee getEmployeeById(int id) {
        return employeeDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee with id=" + id + " not found."));
    }

    public List<Employee> searchEmployees(String name, String department, String position, LocalDate hireDate) {
        return employeeDAO.search(name, department, position, hireDate);
    }

    public Employee updateEmployee(int id, Employee updatedData) {
        Employee existing = getEmployeeById(id);
        if (!existing.getEmail().equalsIgnoreCase(updatedData.getEmail())
                && employeeDAO.emailExists(updatedData.getEmail(), id))
            throw new IllegalArgumentException("Email '" + updatedData.getEmail() + "' is already used.");
        Department dept = departmentDAO.findById(updatedData.getDepartment().getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found."));
        Position pos = positionDAO.findById(updatedData.getPosition().getPositionId())
                .orElseThrow(() -> new IllegalArgumentException("Position not found."));
        existing.setFirstName(updatedData.getFirstName());
        existing.setLastName(updatedData.getLastName());
        existing.setEmail(updatedData.getEmail());
        existing.setPhone(updatedData.getPhone());
        existing.setDepartment(dept);
        existing.setPosition(pos);
        existing.setHireDate(updatedData.getHireDate());
        existing.setSalary(updatedData.getSalary());
        return employeeDAO.update(existing);
    }

    public PastEmployee archiveEmployee(int id, String reason) {
        Employee employee = getEmployeeById(id);
        PastEmployee archive = PastEmployee.from(employee,
                (reason != null && !reason.isBlank()) ? reason : "Removed by administrator");
        PastEmployee saved = pastEmpDAO.save(archive);
        logger.info("Employee {} archived (archive id={})", employee.getFullName(), saved.getPastEmployeeId());
        employeeDAO.deleteById(id);
        return saved;
    }

    public List<PastEmployee> getAllPastEmployees() {
        return pastEmpDAO.findAll();
    }

    public PastEmployee getPastEmployeeById(int id) {
        return pastEmpDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Past employee with id=" + id + " not found."));
    }
}