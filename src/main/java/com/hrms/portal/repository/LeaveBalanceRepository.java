package com.hrms.portal.repository;

import com.hrms.portal.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    List<LeaveBalance> findByEmployeeId(Long employeeId);

    List<LeaveBalance> findByEmployeeIdAndYear(Long employeeId, Integer year);

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeAndYear(Long employeeId, String leaveType, Integer year);
}
