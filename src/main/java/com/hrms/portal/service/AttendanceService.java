package com.hrms.portal.service;

import com.hrms.portal.entity.Attendance;
import com.hrms.portal.entity.Employee;
import com.hrms.portal.repository.AttendanceRepository;
import com.hrms.portal.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceService {

    private final AttendanceRepository repository;
    private final EmployeeRepository employeeRepository;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    public AttendanceService(AttendanceRepository repository, EmployeeRepository employeeRepository) {
        this.repository = repository;
        this.employeeRepository = employeeRepository;
    }

    public List<Attendance> getAllAttendance() {
        return repository.findAllByOrderByDateDesc();
    }

    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return repository.findByDate(date);
    }

    public List<Attendance> getEmployeeAttendance(Long employeeId) {
        return repository.findByEmployeeIdOrderByDateDesc(employeeId);
    }

    public Attendance getTodayAttendance(Long employeeId) {
        return repository.findByEmployeeIdAndDate(employeeId, LocalDate.now()).orElse(null);
    }

    @Transactional
    public Attendance clockIn(Long employeeId) {
        LocalDate today = LocalDate.now();
        Attendance existing = repository.findByEmployeeIdAndDate(employeeId, today).orElse(null);
        if (existing != null && existing.getCheckInTime() != null) {
            throw new IllegalStateException("You have already clocked in for today at " + existing.getCheckInTime());
        }

        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));

        if (existing == null) {
            existing = new Attendance();
            existing.setEmployeeId(employeeId);
            existing.setEmployeeName(emp.getFirstName() + " " + emp.getLastName());
            existing.setDate(today);
        }

        existing.setCheckInTime(LocalTime.now().format(TIME_FMT));
        existing.setStatus("PRESENT");
        existing.setWorkHours(8.0);
        return repository.save(existing);
    }

    @Transactional
    public Attendance clockOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        Attendance existing = repository.findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new IllegalStateException("No clock-in record found for today. Please clock in first."));

        if (existing.getCheckOutTime() != null) {
            throw new IllegalStateException("You have already clocked out for today at " + existing.getCheckOutTime());
        }

        LocalTime now = LocalTime.now();
        existing.setCheckOutTime(now.format(TIME_FMT));

        try {
            LocalTime inTime = LocalTime.parse(existing.getCheckInTime(), TIME_FMT);
            double hours = Math.max(0.5, Math.round((Duration.between(inTime, now).toMinutes() / 60.0) * 10.0) / 10.0);
            existing.setWorkHours(hours);
            if (hours < 4.0) {
                existing.setStatus("HALF_DAY");
            }
        } catch (Exception ignored) {
            existing.setWorkHours(8.0);
        }

        return repository.save(existing);
    }

    @Transactional
    public Attendance recordAttendance(Attendance attendance) {
        if (attendance.getEmployeeId() == null) {
            throw new IllegalArgumentException("Employee ID is required");
        }
        if (attendance.getDate() == null) {
            attendance.setDate(LocalDate.now());
        }
        if (attendance.getEmployeeName() == null || attendance.getEmployeeName().isBlank()) {
            employeeRepository.findById(attendance.getEmployeeId()).ifPresent(e ->
                    attendance.setEmployeeName(e.getFirstName() + " " + e.getLastName())
            );
        }
        return repository.save(attendance);
    }

    public Map<String, Object> getTodaySummary() {
        LocalDate today = LocalDate.now();
        long totalEmployees = employeeRepository.count();
        long present = repository.countByDateAndStatus(today, "PRESENT");
        long halfDay = repository.countByDateAndStatus(today, "HALF_DAY");
        long onLeave = repository.countByDateAndStatus(today, "ON_LEAVE");
        long absent = Math.max(0, totalEmployees - (present + halfDay + onLeave));

        Map<String, Object> summary = new HashMap<>();
        summary.put("date", today.toString());
        summary.put("totalEmployees", totalEmployees);
        summary.put("present", present);
        summary.put("halfDay", halfDay);
        summary.put("onLeave", onLeave);
        summary.put("absent", absent);
        return summary;
    }
}

