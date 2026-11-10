package com.hrms.portal.repository;

import com.hrms.portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<User> findByEmployeeId(Long employeeId);

    java.util.List<User> findAllByEmployeeId(Long employeeId);
}