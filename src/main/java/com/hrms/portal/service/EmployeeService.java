package com.hrms.portal.service;

import com.hrms.portal.entity.Employee;
import com.hrms.portal.repository.DepartmentRepository;
import com.hrms.portal.repository.EmployeeRepository;
import com.hrms.portal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final LeaveBalanceService leaveBalanceService;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           UserRepository userRepository,
                           LeaveBalanceService leaveBalanceService) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.leaveBalanceService = leaveBalanceService;
    }

    @Transactional
    public Employee createEmployee(Employee employee) {
        normalizeAndValidate(employee, null);
        Employee saved = employeeRepository.save(employee);
        try {
            leaveBalanceService.initializeBalances(saved.getId());
        } catch (Exception ignored) {}
        return saved;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));
    }

    @Transactional
    public Employee updateEmployee(Long id, Employee d) {
        Employee e = getEmployeeById(id);
        copy(e, d);
        normalizeAndValidate(e, id);
        return employeeRepository.save(e);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new IllegalArgumentException("Employee not found with id: " + id);
        }
        userRepository.findAllByEmployeeId(id).forEach(u -> {
            u.setEmployeeId(null);
            userRepository.save(u);
        });
        employeeRepository.deleteById(id);
    }

    private void copy(Employee e, Employee d) {
        e.setFirstName(d.getFirstName());
        e.setLastName(d.getLastName());
        e.setEmail(d.getEmail());
        e.setPhone(d.getPhone());
        e.setDateOfBirth(d.getDateOfBirth());
        e.setGender(d.getGender());
        e.setDepartment(d.getDepartment());
        e.setJobTitle(d.getJobTitle());
        e.setEmploymentType(d.getEmploymentType());
        e.setJoiningDate(d.getJoiningDate());
        e.setSalary(d.getSalary());
        e.setAddress(d.getAddress());
        e.setCity(d.getCity());
        e.setState(d.getState());
        e.setPostalCode(d.getPostalCode());
        e.setEmergencyContactName(d.getEmergencyContactName());
        e.setEmergencyContactPhone(d.getEmergencyContactPhone());
        e.setPhotoUrl(d.getPhotoUrl());
        e.setActive(d.isActive());
    }

    private void normalizeAndValidate(Employee employee, Long id) {
        employee.setFirstName(trimRequired(employee.getFirstName(), "First name"));
        employee.setLastName(trimRequired(employee.getLastName(), "Last name"));
        employee.setEmail(trimRequired(employee.getEmail(), "Email").toLowerCase());
        employee.setJobTitle(trimRequired(employee.getJobTitle(), "Job title"));
        employee.setDepartment(trimRequired(employee.getDepartment(), "Department"));

        if (!employee.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Email address is invalid");
        }
        if (id == null && employeeRepository.existsByEmailIgnoreCase(employee.getEmail())) {
            throw new IllegalArgumentException("An employee with this email already exists");
        }
        if (id != null && employeeRepository.existsByEmailIgnoreCaseAndIdNot(employee.getEmail(), id)) {
            throw new IllegalArgumentException("An employee with this email already exists");
        }

        var department = departmentRepository.findByNameIgnoreCase(employee.getDepartment())
                .orElseThrow(() -> new IllegalArgumentException("Department does not exist: " + employee.getDepartment()));
        if (!department.isActive()) {
            throw new IllegalArgumentException("Employee cannot be assigned to an inactive department");
        }

        employee.setPhone(trimNullable(employee.getPhone()));
        employee.setGender(trimNullable(employee.getGender()));
        employee.setEmploymentType(trimNullable(employee.getEmploymentType()));
        employee.setAddress(trimNullable(employee.getAddress()));
        employee.setCity(trimNullable(employee.getCity()));
        employee.setState(trimNullable(employee.getState()));
        employee.setPostalCode(trimNullable(employee.getPostalCode()));
        employee.setEmergencyContactName(trimNullable(employee.getEmergencyContactName()));
        employee.setEmergencyContactPhone(trimNullable(employee.getEmergencyContactPhone()));
        employee.setPhotoUrl(trimNullable(employee.getPhotoUrl()));
        if (employee.getSalary() != null && employee.getSalary().signum() < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
    }

    private String trimRequired(String value, String field) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return trimmed;
    }

    private String trimNullable(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
