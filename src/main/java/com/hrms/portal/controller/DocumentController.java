package com.hrms.portal.controller;

import com.hrms.portal.entity.Document;
import com.hrms.portal.entity.User;
import com.hrms.portal.repository.UserRepository;
import com.hrms.portal.service.AuditService;
import com.hrms.portal.service.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

@Controller
public class DocumentController {

    private final DocumentService service;
    private final AuditService auditService;
    private final UserRepository userRepository;

    public DocumentController(DocumentService service, AuditService auditService, UserRepository userRepository) {
        this.service = service;
        this.auditService = auditService;
        this.userRepository = userRepository;
    }

    @GetMapping("/api/documents/{employeeId}")
    @ResponseBody
    public List<Document> getDocuments(@PathVariable Long employeeId, Authentication auth) {
        boolean isEmployee = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
        if (isEmployee) {
            User user = userRepository.findByUsername(auth.getName()).orElseThrow();
            if (user.getEmployeeId() == null || !user.getEmployeeId().equals(employeeId)) {
                throw new SecurityException("Access denied");
            }
        }
        return service.getDocumentsForEmployee(employeeId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/api/documents/{employeeId}/upload")
    @ResponseBody
    public ResponseEntity<Document> upload(@PathVariable Long employeeId,
                                            @RequestParam("file") MultipartFile file,
                                            @RequestParam(value = "category", defaultValue = "OTHER") String category,
                                            @RequestParam(value = "description", required = false) String description,
                                            Authentication auth) {
        Document doc = service.upload(employeeId, file, category, description, auth.getName());
        auditService.log("CREATE", "DOCUMENT", doc.getId(),
                "Uploaded document: " + doc.getOriginalFileName() + " for employee #" + employeeId, auth.getName());
        return ResponseEntity.ok(doc);
    }

    @GetMapping("/api/documents/download/{id}")
    @ResponseBody
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        try {
            Document doc = service.getById(id);
            Path filePath = service.getFilePath(id);
            Resource resource = new UrlResource(filePath.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(doc.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getOriginalFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("File download failed", e);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @DeleteMapping("/api/documents/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        service.delete(id);
        auditService.log("DELETE", "DOCUMENT", id, "Deleted document", auth.getName());
        return ResponseEntity.noContent().build();
    }
}
