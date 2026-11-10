package com.hrms.portal.repository;

import com.hrms.portal.entity.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, Long> {

    List<Payslip> findByEmployeeIdOrderByYearDescMonthDesc(Long employeeId);

    List<Payslip> findByYearAndMonthOrderByEmployeeNameAsc(int year, int month);

    Optional<Payslip> findByEmployeeIdAndYearAndMonth(Long employeeId, int year, int month);

    boolean existsByEmployeeIdAndYearAndMonth(Long employeeId, int year, int month);

    List<Payslip> findAllByOrderByYearDescMonthDescEmployeeNameAsc();
}
