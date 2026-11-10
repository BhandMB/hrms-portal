package com.hrms.portal.controller;

import com.hrms.portal.entity.Employee;
import com.hrms.portal.entity.Payslip;
import com.hrms.portal.entity.User;
import com.hrms.portal.repository.UserRepository;
import com.hrms.portal.service.AuditService;
import com.hrms.portal.service.EmployeeService;
import com.hrms.portal.service.PayrollService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PayrollReportsController {

    private final EmployeeService employeeService;
    private final UserRepository userRepository;
    private final PayrollService payrollService;
    private final AuditService auditService;

    public PayrollReportsController(EmployeeService employeeService,
                                  UserRepository userRepository,
                                  PayrollService payrollService,
                                  AuditService auditService) {
        this.employeeService = employeeService;
        this.userRepository = userRepository;
        this.payrollService = payrollService;
        this.auditService = auditService;
    }

    @GetMapping("/payroll")
    public String payrollPage() {
        return "payroll";
    }

    @GetMapping("/reports")
    public String reportsPage() {
        return "reports";
    }

    @GetMapping("/api/payroll")
    @ResponseBody
    public List<Payslip> getPayroll(Authentication auth,
                                   @RequestParam(required = false) Integer year,
                                   @RequestParam(required = false) Integer month) {
        boolean isEmployeeOnly = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        if (isEmployeeOnly) {
            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (user.getEmployeeId() == null) {
                return List.of();
            }
            return payrollService.getPayslipsForEmployee(user.getEmployeeId());
        }

        if (year != null && month != null) {
            return payrollService.getPayslipsForPeriod(year, month);
        }

        List<Payslip> all = payrollService.getAllPayslips();
        // If empty, auto-generate current month to give immediate live data
        if (all.isEmpty()) {
            LocalDate now = LocalDate.now();
            all = payrollService.runMonthlyPayroll(now.getYear(), now.getMonthValue(), "SYSTEM");
        }
        return all;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/api/payroll/run")
    @ResponseBody
    public ResponseEntity<List<Payslip>> runPayroll(@RequestBody Map<String, Integer> payload, Authentication auth) {
        int year = payload.getOrDefault("year", LocalDate.now().getYear());
        int month = payload.getOrDefault("month", LocalDate.now().getMonthValue());

        List<Payslip> generated = payrollService.runMonthlyPayroll(year, month, auth.getName());
        auditService.log("GENERATE", "PAYROLL", null,
                "Executed payroll run for " + month + "/" + year + " (" + generated.size() + " payslips generated)",
                auth.getName());

        return ResponseEntity.ok(generated);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/api/payroll/payslips/{id}/pay")
    @ResponseBody
    public ResponseEntity<Payslip> disburseSalary(@PathVariable Long id,
                                                  @RequestBody(required = false) Map<String, String> body,
                                                  Authentication auth) {
        String ref = body != null ? body.get("transactionReference") : null;
        Payslip p = payrollService.markAsPaid(id, ref);
        auditService.log("UPDATE", "PAYROLL", id,
                "Marked payslip #" + id + " as PAID for " + p.getEmployeeName(), auth.getName());
        return ResponseEntity.ok(p);
    }

    @GetMapping(value = "/api/payroll/payslips/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPayslipPdf(@PathVariable Long id, Authentication auth) {
        Payslip p = payrollService.getPayslipById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found: " + id));

        // Check employee access boundary
        boolean isEmployeeOnly = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
        if (isEmployeeOnly) {
            User user = userRepository.findByUsername(auth.getName()).orElseThrow();
            if (p.getEmployee() == null || !p.getEmployee().getId().equals(user.getEmployeeId())) {
                throw new org.springframework.security.access.AccessDeniedException("Unauthorized to view other employee's payslip");
            }
        }

        byte[] pdfBytes = payrollService.getPayslipPdf(id);
        String filename = "Payslip-" + p.getPayPeriod().replace(" ", "-") + "-" + p.getEmployeeName().replace(" ", "_") + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .body(pdfBytes);
    }

    @GetMapping("/api/reports/summary")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getReportsSummary() {
        List<Employee> all = employeeService.getAllEmployees();

        Map<String, Long> byDept = all.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getDepartment() == null ? "Unassigned" : e.getDepartment(),
                        Collectors.counting()));

        Map<String, Long> byType = all.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getEmploymentType() == null ? "Full-time" : e.getEmploymentType(),
                        Collectors.counting()));

        long activeCount = all.stream().filter(Employee::isActive).count();
        long inactiveCount = all.size() - activeCount;

        BigDecimal totalAnnualSalary = all.stream()
                .map(e -> e.getSalary() == null ? BigDecimal.ZERO : e.getSalary())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgAnnualSalary = all.isEmpty() ? BigDecimal.ZERO :
                totalAnnualSalary.divide(BigDecimal.valueOf(all.size()), 2, RoundingMode.HALF_UP);

        Map<String, BigDecimal> deptExpense = all.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getDepartment() == null ? "Unassigned" : e.getDepartment(),
                        Collectors.reducing(BigDecimal.ZERO,
                                e -> e.getSalary() == null ? BigDecimal.ZERO : e.getSalary(),
                                BigDecimal::add)));

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalEmployees", all.size());
        summary.put("activeCount", activeCount);
        summary.put("inactiveCount", inactiveCount);
        summary.put("totalAnnualSalary", totalAnnualSalary);
        summary.put("monthlyPayroll", totalAnnualSalary.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));
        summary.put("avgAnnualSalary", avgAnnualSalary);
        summary.put("byDepartment", byDept);
        summary.put("byEmploymentType", byType);
        summary.put("departmentExpense", deptExpense);

        return ResponseEntity.ok(summary);
    }
}
