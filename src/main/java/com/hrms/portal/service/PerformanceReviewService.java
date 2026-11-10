package com.hrms.portal.service;

import com.hrms.portal.entity.Employee;
import com.hrms.portal.entity.PerformanceReview;
import com.hrms.portal.repository.EmployeeRepository;
import com.hrms.portal.repository.PerformanceReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PerformanceReviewService {

    private final PerformanceReviewRepository repository;
    private final EmployeeRepository employeeRepository;

    public PerformanceReviewService(PerformanceReviewRepository repository,
                                    EmployeeRepository employeeRepository) {
        this.repository = repository;
        this.employeeRepository = employeeRepository;
    }

    public List<PerformanceReview> getAll() {
        return repository.findAllByOrderByReviewDateDesc();
    }

    public List<PerformanceReview> getByEmployee(Long employeeId) {
        return repository.findByEmployeeIdOrderByReviewDateDesc(employeeId);
    }

    public PerformanceReview getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Performance review not found with id: " + id));
    }

    @Transactional
    public PerformanceReview create(PerformanceReview review, String reviewedBy) {
        if (review.getEmployeeId() == null) {
            throw new IllegalArgumentException("Employee ID is required");
        }
        if (review.getReviewPeriod() == null || review.getReviewPeriod().isBlank()) {
            throw new IllegalArgumentException("Review period is required");
        }

        Employee emp = employeeRepository.findById(review.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + review.getEmployeeId()));

        review.setEmployeeName(emp.getFirstName() + " " + emp.getLastName());
        review.setDepartment(emp.getDepartment());
        review.setReviewedBy(reviewedBy);
        review.setReviewDate(LocalDate.now());
        review.setStatus("DRAFT");

        return repository.save(review);
    }

    @Transactional
    public PerformanceReview update(Long id, PerformanceReview updated) {
        PerformanceReview existing = getById(id);
        existing.setReviewPeriod(updated.getReviewPeriod());
        existing.setRating(updated.getRating());
        existing.setStrengths(updated.getStrengths());
        existing.setAreasOfImprovement(updated.getAreasOfImprovement());
        existing.setGoals(updated.getGoals());
        existing.setReviewerComments(updated.getReviewerComments());
        return repository.save(existing);
    }

    @Transactional
    public PerformanceReview submit(Long id) {
        PerformanceReview review = getById(id);
        review.setStatus("SUBMITTED");
        return repository.save(review);
    }

    @Transactional
    public PerformanceReview acknowledge(Long id) {
        PerformanceReview review = getById(id);
        review.setStatus("ACKNOWLEDGED");
        return repository.save(review);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Performance review not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
