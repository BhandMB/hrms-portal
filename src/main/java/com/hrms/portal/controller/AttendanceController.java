package com.hrms.portal.controller;

import com.hrms.portal.entity.Attendance;
import com.hrms.portal.entity.User;
import com.hrms.portal.repository.UserRepository;
import com.hrms.portal.service.AttendanceService;
import com.hrms.portal.service.AuditService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class AttendanceController {

    private final AttendanceService service;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public AttendanceController(AttendanceService service, UserRepository userRepository, AuditService auditService) {
        this.service = service;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @GetMapping("/attendance")
    public String attendancePage() {
        return "attendance";
    }

    @GetMapping("/api/attendance")
    @ResponseBody
    public List<Attendance> getAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication auth) {

        boolean isEmployeeOnly = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        if (isEmployeeOnly) {
            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (user.getEmployeeId() == null) {
                return List.of();
            }
            return service.getEmployeeAttendance(user.getEmployeeId());
        }

        if (date != null) {
            return service.getAttendanceByDate(date);
        }
        return service.getAllAttendance();
    }

    @GetMapping("/api/attendance/summary")
    @ResponseBody
    public Map<String, Object> getSummary() {
        return service.getTodaySummary();
    }

    @GetMapping("/api/attendance/today")
    @ResponseBody
    public ResponseEntity<Attendance> getTodayStatus(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getEmployeeId() == null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(service.getTodayAttendance(user.getEmployeeId()));
    }

    @PostMapping("/api/attendance/clock-in")
    @ResponseBody
    public ResponseEntity<Attendance> clockIn(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getEmployeeId() == null) {
            throw new IllegalStateException("Your account is not linked to an employee profile. Contact HR.");
        }
        Attendance att = service.clockIn(user.getEmployeeId());
        auditService.log("CLOCK_IN", "ATTENDANCE", att.getId(),
                "Clocked in at " + att.getCheckInTime(), auth.getName());
        return ResponseEntity.ok(att);
    }

    @PostMapping("/api/attendance/clock-out")
    @ResponseBody
    public ResponseEntity<Attendance> clockOut(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getEmployeeId() == null) {
            throw new IllegalStateException("Your account is not linked to an employee profile. Contact HR.");
        }
        Attendance att = service.clockOut(user.getEmployeeId());
        auditService.log("CLOCK_OUT", "ATTENDANCE", att.getId(),
                "Clocked out at " + att.getCheckOutTime() + " (Hours: " + att.getWorkHours() + ")", auth.getName());
        return ResponseEntity.ok(att);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/api/attendance")
    @ResponseBody
    public ResponseEntity<Attendance> recordAttendance(@RequestBody Attendance attendance, Authentication auth) {
        Attendance saved = service.recordAttendance(attendance);
        auditService.log("RECORD", "ATTENDANCE", saved.getId(),
                "Manual attendance recorded for employee #" + saved.getEmployeeId() + " (" + saved.getStatus() + ")",
                auth.getName());
        return ResponseEntity.ok(saved);
    }
}

