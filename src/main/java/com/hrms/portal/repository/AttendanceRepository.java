package com.hrms.portal.repository;

import com.hrms.portal.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByDate(LocalDate date);

    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    List<Attendance> findByEmployeeIdOrderByDateDesc(Long employeeId);

    List<Attendance> findAllByOrderByDateDesc();

    long countByDateAndStatus(LocalDate date, String status);
}

