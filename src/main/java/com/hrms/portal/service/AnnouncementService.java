package com.hrms.portal.service;

import com.hrms.portal.entity.Announcement;
import com.hrms.portal.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementService {

    private final AnnouncementRepository repository;

    public AnnouncementService(AnnouncementRepository repository) {
        this.repository = repository;
    }

    public List<Announcement> getAllAnnouncements() {
        return repository.findAllByOrderByPostedAtDesc();
    }

    public List<Announcement> getActiveAnnouncements() {
        return repository.findByActiveTrueOrderByPinnedDescPostedAtDesc();
    }

    public Announcement getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Announcement not found with id: " + id));
    }

    @Transactional
    public Announcement create(Announcement announcement, String postedBy) {
        if (announcement.getTitle() == null || announcement.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (announcement.getContent() == null || announcement.getContent().isBlank()) {
            throw new IllegalArgumentException("Content is required");
        }
        announcement.setPostedBy(postedBy);
        announcement.setPostedAt(LocalDateTime.now());
        return repository.save(announcement);
    }

    @Transactional
    public Announcement update(Long id, Announcement updated) {
        Announcement existing = getById(id);
        existing.setTitle(updated.getTitle());
        existing.setContent(updated.getContent());
        existing.setPriority(updated.getPriority());
        existing.setCategory(updated.getCategory());
        existing.setExpiresAt(updated.getExpiresAt());
        existing.setPinned(updated.isPinned());
        existing.setActive(updated.isActive());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Announcement not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
