package com.hrms.portal;
import com.hrms.portal.entity.*;
import com.hrms.portal.repository.*;
import com.hrms.portal.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class HrmsPortalApplicationTests {

    @Autowired DepartmentRepository departmentRepository;
    @Autowired EmployeeRepository employeeRepository;
    @Autowired DepartmentService departmentService;
    @Autowired EmployeeService employeeService;
    @Autowired AttendanceRepository attendanceRepository;
    @Autowired AttendanceService attendanceService;
    @Autowired LeaveRequestRepository leaveRequestRepository;
    @Autowired LeaveService leaveService;
    @Autowired UserRepository userRepository;
    @Autowired PayslipRepository payslipRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired HolidayRepository holidayRepository;
    @Autowired HolidayService holidayService;
    @Autowired AnnouncementRepository announcementRepository;
    @Autowired AnnouncementService announcementService;
    @Autowired LeaveBalanceRepository leaveBalanceRepository;
    @Autowired LeaveBalanceService leaveBalanceService;
    @Autowired AuditLogRepository auditLogRepository;
    @Autowired AuditService auditService;
    @Autowired PerformanceReviewRepository performanceReviewRepository;
    @Autowired PerformanceReviewService performanceReviewService;

    @BeforeEach
    void cleanAndSeed() {
        auditLogRepository.deleteAll();
        performanceReviewRepository.deleteAll();
        leaveBalanceRepository.deleteAll();
        announcementRepository.deleteAll();
        holidayRepository.deleteAll();
        payslipRepository.deleteAll();
        attendanceRepository.deleteAll();
        leaveRequestRepository.deleteAll();
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.save(new Department("Engineering", "Engineering team", "Head"));
    }

    @Test
    void contextLoads() {
        assertThat(departmentRepository.count()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void employeeCanOnlyUseExistingDepartment() {
        Employee employee = employee("a@example.com", "Engineering");
        assertThat(employeeRepository).isNotNull();
        assertThatThrownBy(() -> {
            employee.setDepartment("Unknown");
            new EmployeeService(employeeRepository, departmentRepository, userRepository, leaveBalanceService).createEmployee(employee);
        }).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Department does not exist");
    }

    @Test
    void departmentRenameUpdatesEmployeeAssignments() {
        Department d = departmentRepository.findByNameIgnoreCase("Engineering").orElseThrow();
        employeeRepository.save(employee("a@example.com", "Engineering"));
        departmentService.updateDepartment(d.getId(), new Department("Software Engineering", "Updated", "New Head"));
        assertThat(employeeRepository.findAll()).singleElement().extracting(Employee::getDepartment).isEqualTo("Software Engineering");
    }

    @Test
    void assignedDepartmentCannotBeDeleted() {
        Department d = departmentRepository.findByNameIgnoreCase("Engineering").orElseThrow();
        employeeRepository.save(employee("a@example.com", "Engineering"));
        assertThatThrownBy(() -> departmentService.deleteDepartment(d.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("assigned employees");
    }

    @Test
    void seededPasswordEncoderRoundTripWorks() {
        String raw = "Admin@123";
        String encoded = passwordEncoder.encode(raw);
        assertThat(passwordEncoder.matches(raw, encoded)).isTrue();
    }

    @Test
    void employeeDeletionCleansUpLinkedUserReference() {
        Employee saved = employeeRepository.save(employee("emp@example.com", "Engineering"));
        User user = new User("empuser", passwordEncoder.encode("Secret@123"), "EMPLOYEE");
        user.setEmployeeId(saved.getId());
        userRepository.save(user);

        assertThat(userRepository.findByEmployeeId(saved.getId())).isPresent();

        employeeService.deleteEmployee(saved.getId());

        assertThat(employeeRepository.existsById(saved.getId())).isFalse();
        User updatedUser = userRepository.findByUsername("empuser").orElseThrow();
        assertThat(updatedUser.getEmployeeId()).isNull();
    }

    @Test
    void attendanceClockInAndClockOutFlow() {
        Employee emp = employeeRepository.save(employee("att@example.com", "Engineering"));

        Attendance in = attendanceService.clockIn(emp.getId());
        assertThat(in.getStatus()).isEqualTo("PRESENT");
        assertThat(in.getCheckInTime()).isNotNull();

        Attendance out = attendanceService.clockOut(emp.getId());
        assertThat(out.getCheckOutTime()).isNotNull();
        assertThat(out.getWorkHours()).isNotNull();
    }

    @Test
    void leaveApplicationAndApprovalFlow() {
        Employee emp = employeeRepository.save(employee("leave@example.com", "Engineering"));

        LeaveRequest req = new LeaveRequest(emp.getId(), "Test User", "Engineering", "Casual Leave",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 3, "Vacation");
        LeaveRequest saved = leaveService.applyLeave(req);
        assertThat(saved.getStatus()).isEqualTo("PENDING");

        LeaveRequest approved = leaveService.approveLeave(saved.getId(), "admin", "Approved");
        assertThat(approved.getStatus()).isEqualTo("APPROVED");

        // Verify attendance record created as ON_LEAVE
        Attendance att = attendanceRepository.findByEmployeeIdAndDate(emp.getId(), LocalDate.now().plusDays(1)).orElseThrow();
        assertThat(att.getStatus()).isEqualTo("ON_LEAVE");
    }

    @Test
    void holidayManagementFlow() {
        Holiday h = new Holiday("Independence Day", LocalDate.of(2026, 8, 15), "PUBLIC", "National Holiday");
        Holiday saved = holidayService.create(h);
        assertThat(saved.getId()).isNotNull();
        assertThat(holidayService.getAllHolidays()).hasSize(1);
        assertThat(holidayService.getHolidaysForYear(2026)).hasSize(1);

        saved.setDescription("Updated National Holiday");
        Holiday updated = holidayService.update(saved.getId(), saved);
        assertThat(updated.getDescription()).isEqualTo("Updated National Holiday");

        holidayService.delete(saved.getId());
        assertThat(holidayService.getAllHolidays()).isEmpty();
    }

    @Test
    void announcementManagementFlow() {
        Announcement a = new Announcement("All Hands Meeting", "Q4 Town hall on Friday at 3PM", "HIGH", "GENERAL", "admin");
        a.setPinned(true);
        Announcement saved = announcementService.create(a, "admin");
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPostedBy()).isEqualTo("admin");
        assertThat(announcementService.getActiveAnnouncements()).hasSize(1);

        saved.setPriority("URGENT");
        announcementService.update(saved.getId(), saved);
        Announcement fetched = announcementService.getById(saved.getId());
        assertThat(fetched.getPriority()).isEqualTo("URGENT");

        announcementService.delete(saved.getId());
        assertThat(announcementService.getAllAnnouncements()).isEmpty();
    }

    @Test
    void leaveBalanceInitializationAndDeductionFlow() {
        Employee emp = employeeRepository.save(employee("balance@example.com", "Engineering"));
        leaveBalanceService.initializeBalances(emp.getId());

        var balances = leaveBalanceService.getBalancesForEmployee(emp.getId());
        assertThat(balances).isNotEmpty();
        assertThat(leaveBalanceService.hasBalance(emp.getId(), "Casual Leave", 5)).isTrue();

        leaveBalanceService.deductLeave(emp.getId(), "Casual Leave", 3);
        var updated = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(emp.getId(), "Casual Leave", LocalDate.now().getYear()).orElseThrow();
        assertThat(updated.getUsed()).isEqualTo(3);
        assertThat(updated.getRemaining()).isEqualTo(9);
    }

    @Test
    void auditLoggingFlow() {
        auditService.log("CREATE", "EMPLOYEE", 101L, "Created new employee John", "admin");
        var logs = auditService.getAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getAction()).isEqualTo("CREATE");
        assertThat(logs.get(0).getPerformedBy()).isEqualTo("admin");
    }

    @Test
    void performanceReviewLifecycleFlow() {
        Employee emp = employeeRepository.save(employee("review@example.com", "Engineering"));
        PerformanceReview review = new PerformanceReview();
        review.setEmployeeId(emp.getId());
        review.setReviewPeriod("2026-H1");
        review.setRating(5);
        review.setStrengths("Exceptional technical leadership");
        review.setAreasOfImprovement("Delegate more tasks");
        review.setGoals("Lead cloud modernization initiative");

        PerformanceReview created = performanceReviewService.create(review, "admin");
        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo("DRAFT");

        PerformanceReview submitted = performanceReviewService.submit(created.getId());
        assertThat(submitted.getStatus()).isEqualTo("SUBMITTED");

        PerformanceReview acknowledged = performanceReviewService.acknowledge(submitted.getId());
        assertThat(acknowledged.getStatus()).isEqualTo("ACKNOWLEDGED");
        assertThat(performanceReviewService.getByEmployee(emp.getId())).hasSize(1);
    }

    @Test
    void employeeCreationAutoInitializesLeaveBalances() {
        Employee e = employeeService.createEmployee(employee("autobalance@example.com", "Engineering"));
        var balances = leaveBalanceService.getBalancesForEmployee(e.getId());
        assertThat(balances).isNotEmpty();
        assertThat(balances).anyMatch(b -> b.getLeaveType().equals("Casual Leave") && b.getAllocatedDays() == 12);
    }

    @Test
    void leaveApprovalAutoDeductsFromLeaveBalance() {
        Employee emp = employeeService.createEmployee(employee("deducttest@example.com", "Engineering"));
        LeaveRequest req = new LeaveRequest(emp.getId(), emp.getFirstName() + " " + emp.getLastName(), "Engineering", "Casual Leave",
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(4), 3, "Personal travel");
        LeaveRequest applied = leaveService.applyLeave(req);
        leaveService.approveLeave(applied.getId(), "admin", "Approved by lead");

        var bal = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(emp.getId(), "Casual Leave", LocalDate.now().getYear()).orElseThrow();
        assertThat(bal.getUsed()).isEqualTo(3);
        assertThat(bal.getRemaining()).isEqualTo(9);
    }

    private Employee employee(String email, String department) {
        Employee e = new Employee();
        e.setFirstName("Test"); e.setLastName("User"); e.setEmail(email);
        e.setDepartment(department); e.setJobTitle("Developer");
        e.setEmploymentType("Full-time"); e.setSalary(new BigDecimal("100000"));
        return e;
    }
}
