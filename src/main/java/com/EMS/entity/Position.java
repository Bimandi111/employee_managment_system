package com.EMS.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "positions")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "position_id")
    private Integer positionId;

    @NotBlank(message = "Position title is required")
    @Size(max = 100)
    @Column(name = "title", nullable = false, unique = true, length = 100)
    private String title;

    @NotBlank(message = "Pay grade is required")
    @Column(name = "pay_grade", nullable = false, length = 10)
    private String payGrade;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "position", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Employee> employees;

    public Position() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Integer getPositionId() {
        return positionId;
    }

    public void setPositionId(Integer id) {
        this.positionId = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPayGrade() {
        return payGrade;
    }

    public void setPayGrade(String pg) {
        this.payGrade = pg;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> e) {
        this.employees = e;
    }
}