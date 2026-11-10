package com.hrms.portal.service;

import com.hrms.portal.entity.User;
import com.hrms.portal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserManagementService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public User getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    @Transactional
    public User createUser(String username, String password, String role, Long employeeId) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (repository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        User user = new User(username, passwordEncoder.encode(password), role != null ? role : "EMPLOYEE");
        user.setEmployeeId(employeeId);
        return repository.save(user);
    }

    @Transactional
    public User updateUser(Long id, String role, Long employeeId, Boolean enabled) {
        User user = getById(id);
        if (role != null) user.setRole(role);
        if (employeeId != null) user.setEmployeeId(employeeId);
        if (enabled != null) user.setEnabled(enabled);
        return repository.save(user);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        User user = getById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        repository.save(user);
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        repository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
