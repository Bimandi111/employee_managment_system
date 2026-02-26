package com.EMS.service;

import com.EMS.dao.DepartmentDAO;
import com.EMS.dao.PositionDAO;
import com.EMS.entity.Department;
import com.EMS.entity.Position;

import java.util.List;

public class LookupService {

    private final DepartmentDAO deptDAO = new DepartmentDAO();
    private final PositionDAO posDAO = new PositionDAO();

    public List<Department> getAllDepartments() {
        return deptDAO.findAll();
    }

    public List<Position> getAllPositions() {
        return posDAO.findAll();
    }
}