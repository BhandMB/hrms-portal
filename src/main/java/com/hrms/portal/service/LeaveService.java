package com.hrms.portal.service;

import com.hrms.portal.entity.Attendance;
import com.hrms.portal.entity.Employee;
import com.hrms.portal.entity.LeaveRequest;
import com.hrms.portal.repository.AttendanceRepository;
import com.hrms.portal.repository.EmployeeRepository;
import com.hrms.portal.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LeaveService {

    private final LeaveRequestRepository repository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveBalanceService leaveBalanceService;

    public LeaveService(LeaveRequestRepository repository,
                        EmployeeRepository employeeRepository,
                        AttendanceRepository attendanceRepository,
                        LeaveBalanceService leaveBalanceService) {
        this.repository = repository;
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveBalanceService = leaveBalanceService;
    }

    public List<LeaveRequest> getAllLeaves() {
        return repository.findAllByOrderByAppliedAtDesc();
    }

    public List<LeaveRequest> getEmployeeLeaves(Long employeeId) {
        return repository.findByEmployeeIdOrderByAppliedAtDesc(employeeId);
    }

    public List<LeaveRequest> getLeavesByStatus(String status) {
        return repository.findByStatusOrderByAppliedAtDesc(status.toUpperCase());
    }

    @Transactional
    public LeaveRequest applyLeave(LeaveRequest request) {
        if (request.getEmployeeId() == null) {
            throw new IllegalArgumentException("Employee ID is required");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        Employee emp = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + request.getEmployeeId()));

        request.setEmployeeName(emp.getFirstName() + " " + emp.getLastName());
        request.setDepartment(emp.getDepartment());
        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        request.setTotalDays((int) days);
        request.setStatus("PENDING");
        request.setAppliedAt(LocalDate.now());

        return repository.save(request);
    }

    @Transactional
    public LeaveRequest approveLeave(Long id, String reviewerUsername, String comment) {
        LeaveRequest request = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with id: " + id));

        request.setStatus("APPROVED");
        request.setReviewedBy(reviewerUsername);
        request.setReviewerComment(comment);

        // Deduct from leave balance
        try {
            leaveBalanceService.deductLeave(request.getEmployeeId(), request.getLeaveType(), request.getTotalDays());
        } catch (Exception ignored) {}

        // Record ON_LEAVE attendance records for dates within the range
        LocalDate current = request.getStartDate();
        while (!current.isAfter(request.getEndDate())) {
            LocalDate d = current;
            Attendance att = attendanceRepository.findByEmployeeIdAndDate(request.getEmployeeId(), d)
                    .orElseGet(() -> {
                        Attendance a = new Attendance();
                        a.setEmployeeId(request.getEmployeeId());
                        a.setEmployeeName(request.getEmployeeName());
                        a.setDate(d);
                        return a;
                    });
            att.setStatus("ON_LEAVE");
            att.setRemarks("Leave: " + request.getLeaveType() + (request.getReason() != null ? " - " + request.getReason() : ""));
            att.setWorkHours(0.0);
            attendanceRepository.save(att);
            current = current.plusDays(1);
        }

        return repository.save(request);
    }

    @Transactional
    public LeaveRequest rejectLeave(Long id, String reviewerUsername, String comment) {
        LeaveRequest request = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with id: " + id));

        request.setStatus("REJECTED");
        request.setReviewedBy(reviewerUsername);
        request.setReviewerComment(comment);

        return repository.save(request);
    }

    public Map<String, Object> getSummary() {
        Map<String, Object> map = new HashMap<>();
        map.put("total", repository.count());
        map.put("pending", repository.countByStatus("PENDING"));
        map.put("approved", repository.countByStatus("APPROVED"));
        map.put("rejected", repository.countByStatus("REJECTED"));
        return map;
    }
}

