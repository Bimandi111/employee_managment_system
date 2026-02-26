package com.EMS.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "past_employees")
public class PastEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "past_employee_id")
    private Integer pastEmployeeId;

    @Column(name = "original_employee_id", nullable = false)
    private Integer originalEmployeeId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal salary;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "termination_date", nullable = false)
    private LocalDate terminationDate;

    @Column(name = "termination_reason", length = 255)
    private String terminationReason;

    @Column(name = "archived_at", updatable = false)
    private LocalDateTime archivedAt;

    public static PastEmployee from(Employee emp, String reason) {
        PastEmployee pe = new PastEmployee();
        pe.originalEmployeeId = emp.getEmployeeId();
        pe.firstName = emp.getFirstName();
        pe.lastName = emp.getLastName();
        pe.email = emp.getEmail();
        pe.phone = emp.getPhone();
        pe.department = emp.getDepartment();
        pe.position = emp.getPosition();
        pe.hireDate = emp.getHireDate();
        pe.salary = emp.getSalary();
        pe.terminationDate = LocalDate.now();
        pe.terminationReason = reason;
        pe.archivedAt = LocalDateTime.now();
        return pe;
    }

    @PrePersist
    protected void onCreate() {
        if (this.archivedAt == null) this.archivedAt = LocalDateTime.now();
        if (this.terminationDate == null) this.terminationDate = LocalDate.now();
    }

    public Integer getPastEmployeeId() {
        return pastEmployeeId;
    }

    public Integer getOriginalEmployeeId() {
        return originalEmployeeId;
    }

    public void setOriginalEmployeeId(Integer id) {
        this.originalEmployeeId = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String fn) {
        this.firstName = fn;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String ln) {
        this.lastName = ln;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department d) {
        this.department = d;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position p) {
        this.position = p;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hd) {
        this.hireDate = hd;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal s) {
        this.salary = s;
    }

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(LocalDate td) {
        this.terminationDate = td;
    }

    public String getTerminationReason() {
        return terminationReason;
    }

    public void setTerminationReason(String r) {
        this.terminationReason = r;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setPastEmployeeId(int anInt) {
        this.pastEmployeeId = anInt;
    }
}