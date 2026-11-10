package com.hrms.portal.service;

import com.hrms.portal.entity.LeaveBalance;
import com.hrms.portal.repository.LeaveBalanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class LeaveBalanceService {

    private final LeaveBalanceRepository repository;

    public LeaveBalanceService(LeaveBalanceRepository repository) {
        this.repository = repository;
    }

    public List<LeaveBalance> getBalancesForEmployee(Long employeeId) {
        int year = LocalDate.now().getYear();
        return repository.findByEmployeeIdAndYear(employeeId, year);
    }

    public List<LeaveBalance> getBalancesForEmployeeAndYear(Long employeeId, int year) {
        return repository.findByEmployeeIdAndYear(employeeId, year);
    }

    @Transactional
    public void initializeBalances(Long employeeId) {
        int year = LocalDate.now().getYear();
        createIfAbsent(employeeId, "Casual Leave", 12, year);
        createIfAbsent(employeeId, "Sick Leave", 10, year);
        createIfAbsent(employeeId, "Annual Leave", 15, year);
        createIfAbsent(employeeId, "Unpaid Leave", 365, year);
    }

    private void createIfAbsent(Long employeeId, String leaveType, int allocated, int year) {
        if (repository.findByEmployeeIdAndLeaveTypeAndYear(employeeId, leaveType, year).isEmpty()) {
            repository.save(new LeaveBalance(employeeId, leaveType, allocated, year));
        }
    }

    @Transactional
    public void deductLeave(Long employeeId, String leaveType, int days) {
        int year = LocalDate.now().getYear();
        LeaveBalance balance = repository.findByEmployeeIdAndLeaveTypeAndYear(employeeId, leaveType, year)
                .orElse(null);
        if (balance != null) {
            balance.setUsed(balance.getUsed() + days);
            repository.save(balance);
        }
    }

    public boolean hasBalance(Long employeeId, String leaveType, int days) {
        int year = LocalDate.now().getYear();
        return repository.findByEmployeeIdAndLeaveTypeAndYear(employeeId, leaveType, year)
                .map(b -> b.getRemaining() >= days)
                .orElse(false);
    }
}
