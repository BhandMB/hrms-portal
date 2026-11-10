package com.hrms.portal.controller;

import com.hrms.portal.entity.Holiday;
import com.hrms.portal.service.AuditService;
import com.hrms.portal.service.HolidayService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class HolidayController {

    private final HolidayService service;
    private final AuditService auditService;

    public HolidayController(HolidayService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping("/holidays")
    public String holidaysPage() {
        return "holidays";
    }

    @GetMapping("/api/holidays")
    @ResponseBody
    public List<Holiday> getHolidays(@RequestParam(required = false) Integer year) {
        if (year != null) {
            return service.getHolidaysForYear(year);
        }
        return service.getHolidaysForYear(LocalDate.now().getYear());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/api/holidays")
    @ResponseBody
    public ResponseEntity<Holiday> create(@RequestBody Holiday holiday, Authentication auth) {
        Holiday created = service.create(holiday);
        auditService.log("CREATE", "HOLIDAY", created.getId(),
                "Added holiday: " + created.getName() + " on " + created.getDate(), auth.getName());
        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PutMapping("/api/holidays/{id}")
    @ResponseBody
    public ResponseEntity<Holiday> update(@PathVariable Long id,
                                           @RequestBody Holiday holiday,
                                           Authentication auth) {
        Holiday updated = service.update(id, holiday);
        auditService.log("UPDATE", "HOLIDAY", id,
                "Updated holiday: " + updated.getName(), auth.getName());
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/holidays/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        service.delete(id);
        auditService.log("DELETE", "HOLIDAY", id, "Deleted holiday", auth.getName());
        return ResponseEntity.noContent().build();
    }
}
