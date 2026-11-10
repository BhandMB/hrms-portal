package com.hrms.portal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Content is required")
    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT

    @Column(nullable = false)
    private String category = "GENERAL"; // GENERAL, HR, IT, EVENT, POLICY

    private String postedBy;

    @Column(nullable = false)
    private LocalDateTime postedAt = LocalDateTime.now();

    private LocalDate expiresAt;

    private boolean pinned = false;

    private boolean active = true;

    public Announcement() {}

    public Announcement(String title, String content, String priority, String category, String postedBy) {
        this.title = title;
        this.content = content;
        this.priority = priority;
        this.category = category;
        this.postedBy = postedBy;
        this.postedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPostedBy() { return postedBy; }
    public void setPostedBy(String postedBy) { this.postedBy = postedBy; }

    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }

    public LocalDate getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDate expiresAt) { this.expiresAt = expiresAt; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
