package com.hrms.portal.controller;

import com.hrms.portal.entity.Department;
import com.hrms.portal.entity.Employee;
import com.hrms.portal.entity.LeaveBalance;
import com.hrms.portal.entity.User;
import com.hrms.portal.repository.AnnouncementRepository;
import com.hrms.portal.repository.DepartmentRepository;
import com.hrms.portal.repository.EmployeeRepository;
import com.hrms.portal.repository.UserRepository;
import com.hrms.portal.service.DashboardService;
import com.hrms.portal.service.LeaveBalanceService;
import com.hrms.portal.service.UserManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SettingsController {

    private final UserManagementService userService;
    private final LeaveBalanceService leaveBalanceService;
    private final DashboardService dashboardService;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AnnouncementRepository announcementRepository;

    public SettingsController(UserManagementService userService,
                              LeaveBalanceService leaveBalanceService,
                              DashboardService dashboardService,
                              UserRepository userRepository,
                              EmployeeRepository employeeRepository,
                              DepartmentRepository departmentRepository,
                              AnnouncementRepository announcementRepository) {
        this.userService = userService;
        this.leaveBalanceService = leaveBalanceService;
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.announcementRepository = announcementRepository;
    }

    @GetMapping("/settings")
    public String settingsPage() {
        return "settings";
    }

    @PostMapping("/api/settings/change-password")
    @ResponseBody
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody Map<String, String> body,
                                                               Authentication auth) {
        userService.changePassword(auth.getName(), body.get("currentPassword"), body.get("newPassword"));
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @GetMapping("/api/leave-balances")
    @ResponseBody
    public List<LeaveBalance> getLeaveBalances(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        if (user.getEmployeeId() == null) return List.of();
        return leaveBalanceService.getBalancesForEmployee(user.getEmployeeId());
    }

    @GetMapping("/api/leave-balances/{employeeId}")
    @ResponseBody
    public List<LeaveBalance> getLeaveBalancesForEmployee(@PathVariable Long employeeId) {
        return leaveBalanceService.getBalancesForEmployee(employeeId);
    }

    @GetMapping("/api/dashboard/stats")
    @ResponseBody
    public Map<String, Object> getDashboardStats() {
        return dashboardService.getDashboardStats();
    }

    @GetMapping("/api/search")
    @ResponseBody
    public Map<String, Object> globalSearch(@RequestParam(defaultValue = "") String q, Authentication auth) {
        String query = q.trim();
        if (query.isEmpty()) {
            return Map.of("employees", List.of(), "departments", List.of(), "announcements", List.of());
        }

        List<Employee> employees = employeeRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query, query);

        List<Department> departments = departmentRepository.findAll().stream()
                .filter(d -> d.getName().toLowerCase().contains(query.toLowerCase()) ||
                        (d.getDescription() != null && d.getDescription().toLowerCase().contains(query.toLowerCase())))
                .toList();

        var announcements = announcementRepository.findAll().stream()
                .filter(a -> a.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        (a.getContent() != null && a.getContent().toLowerCase().contains(query.toLowerCase())))
                .limit(5)
                .toList();

        Map<String, Object> results = new HashMap<>();
        results.put("query", query);
        results.put("employees", employees);
        results.put("departments", departments);
        results.put("announcements", announcements);
        return results;
    }
}
