package com.hrms.portal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "leave_balances", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employeeId", "leaveType"})
})
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private String leaveType; // CASUAL, SICK, ANNUAL, UNPAID

    @Column(nullable = false)
    private Integer totalAllocated = 0;

    @Column(nullable = false)
    private Integer used = 0;

    @Column(name = "leave_year")
    private Integer year;

    public LeaveBalance() {}

    public LeaveBalance(Long employeeId, String leaveType, Integer totalAllocated, Integer year) {
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.totalAllocated = totalAllocated;
        this.used = 0;
        this.year = year;
    }

    public Integer getRemaining() {
        return Math.max(0, totalAllocated - used);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public Integer getTotalAllocated() { return totalAllocated; }
    public void setTotalAllocated(Integer totalAllocated) { this.totalAllocated = totalAllocated; }

    public Integer getAllocatedDays() { return totalAllocated; }
    public void setAllocatedDays(Integer allocatedDays) { this.totalAllocated = allocatedDays; }

    public Integer getUsed() { return used; }
    public void setUsed(Integer used) { this.used = used; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
}
