package com.hrms.portal.repository;

import com.hrms.portal.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByActiveTrueOrderByPinnedDescPostedAtDesc();

    List<Announcement> findAllByOrderByPostedAtDesc();

    List<Announcement> findByActiveTrueAndExpiresAtAfterOrExpiresAtIsNullOrderByPinnedDescPostedAtDesc(LocalDate date);

    long countByActiveTrue();
}
