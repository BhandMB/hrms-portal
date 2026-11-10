package com.hrms.portal.service;

import com.hrms.portal.entity.Department;
import com.hrms.portal.entity.Employee;
import com.hrms.portal.repository.DepartmentRepository;
import com.hrms.portal.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository repository;
    private final EmployeeRepository employeeRepository;

    public DepartmentService(DepartmentRepository repository, EmployeeRepository employeeRepository) {
        this.repository = repository;
        this.employeeRepository = employeeRepository;
    }

    public List<Department> getAllDepartments() {
        return repository.findAll().stream()
                .sorted(java.util.Comparator.comparing(Department::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Department getDepartmentById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + id));
    }

    @Transactional
    public Department createDepartment(Department department) {
        String name = normalizeName(department.getName());
        if (repository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Department already exists: " + name);
        }
        department.setName(name);
        department.setDescription(trimNullable(department.getDescription()));
        department.setDepartmentHead(trimNullable(department.getDepartmentHead()));
        return repository.save(department);
    }

    @Transactional
    public Department updateDepartment(Long id, Department updated) {
        Department department = getDepartmentById(id);
        String oldName = department.getName();
        String newName = normalizeName(updated.getName());
        repository.findByNameIgnoreCase(newName).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Department already exists: " + newName);
            }
        });

        department.setName(newName);
        department.setDescription(trimNullable(updated.getDescription()));
        department.setDepartmentHead(trimNullable(updated.getDepartmentHead()));
        department.setActive(updated.isActive());

        if (!oldName.equalsIgnoreCase(newName)) {
            List<Employee> employees = employeeRepository.findAll().stream()
                    .filter(e -> e.getDepartment() != null && e.getDepartment().equalsIgnoreCase(oldName))
                    .toList();
            employees.forEach(e -> e.setDepartment(newName));
            employeeRepository.saveAll(employees);
        }
        return repository.save(department);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = getDepartmentById(id);
        if (employeeRepository.existsByDepartmentIgnoreCase(department.getName())) {
            throw new IllegalStateException("Cannot delete a department that has assigned employees. Reassign employees first.");
        }
        repository.delete(department);
    }

    private String normalizeName(String value) {
        String name = value == null ? "" : value.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Department name is required");
        }
        return name;
    }

    private String trimNullable(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
