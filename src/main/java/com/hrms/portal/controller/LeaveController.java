package com.hrms.portal.controller;

import com.hrms.portal.entity.LeaveRequest;
import com.hrms.portal.entity.User;
import com.hrms.portal.repository.UserRepository;
import com.hrms.portal.service.AuditService;
import com.hrms.portal.service.LeaveService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class LeaveController {

    private final LeaveService service;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public LeaveController(LeaveService service, UserRepository userRepository, AuditService auditService) {
        this.service = service;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @GetMapping("/leave")
    public String leavePage() {
        return "leave";
    }

    @GetMapping("/api/leaves")
    @ResponseBody
    public List<LeaveRequest> getLeaves(
            @RequestParam(required = false) String status,
            Authentication auth) {

        boolean isEmployeeOnly = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        if (isEmployeeOnly) {
            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (user.getEmployeeId() == null) {
                return List.of();
            }
            return service.getEmployeeLeaves(user.getEmployeeId());
        }

        if (status != null && !status.isBlank()) {
            return service.getLeavesByStatus(status);
        }
        return service.getAllLeaves();
    }

    @GetMapping("/api/leaves/summary")
    @ResponseBody
    public Map<String, Object> getSummary() {
        return service.getSummary();
    }

    @PostMapping("/api/leaves/apply")
    @ResponseBody
    public ResponseEntity<LeaveRequest> applyLeave(
            @RequestBody LeaveRequest request,
            Authentication auth) {

        boolean isEmployeeOnly = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        if (isEmployeeOnly) {
            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (user.getEmployeeId() == null) {
                throw new IllegalStateException("Your account is not linked to an employee profile.");
            }
            request.setEmployeeId(user.getEmployeeId());
        }

        LeaveRequest applied = service.applyLeave(request);
        auditService.log("APPLY", "LEAVE", applied.getId(),
                "Applied for " + applied.getTotalDays() + " days " + applied.getLeaveType(),
                auth.getName());
        return ResponseEntity.ok(applied);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/api/leaves/{id}/approve")
    @ResponseBody
    public ResponseEntity<LeaveRequest> approveLeave(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication auth) {

        String comment = body != null ? body.getOrDefault("comment", "Approved") : "Approved";
        LeaveRequest approved = service.approveLeave(id, auth.getName(), comment);
        auditService.log("APPROVE", "LEAVE", id,
                "Approved " + approved.getTotalDays() + " days " + approved.getLeaveType() + " for " + approved.getEmployeeName(),
                auth.getName());
        return ResponseEntity.ok(approved);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/api/leaves/{id}/reject")
    @ResponseBody
    public ResponseEntity<LeaveRequest> rejectLeave(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication auth) {

        String comment = body != null ? body.getOrDefault("comment", "Rejected") : "Rejected";
        LeaveRequest rejected = service.rejectLeave(id, auth.getName(), comment);
        auditService.log("REJECT", "LEAVE", id,
                "Rejected leave request with note: " + comment,
                auth.getName());
        return ResponseEntity.ok(rejected);
    }
}

