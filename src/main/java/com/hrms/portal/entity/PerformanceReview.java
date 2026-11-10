package com.hrms.portal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    private String employeeName;

    private String department;

    @Column(nullable = false)
    private String reviewPeriod; // e.g., "Q1 2026", "H1 2026", "2025-2026"

    @Min(1) @Max(5)
    @Column(nullable = false)
    private Integer rating = 3; // 1=Poor, 2=Below Average, 3=Meets Expectations, 4=Exceeds, 5=Outstanding

    @Column(length = 1000)
    private String strengths;

    @Column(length = 1000)
    private String areasOfImprovement;

    @Column(length = 1000)
    private String goals;

    @Column(length = 500)
    private String reviewerComments;

    @Column(nullable = false)
    private String reviewedBy;

    @Column(nullable = false)
    private LocalDate reviewDate = LocalDate.now();

    @Column(nullable = false)
    private String status = "DRAFT"; // DRAFT, SUBMITTED, ACKNOWLEDGED

    public PerformanceReview() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getReviewPeriod() { return reviewPeriod; }
    public void setReviewPeriod(String reviewPeriod) { this.reviewPeriod = reviewPeriod; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }

    public String getAreasOfImprovement() { return areasOfImprovement; }
    public void setAreasOfImprovement(String areasOfImprovement) { this.areasOfImprovement = areasOfImprovement; }

    public String getGoals() { return goals; }
    public void setGoals(String goals) { this.goals = goals; }

    public String getReviewerComments() { return reviewerComments; }
    public void setReviewerComments(String reviewerComments) { this.reviewerComments = reviewerComments; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDate getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
