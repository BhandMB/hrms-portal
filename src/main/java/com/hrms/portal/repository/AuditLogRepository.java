package com.hrms.portal.repository;

import com.hrms.portal.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByPerformedAtDesc();

    List<AuditLog> findByEntityTypeOrderByPerformedAtDesc(String entityType);

    List<AuditLog> findByPerformedByOrderByPerformedAtDesc(String performedBy);

    List<AuditLog> findByPerformedAtBetweenOrderByPerformedAtDesc(LocalDateTime from, LocalDateTime to);

    long countByAction(String action);
}
