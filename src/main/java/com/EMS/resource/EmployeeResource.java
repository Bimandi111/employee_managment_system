package com.EMS.resource;

import com.EMS.auth.Secured;
import com.EMS.entity.Department;
import com.EMS.entity.Employee;
import com.EMS.entity.PastEmployee;
import com.EMS.entity.Position;
import com.EMS.service.EmployeeService;
import com.EMS.util.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured
public class EmployeeResource {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeResource.class);
    private final EmployeeService service = new EmployeeService();

    private Employee buildEmployeeFromMap(Map<String, Object> body) {
        Employee employee = new Employee();

        if (body.get("firstName") != null)
            employee.setFirstName(body.get("firstName").toString());
        if (body.get("lastName") != null)
            employee.setLastName(body.get("lastName").toString());
        if (body.get("email") != null)
            employee.setEmail(body.get("email").toString());
        if (body.get("phone") != null)
            employee.setPhone(body.get("phone").toString());
        if (body.get("hireDate") != null)
            employee.setHireDate(LocalDate.parse(body.get("hireDate").toString()));
        if (body.get("salary") != null)
            employee.setSalary(new BigDecimal(body.get("salary").toString()));

        if (body.get("department") != null) {
            Department dept = new Department();
            @SuppressWarnings("unchecked")
            Map<String, Object> deptMap = (Map<String, Object>) body.get("department");
            dept.setDepartmentId(Integer.parseInt(deptMap.get("departmentId").toString()));
            employee.setDepartment(dept);
        }

        if (body.get("position") != null) {
            Position pos = new Position();
            @SuppressWarnings("unchecked")
            Map<String, Object> posMap = (Map<String, Object>) body.get("position");
            pos.setPositionId(Integer.parseInt(posMap.get("positionId").toString()));
            employee.setPosition(pos);
        }

        return employee;
    }

    @GET
    public Response getAllEmployees() {
        try {
            return Response.ok(ApiResponse.success(service.getAllEmployees())).build();
        } catch (Exception e) {
            logger.error("Error fetching employees", e);
            return Response.serverError()
                    .entity(ApiResponse.error("Failed to retrieve employees.")).build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getEmployeeById(@PathParam("id") int id) {
        try {
            return Response.ok(ApiResponse.success(service.getEmployeeById(id))).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    @GET
    @Path("/search")
    public Response searchEmployees(
            @QueryParam("name") String name,
            @QueryParam("department") String department,
            @QueryParam("position") String position,
            @QueryParam("hireDate") String hireDateStr) {
        try {
            LocalDate hireDate = (hireDateStr != null && !hireDateStr.isBlank())
                    ? LocalDate.parse(hireDateStr) : null;
            return Response.ok(ApiResponse.success(
                    service.searchEmployees(name, department, position, hireDate))).build();
        } catch (Exception exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Search failed: " + exception.getMessage())).build();
        }
    }

    @POST
    @RolesAllowed({"ADMIN", "HR"})
    public Response createEmployee(Map<String, Object> body) {
        try {
            Employee employee = buildEmployeeFromMap(body);
            Employee created = service.createEmployee(employee);
            return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("Employee created successfully.", created)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(ApiResponse.error(e.getMessage())).build();
        } catch (Exception e) {
            logger.error("Error creating employee", e);
            return Response.serverError()
                    .entity(ApiResponse.error("Failed to create employee: " + e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "HR"})
    public Response updateEmployee(@PathParam("id") int id, Map<String, Object> body) {
        try {
            Employee employee = buildEmployeeFromMap(body);
            return Response.ok(ApiResponse.success("Employee updated successfully.",
                    service.updateEmployee(id, employee))).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error(e.getMessage())).build();
        } catch (Exception e) {
            logger.error("Error updating employee", e);
            return Response.serverError()
                    .entity(ApiResponse.error("Failed to update employee: " + e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response archiveEmployee(
            @PathParam("id") int id,
            @QueryParam("reason") @DefaultValue("") String reason) {
        try {
            PastEmployee archived = service.archiveEmployee(id, reason);
            return Response.ok(ApiResponse.success(
                    "Employee archived to Past Employees.", archived)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error(e.getMessage())).build();
        } catch (Exception e) {
            logger.error("Error archiving employee", e);
            return Response.serverError()
                    .entity(ApiResponse.error("Failed to archive employee.")).build();
        }
    }

    @GET
    @Path("/past")
    public Response getAllPastEmployees() {
        try {
            return Response.ok(ApiResponse.success(service.getAllPastEmployees())).build();
        } catch (Exception e) {
            logger.error("Error fetching past employees", e);
            return Response.serverError()
                    .entity(ApiResponse.error("Failed to retrieve past employees.")).build();
        }
    }

    @GET
    @Path("/past/{id}")
    public Response getPastEmployeeById(@PathParam("id") int id) {
        try {
            return Response.ok(ApiResponse.success(service.getPastEmployeeById(id))).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error(e.getMessage())).build();
        }
    }
}

