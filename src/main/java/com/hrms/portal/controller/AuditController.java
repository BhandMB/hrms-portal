package com.hrms.portal.controller;

import com.hrms.portal.entity.AuditLog;
import com.hrms.portal.service.AuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AuditController {

    private final AuditService service;

    public AuditController(AuditService service) {
        this.service = service;
    }

    @GetMapping("/audit-log")
    @PreAuthorize("hasRole('ADMIN')")
    public String auditPage() {
        return "audit-log";
    }

    @GetMapping("/api/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public List<AuditLog> getAll(@RequestParam(required = false) String entityType,
                                  @RequestParam(required = false) String user) {
        if (entityType != null && !entityType.isBlank()) {
            return service.getByEntityType(entityType);
        }
        if (user != null && !user.isBlank()) {
            return service.getByUser(user);
        }
        return service.getAll();
    }
}
