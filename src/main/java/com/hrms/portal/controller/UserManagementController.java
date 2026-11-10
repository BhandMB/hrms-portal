package com.hrms.portal.controller;

import com.hrms.portal.entity.User;
import com.hrms.portal.service.AuditService;
import com.hrms.portal.service.UserManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class UserManagementController {

    private final UserManagementService service;
    private final AuditService auditService;

    public UserManagementController(UserManagementService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping("/user-management")
    @PreAuthorize("hasRole('ADMIN')")
    public String userManagementPage() {
        return "user-management";
    }

    @GetMapping("/api/users")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public List<User> getAll() {
        return service.getAllUsers();
    }

    @PostMapping("/api/users")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<User> create(@RequestBody Map<String, Object> body, Authentication auth) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String role = (String) body.get("role");
        Long employeeId = body.get("employeeId") != null ? Long.valueOf(body.get("employeeId").toString()) : null;

        User user = service.createUser(username, password, role, employeeId);
        auditService.log("CREATE", "USER", user.getId(),
                "Created user account: " + username + " (role: " + role + ")", auth.getName());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/api/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<User> update(@PathVariable Long id,
                                        @RequestBody Map<String, Object> body,
                                        Authentication auth) {
        String role = (String) body.get("role");
        Long employeeId = body.get("employeeId") != null ? Long.valueOf(body.get("employeeId").toString()) : null;
        Boolean enabled = body.get("enabled") != null ? (Boolean) body.get("enabled") : null;

        User user = service.updateUser(id, role, employeeId, enabled);
        auditService.log("UPDATE", "USER", id,
                "Updated user: " + user.getUsername(), auth.getName());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/api/users/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Long id,
                                                              @RequestBody Map<String, String> body,
                                                              Authentication auth) {
        service.resetPassword(id, body.get("password"));
        auditService.log("UPDATE", "USER", id, "Password reset by admin", auth.getName());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @DeleteMapping("/api/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        service.deleteUser(id);
        auditService.log("DELETE", "USER", id, "Deleted user account", auth.getName());
        return ResponseEntity.noContent().build();
    }
}
