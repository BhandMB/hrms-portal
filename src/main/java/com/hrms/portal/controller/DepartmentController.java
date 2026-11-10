package com.hrms.portal.controller;

import com.hrms.portal.entity.Department;
import com.hrms.portal.service.AuditService;
import com.hrms.portal.service.DepartmentService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@Controller
public class DepartmentController {

    private final DepartmentService service;
    private final AuditService auditService;

    public DepartmentController(DepartmentService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    // Department page
    @GetMapping("/departments")
    public String departments() {
        return "departments";
    }

    // Get all departments
    @GetMapping("/api/departments")
    @ResponseBody
    public List<Department> getDepartments() {
        return service.getAllDepartments();
    }

    // Get department
    @GetMapping("/api/departments/{id}")
    @ResponseBody
    public ResponseEntity<Department> getDepartment(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getDepartmentById(id)
        );
    }

    // ADMIN and HR can create
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/api/departments")
    @ResponseBody
    public ResponseEntity<Department> createDepartment(
            @Valid @RequestBody Department department,
            Authentication auth) {

        Department created = service.createDepartment(department);
        auditService.log("CREATE", "DEPARTMENT", created.getId(),
                "Created department: " + created.getName(), auth.getName());
        return ResponseEntity.ok(created);
    }

    // ADMIN and HR can update
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PutMapping("/api/departments/{id}")
    @ResponseBody
    public ResponseEntity<Department> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody Department department,
            Authentication auth) {

        Department updated = service.updateDepartment(id, department);
        auditService.log("UPDATE", "DEPARTMENT", id,
                "Updated department: " + updated.getName(), auth.getName());
        return ResponseEntity.ok(updated);
    }

    // Only ADMIN can delete
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/departments/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable Long id,
            Authentication auth) {

        service.deleteDepartment(id);
        auditService.log("DELETE", "DEPARTMENT", id,
                "Deleted department ID #" + id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}