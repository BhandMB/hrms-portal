package com.hrms.portal.repository;

import com.hrms.portal.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByEmployeeIdOrderByUploadedAtDesc(Long employeeId);

    long countByEmployeeId(Long employeeId);
}
