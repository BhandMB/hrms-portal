package com.hrms.portal.service;

import com.hrms.portal.entity.AuditLog;
import com.hrms.portal.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void log(String action, String entityType, Long entityId, String description, String performedBy) {
        AuditLog entry = new AuditLog(action, entityType, entityId, description, performedBy);
        repository.save(entry);
        log.info("AUDIT: {} {} {} by {} — {}", action, entityType, entityId, performedBy, description);
    }

    public List<AuditLog> getAll() {
        return repository.findAllByOrderByPerformedAtDesc();
    }

    public List<AuditLog> getByEntityType(String entityType) {
        return repository.findByEntityTypeOrderByPerformedAtDesc(entityType);
    }

    public List<AuditLog> getByUser(String username) {
        return repository.findByPerformedByOrderByPerformedAtDesc(username);
    }

    public List<AuditLog> getByDateRange(LocalDateTime from, LocalDateTime to) {
        return repository.findByPerformedAtBetweenOrderByPerformedAtDesc(from, to);
    }
}
