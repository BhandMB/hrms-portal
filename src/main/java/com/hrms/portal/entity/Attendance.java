package com.hrms.portal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Employee ID is required")
    @Column(nullable = false)
    private Long employeeId;

    private String employeeName;

    @NotNull(message = "Date is required")
    @Column(nullable = false)
    private LocalDate date;

    private String checkInTime;

    private String checkOutTime;

    @Column(nullable = false)
    private String status = "PRESENT"; // PRESENT, ABSENT, HALF_DAY, ON_LEAVE

    private Double workHours = 8.0;

    private String remarks;

    public Attendance() {}

    public Attendance(Long employeeId, String employeeName, LocalDate date, String checkInTime,
                      String checkOutTime, String status, Double workHours, String remarks) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.date = date;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = status;
        this.workHours = workHours;
        this.remarks = remarks;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }

    public String getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getWorkHours() { return workHours; }
    public void setWorkHours(Double workHours) { this.workHours = workHours; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}

