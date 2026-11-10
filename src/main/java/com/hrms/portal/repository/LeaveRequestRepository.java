package com.hrms.portal.repository;

import com.hrms.portal.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeIdOrderByAppliedAtDesc(Long employeeId);

    List<LeaveRequest> findByStatusOrderByAppliedAtDesc(String status);

    List<LeaveRequest> findAllByOrderByAppliedAtDesc();

    long countByStatus(String status);
}

