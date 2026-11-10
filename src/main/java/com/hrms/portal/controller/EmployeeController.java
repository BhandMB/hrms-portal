package com.hrms.portal.controller;

import com.hrms.portal.entity.Employee;
import com.hrms.portal.entity.User;
import com.hrms.portal.repository.UserRepository;
import com.hrms.portal.service.AuditService;
import com.hrms.portal.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;           
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@Controller
public class EmployeeController {

    private final EmployeeService service;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public EmployeeController(EmployeeService service, UserRepository userRepository, AuditService auditService) {
        this.service = service;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @GetMapping("/")
    public String home() {
        return "dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/employees")
    public String employees() {
        return "employees";
    }

    @GetMapping("/employee-form")
    public String employeeForm() {
        return "employee-form";
    }

    @GetMapping("/employee-profile")
    public String employeeProfile() {
        return "employee-profile";
    }

    /*
     * ADMIN and HR can see all employees.
     * EMPLOYEE can see only their own record.
     */
    @GetMapping("/api/employees")
    @ResponseBody
    public List<Employee> getEmployees(Authentication auth) {

        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"))) {

            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getEmployeeId() == null) {
                return List.of();
            }

            return List.of(service.getEmployeeById(user.getEmployeeId()));
        }

        return service.getAllEmployees();
    }

    /*
     * ADMIN and HR can view any employee.
     * EMPLOYEE can view only their own employee record.
     */
    @GetMapping("/api/employees/{id}")
    @ResponseBody
    public ResponseEntity<Employee> getEmployee(
            @PathVariable Long id,
            Authentication auth) {

        boolean employeeRole = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        if (employeeRole) {

            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getEmployeeId() == null ||
                    !user.getEmployeeId().equals(id)) {

                return ResponseEntity.status(403).build();
            }
        }

        return ResponseEntity.ok(service.getEmployeeById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/api/employees")
    @ResponseBody
    public ResponseEntity<Employee> create(
            @Valid @RequestBody Employee employee,
            Authentication auth) {

        Employee created = service.createEmployee(employee);
        auditService.log("CREATE", "EMPLOYEE", created.getId(),
                "Created employee " + created.getFirstName() + " " + created.getLastName() + " (" + created.getDepartment() + ")",
                auth.getName());
        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PutMapping("/api/employees/{id}")
    @ResponseBody
    public ResponseEntity<Employee> update(
            @PathVariable Long id,
            @Valid @RequestBody Employee employee,
            Authentication auth) {

        Employee updated = service.updateEmployee(id, employee);
        auditService.log("UPDATE", "EMPLOYEE", id,
                "Updated employee " + updated.getFirstName() + " " + updated.getLastName(),
                auth.getName());
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/employees/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Authentication auth) {

        service.deleteEmployee(id);
        auditService.log("DELETE", "EMPLOYEE", id,
                "Deleted employee ID #" + id,
                auth.getName());
        return ResponseEntity.noContent().build();
    }
}