package com.hrms.portal.controller;

import com.hrms.portal.entity.Announcement;
import com.hrms.portal.service.AnnouncementService;
import com.hrms.portal.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AnnouncementController {

    private final AnnouncementService service;
    private final AuditService auditService;

    public AnnouncementController(AnnouncementService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping("/announcements")
    public String announcementsPage() {
        return "announcements";
    }

    @GetMapping("/api/announcements")
    @ResponseBody
    public List<Announcement> getAnnouncements(@RequestParam(required = false) Boolean activeOnly) {
        if (Boolean.TRUE.equals(activeOnly)) {
            return service.getActiveAnnouncements();
        }
        return service.getAllAnnouncements();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/api/announcements")
    @ResponseBody
    public ResponseEntity<Announcement> create(@RequestBody Announcement announcement, Authentication auth) {
        Announcement created = service.create(announcement, auth.getName());
        auditService.log("CREATE", "ANNOUNCEMENT", created.getId(),
                "Created announcement: " + created.getTitle(), auth.getName());
        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PutMapping("/api/announcements/{id}")
    @ResponseBody
    public ResponseEntity<Announcement> update(@PathVariable Long id,
                                                @RequestBody Announcement announcement,
                                                Authentication auth) {
        Announcement updated = service.update(id, announcement);
        auditService.log("UPDATE", "ANNOUNCEMENT", id,
                "Updated announcement: " + updated.getTitle(), auth.getName());
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/announcements/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        service.delete(id);
        auditService.log("DELETE", "ANNOUNCEMENT", id, "Deleted announcement", auth.getName());
        return ResponseEntity.noContent().build();
    }
}
