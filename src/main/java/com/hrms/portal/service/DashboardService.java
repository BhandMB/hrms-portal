package com.hrms.portal.service;

import com.hrms.portal.entity.Employee;
import com.hrms.portal.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AnnouncementRepository announcementRepository;

    public DashboardService(EmployeeRepository employeeRepository,
                            DepartmentRepository departmentRepository,
                            AttendanceRepository attendanceRepository,
                            LeaveRequestRepository leaveRequestRepository,
                            AnnouncementRepository announcementRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.announcementRepository = announcementRepository;
    }

    public Map<String, Object> getDashboardStats() {
        List<Employee> employees = employeeRepository.findAll();
        LocalDate today = LocalDate.now();

        Map<String, Object> stats = new HashMap<>();

        // Employee stats
        long totalEmployees = employees.size();
        long activeEmployees = employees.stream().filter(Employee::isActive).count();
        stats.put("totalEmployees", totalEmployees);
        stats.put("activeEmployees", activeEmployees);
        stats.put("inactiveEmployees", totalEmployees - activeEmployees);

        // Department stats
        long totalDepartments = departmentRepository.count();
        stats.put("totalDepartments", totalDepartments);

        // Payroll stats
        BigDecimal totalAnnualSalary = employees.stream()
                .map(e -> e.getSalary() == null ? BigDecimal.ZERO : e.getSalary())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("monthlyPayroll", totalAnnualSalary.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));

        // Attendance stats
        long presentToday = attendanceRepository.countByDateAndStatus(today, "PRESENT");
        long halfDayToday = attendanceRepository.countByDateAndStatus(today, "HALF_DAY");
        long onLeaveToday = attendanceRepository.countByDateAndStatus(today, "ON_LEAVE");
        long absentToday = Math.max(0, totalEmployees - (presentToday + halfDayToday + onLeaveToday));
        stats.put("presentToday", presentToday);
        stats.put("halfDayToday", halfDayToday);
        stats.put("onLeaveToday", onLeaveToday);
        stats.put("absentToday", absentToday);

        // Leave stats
        long pendingLeaves = leaveRequestRepository.countByStatus("PENDING");
        stats.put("pendingLeaves", pendingLeaves);

        // Announcement count
        long activeAnnouncements = announcementRepository.countByActiveTrue();
        stats.put("activeAnnouncements", activeAnnouncements);

        // Department distribution
        Map<String, Long> byDept = employees.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getDepartment() == null ? "Unassigned" : e.getDepartment(),
                        Collectors.counting()));
        stats.put("byDepartment", byDept);

        // Upcoming birthdays (next 30 days)
        List<Map<String, Object>> upcomingBirthdays = employees.stream()
                .filter(e -> e.getDateOfBirth() != null)
                .filter(e -> {
                    LocalDate bd = e.getDateOfBirth().withYear(today.getYear());
                    if (bd.isBefore(today)) bd = bd.plusYears(1);
                    return !bd.isAfter(today.plusDays(30));
                })
                .sorted(Comparator.comparing(e -> {
                    LocalDate bd = e.getDateOfBirth().withYear(today.getYear());
                    if (bd.isBefore(today)) bd = bd.plusYears(1);
                    return bd;
                }))
                .limit(5)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getFirstName() + " " + e.getLastName());
                    m.put("department", e.getDepartment());
                    LocalDate bd = e.getDateOfBirth().withYear(today.getYear());
                    if (bd.isBefore(today)) bd = bd.plusYears(1);
                    m.put("date", bd.toString());
                    return m;
                })
                .toList();
        stats.put("upcomingBirthdays", upcomingBirthdays);

        // Work anniversaries (next 30 days)
        List<Map<String, Object>> anniversaries = employees.stream()
                .filter(e -> e.getJoiningDate() != null && e.getJoiningDate().isBefore(today))
                .filter(e -> {
                    LocalDate ad = e.getJoiningDate().withYear(today.getYear());
                    if (ad.isBefore(today)) ad = ad.plusYears(1);
                    return !ad.isAfter(today.plusDays(30));
                })
                .sorted(Comparator.comparing(e -> {
                    LocalDate ad = e.getJoiningDate().withYear(today.getYear());
                    if (ad.isBefore(today)) ad = ad.plusYears(1);
                    return ad;
                }))
                .limit(5)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getFirstName() + " " + e.getLastName());
                    m.put("department", e.getDepartment());
                    int years = today.getYear() - e.getJoiningDate().getYear();
                    LocalDate ad = e.getJoiningDate().withYear(today.getYear());
                    if (ad.isBefore(today)) { ad = ad.plusYears(1); years++; }
                    m.put("date", ad.toString());
                    m.put("years", years);
                    return m;
                })
                .toList();
        stats.put("upcomingAnniversaries", anniversaries);

        return stats;
    }
}
