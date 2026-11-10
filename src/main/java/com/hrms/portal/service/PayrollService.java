package com.hrms.portal.service;

import com.hrms.portal.entity.Employee;
import com.hrms.portal.entity.Payslip;
import com.hrms.portal.repository.EmployeeRepository;
import com.hrms.portal.repository.PayslipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class PayrollService {

    private final PayslipRepository payslipRepository;
    private final EmployeeRepository employeeRepository;
    private final PdfPayslipGeneratorService pdfService;

    public PayrollService(PayslipRepository payslipRepository,
                          EmployeeRepository employeeRepository,
                          PdfPayslipGeneratorService pdfService) {
        this.payslipRepository = payslipRepository;
        this.employeeRepository = employeeRepository;
        this.pdfService = pdfService;
    }

    public List<Payslip> getAllPayslips() {
        return payslipRepository.findAllByOrderByYearDescMonthDescEmployeeNameAsc();
    }

    public List<Payslip> getPayslipsForEmployee(Long employeeId) {
        return payslipRepository.findByEmployeeIdOrderByYearDescMonthDesc(employeeId);
    }

    public List<Payslip> getPayslipsForPeriod(int year, int month) {
        return payslipRepository.findByYearAndMonthOrderByEmployeeNameAsc(year, month);
    }

    public Optional<Payslip> getPayslipById(Long id) {
        return payslipRepository.findById(id);
    }

    @Transactional
    public List<Payslip> runMonthlyPayroll(int year, int month, String triggeredBy) {
        List<Employee> activeEmployees = employeeRepository.findByActiveTrue();
        List<Payslip> generated = new ArrayList<>();

        for (Employee emp : activeEmployees) {
            Optional<Payslip> existing = payslipRepository.findByEmployeeIdAndYearAndMonth(emp.getId(), year, month);
            if (existing.isPresent()) {
                generated.add(existing.get());
                continue;
            }

            Payslip payslip = calculateAndBuildPayslip(emp, year, month);
            generated.add(payslipRepository.save(payslip));
        }

        return generated;
    }

    @Transactional
    public Payslip generateEmployeePayslip(Long employeeId, int year, int month) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));

        return payslipRepository.findByEmployeeIdAndYearAndMonth(employeeId, year, month)
                .orElseGet(() -> payslipRepository.save(calculateAndBuildPayslip(emp, year, month)));
    }

    @Transactional
    public Payslip markAsPaid(Long payslipId, String transactionRef) {
        Payslip p = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found with id: " + payslipId));

        p.setStatus("PAID");
        p.setPaymentDate(LocalDate.now());
        if (transactionRef != null && !transactionRef.isBlank()) {
            p.setTransactionReference(transactionRef);
        } else if (p.getTransactionReference() == null) {
            p.setTransactionReference("TXN-" + System.currentTimeMillis());
        }
        return payslipRepository.save(p);
    }

    public byte[] getPayslipPdf(Long payslipId) {
        Payslip p = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found with id: " + payslipId));
        return pdfService.generatePayslipPdf(p);
    }

    public Payslip calculateAndBuildPayslip(Employee e, int year, int month) {
        BigDecimal annual = e.getSalary() == null ? BigDecimal.ZERO : e.getSalary();
        BigDecimal monthlyGross = annual.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

        BigDecimal basic = monthlyGross.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal hra = monthlyGross.multiply(new BigDecimal("0.25")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal special = monthlyGross.subtract(basic).subtract(hra);
        if (special.compareTo(BigDecimal.ZERO) < 0) special = BigDecimal.ZERO;

        BigDecimal bonus = BigDecimal.ZERO;
        BigDecimal grossEarnings = basic.add(hra).add(special).add(bonus);

        // Deductions: PF (12% of basic), Professional Tax (₹200 if >15000), TDS/Income Tax (approx 5% if monthly > 50000)
        BigDecimal pf = basic.multiply(new BigDecimal("0.12")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pt = monthlyGross.compareTo(new BigDecimal("15000")) > 0 ? new BigDecimal("200.00") : BigDecimal.ZERO;
        BigDecimal tds = monthlyGross.compareTo(new BigDecimal("50000")) > 0
                ? monthlyGross.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal otherDeductions = BigDecimal.ZERO;
        BigDecimal totalDeductions = pf.add(pt).add(tds).add(otherDeductions);

        BigDecimal netSalary = grossEarnings.subtract(totalDeductions).setScale(2, RoundingMode.HALF_UP);

        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String period = monthName + " " + year;

        Payslip p = new Payslip();
        p.setEmployee(e);
        p.setEmployeeName(e.getFirstName() + " " + e.getLastName());
        p.setDepartment(e.getDepartment());
        p.setJobTitle(e.getJobTitle());
        p.setYear(year);
        p.setMonth(month);
        p.setPayPeriod(period);
        p.setPaymentDate(LocalDate.of(year, month, Math.min(28, LocalDate.of(year, month, 1).lengthOfMonth())));
        p.setBasicSalary(basic);
        p.setHra(hra);
        p.setSpecialAllowance(special);
        p.setBonus(bonus);
        p.setGrossEarnings(grossEarnings);
        p.setPfDeduction(pf);
        p.setProfessionalTax(pt);
        p.setIncomeTax(tds);
        p.setOtherDeductions(otherDeductions);
        p.setTotalDeductions(totalDeductions);
        p.setNetSalary(netSalary);
        p.setStatus("GENERATED");
        p.setPaymentMethod("DIRECT_DEPOSIT");
        p.setTransactionReference("HRMS-" + year + String.format("%02d", month) + "-" + e.getId());
        p.setRemarks("Standard monthly compensation package");

        return p;
    }
}
