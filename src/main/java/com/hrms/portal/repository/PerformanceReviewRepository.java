package com.hrms.portal.repository;

import com.hrms.portal.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {

    List<PerformanceReview> findByEmployeeIdOrderByReviewDateDesc(Long employeeId);

    List<PerformanceReview> findAllByOrderByReviewDateDesc();

    List<PerformanceReview> findByStatusOrderByReviewDateDesc(String status);

    long countByStatus(String status);
}
