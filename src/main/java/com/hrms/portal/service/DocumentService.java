package com.hrms.portal.service;

import com.hrms.portal.entity.Document;
import com.hrms.portal.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository repository;
    private final Path uploadDir;

    public DocumentService(DocumentRepository repository,
                           @Value("${hrms.upload.dir:./uploads}") String uploadPath) {
        this.repository = repository;
        this.uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadPath, e);
        }
    }

    public List<Document> getDocumentsForEmployee(Long employeeId) {
        return repository.findByEmployeeIdOrderByUploadedAtDesc(employeeId);
    }

    @Transactional
    public Document upload(Long employeeId, MultipartFile file, String category, String description, String uploadedBy) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalName = file.getOriginalFilename();
        String storedName = UUID.randomUUID() + "_" + (originalName != null ? originalName : "file");

        try {
            Path target = uploadDir.resolve(storedName);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + originalName, e);
        }

        Document doc = new Document();
        doc.setEmployeeId(employeeId);
        doc.setFileName(storedName);
        doc.setOriginalFileName(originalName != null ? originalName : "file");
        doc.setContentType(file.getContentType());
        doc.setFileSize(file.getSize());
        doc.setCategory(category != null ? category : "OTHER");
        doc.setDescription(description);
        doc.setUploadedBy(uploadedBy);
        doc.setUploadedAt(LocalDateTime.now());

        return repository.save(doc);
    }

    public Path getFilePath(Long documentId) {
        Document doc = repository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with id: " + documentId));
        return uploadDir.resolve(doc.getFileName());
    }

    public Document getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with id: " + id));
    }

    @Transactional
    public void delete(Long id) {
        Document doc = getById(id);
        try {
            Files.deleteIfExists(uploadDir.resolve(doc.getFileName()));
        } catch (IOException ignored) {}
        repository.deleteById(id);
    }
}
