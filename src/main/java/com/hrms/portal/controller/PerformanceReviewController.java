package com.hrms.portal.controller;

import com.hrms.portal.entity.PerformanceReview;
import com.hrms.portal.entity.User;
import com.hrms.portal.repository.UserRepository;
import com.hrms.portal.service.AuditService;
import com.hrms.portal.service.PerformanceReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class PerformanceReviewController {

    private final PerformanceReviewService service;
    private final AuditService auditService;
    private final UserRepository userRepository;

    public PerformanceReviewController(PerformanceReviewService service,
                                        AuditService auditService,
                                        UserRepository userRepository) {
        this.service = service;
        this.auditService = auditService;
        this.userRepository = userRepository;
    }

    @GetMapping("/performance")
    public String performancePage() {
        return "performance-reviews";
    }

    @GetMapping("/api/performance-reviews")
    @ResponseBody
    public List<PerformanceReview> getAll(Authentication auth) {
        boolean isEmployee = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
        if (isEmployee) {
            User user = userRepository.findByUsername(auth.getName()).orElseThrow();
            if (user.getEmployeeId() == null) return List.of();
            return service.getByEmployee(user.getEmployeeId());
        }
        return service.getAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/api/performance-reviews")
    @ResponseBody
    public ResponseEntity<PerformanceReview> create(@RequestBody PerformanceReview review, Authentication auth) {
        PerformanceReview created = service.create(review, auth.getName());
        auditService.log("CREATE", "PERFORMANCE_REVIEW", created.getId(),
                "Created review for " + created.getEmployeeName() + " — " + created.getReviewPeriod(), auth.getName());
        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PutMapping("/api/performance-reviews/{id}")
    @ResponseBody
    public ResponseEntity<PerformanceReview> update(@PathVariable Long id,
                                                     @RequestBody PerformanceReview review,
                                                     Authentication auth) {
        PerformanceReview updated = service.update(id, review);
        auditService.log("UPDATE", "PERFORMANCE_REVIEW", id,
                "Updated review for " + updated.getEmployeeName(), auth.getName());
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/api/performance-reviews/{id}/submit")
    @ResponseBody
    public ResponseEntity<PerformanceReview> submit(@PathVariable Long id, Authentication auth) {
        PerformanceReview review = service.submit(id);
        auditService.log("UPDATE", "PERFORMANCE_REVIEW", id,
                "Submitted review for " + review.getEmployeeName(), auth.getName());
        return ResponseEntity.ok(review);
    }

    @PostMapping("/api/performance-reviews/{id}/acknowledge")
    @ResponseBody
    public ResponseEntity<PerformanceReview> acknowledge(@PathVariable Long id, Authentication auth) {
        PerformanceReview review = service.acknowledge(id);
        auditService.log("UPDATE", "PERFORMANCE_REVIEW", id,
                "Employee acknowledged review", auth.getName());
        return ResponseEntity.ok(review);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/performance-reviews/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        service.delete(id);
        auditService.log("DELETE", "PERFORMANCE_REVIEW", id, "Deleted performance review", auth.getName());
        return ResponseEntity.noContent().build();
    }
}
