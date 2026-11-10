package com.hrms.portal.config;

import com.hrms.portal.entity.*;
import com.hrms.portal.repository.*;
import com.hrms.portal.service.LeaveBalanceService;
import com.hrms.portal.service.PayrollService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner seedData(UserRepository users,
                               DepartmentRepository departments,
                               EmployeeRepository employees,
                               AttendanceRepository attendanceRepo,
                               LeaveRequestRepository leaveRepo,
                               AnnouncementRepository announcementRepo,
                               HolidayRepository holidayRepo,
                               PerformanceReviewRepository reviewRepo,
                               LeaveBalanceService leaveBalanceService,
                               AuditLogRepository auditLogRepo,
                               PayrollService payrollService,
                               PasswordEncoder encoder) {
        return args -> {
            seedDepartments(departments);
            List<Employee> seeded = seedEmployees(employees, departments);
            Employee demoEmployee = seeded.get(0);

            createUser(users, encoder, "admin", "Admin@123", "ADMIN", null);
            createUser(users, encoder, "hr", "Hr@12345", "HR", null);
            createUser(users, encoder, "employee", "Employee@123", "EMPLOYEE", demoEmployee.getId());

            seedAttendanceAndLeaves(attendanceRepo, leaveRepo, seeded);
            seedAnnouncements(announcementRepo);
            seedHolidays(holidayRepo);
            seedPerformanceReviews(reviewRepo, seeded);
            initializeLeaveBalances(leaveBalanceService, seeded);
            seedAuditLogs(auditLogRepo);

            // Seed 2 months of payroll payslips
            LocalDate now = LocalDate.now();
            payrollService.runMonthlyPayroll(now.getYear(), now.getMonthValue(), "INITIALIZER");
            payrollService.runMonthlyPayroll(now.minusMonths(1).getYear(), now.minusMonths(1).getMonthValue(), "INITIALIZER");

            log.info("✅ HRMS Portal data seeding completed — {} employees, {} departments",
                    employees.count(), departments.count());
        };
    }

    private void seedDepartments(DepartmentRepository repo) {
        List<Department> defaults = List.of(
                new Department("Engineering", "Software development and engineering operations", "Alex Mercer"),
                new Department("IT", "Internal infrastructure, DevOps and cloud systems", "David Kim"),
                new Department("Human Resources", "Talent acquisition, employee wellness and relations", "Sarah Connor"),
                new Department("Finance", "Financial planning, accounting and payroll", "Robert Paulson"),
                new Department("Sales", "Client acquisition and enterprise revenue generation", "Elena Rostova"),
                new Department("Marketing", "Brand management, digital presence and growth marketing", "Michael Scott"),
                new Department("Operations", "Business processes, facilities and quality assurance", "Priya Sharma"),
                new Department("Customer Support", "Client assistance, ticket resolution and customer happiness", "Jordan Bell")
        );

        defaults.forEach(dept -> {
            if (!repo.existsByNameIgnoreCase(dept.getName())) {
                repo.save(dept);
            }
        });
    }

    private List<Employee> seedEmployees(EmployeeRepository repo, DepartmentRepository departments) {
        if (repo.count() > 0) {
            return repo.findAll();
        }

        List<Employee> list = List.of(
                createEmp("Demo", "Employee", "employee@hrms.local", "9999999999", "Engineering", "Software Developer", "Full-time", LocalDate.now().minusMonths(8), new BigDecimal("650000"), true),
                createEmp("Priya", "Sharma", "priya.sharma@hrms.local", "9876543210", "Operations", "Operations Manager", "Full-time", LocalDate.now().minusYears(2), new BigDecimal("850000"), true),
                createEmp("Rahul", "Verma", "rahul.verma@hrms.local", "9812345678", "IT", "DevOps Engineer", "Full-time", LocalDate.now().minusMonths(14), new BigDecimal("750000"), true),
                createEmp("Anita", "Desai", "anita.desai@hrms.local", "9845123456", "Human Resources", "HR Generalist", "Full-time", LocalDate.now().minusMonths(11), new BigDecimal("580000"), true),
                createEmp("Rohan", "Mehta", "rohan.mehta@hrms.local", "9765432109", "Finance", "Senior Financial Analyst", "Full-time", LocalDate.now().minusYears(1), new BigDecimal("720000"), true),
                createEmp("Sneha", "Kapoor", "sneha.kapoor@hrms.local", "9654321098", "Marketing", "Brand Specialist", "Full-time", LocalDate.now().minusMonths(7), new BigDecimal("620000"), true),
                createEmp("Arjun", "Nair", "arjun.nair@hrms.local", "9543210987", "Sales", "Account Executive", "Full-time", LocalDate.now().minusMonths(5), new BigDecimal("600000"), true),
                createEmp("Maya", "Patel", "maya.patel@hrms.local", "9432109876", "Customer Support", "Support Lead", "Contract", LocalDate.now().minusMonths(3), new BigDecimal("480000"), true)
        );

        return repo.saveAll(list);
    }

    private Employee createEmp(String first, String last, String email, String phone,
                               String dept, String title, String type, LocalDate joined,
                               BigDecimal salary, boolean active) {
        Employee e = new Employee();
        e.setFirstName(first);
        e.setLastName(last);
        e.setEmail(email);
        e.setPhone(phone);
        e.setDepartment(dept);
        e.setJobTitle(title);
        e.setEmploymentType(type);
        e.setJoiningDate(joined);
        e.setSalary(salary);
        e.setActive(active);
        return e;
    }

    private void seedAttendanceAndLeaves(AttendanceRepository attendanceRepo,
                                         LeaveRequestRepository leaveRepo,
                                         List<Employee> emps) {
        LocalDate today = LocalDate.now();

        if (attendanceRepo.count() == 0 && !emps.isEmpty()) {
            for (int i = 0; i < emps.size(); i++) {
                Employee emp = emps.get(i);
                Attendance a = new Attendance();
                a.setEmployeeId(emp.getId());
                a.setEmployeeName(emp.getFirstName() + " " + emp.getLastName());
                a.setDate(today);

                if (i == 0) { // Demo employee
                    a.setCheckInTime("09:15 AM");
                    a.setStatus("PRESENT");
                    a.setWorkHours(8.0);
                } else if (i % 4 == 1) {
                    a.setCheckInTime("09:00 AM");
                    a.setStatus("PRESENT");
                    a.setWorkHours(8.0);
                } else if (i % 4 == 2) {
                    a.setCheckInTime("09:30 AM");
                    a.setStatus("PRESENT");
                    a.setWorkHours(8.0);
                } else if (i % 4 == 3) {
                    a.setStatus("ON_LEAVE");
                    a.setRemarks("Approved Annual Leave");
                    a.setWorkHours(0.0);
                }
                attendanceRepo.save(a);
            }
        }

        if (leaveRepo.count() == 0 && emps.size() >= 3) {
            Employee emp1 = emps.get(1);
            LeaveRequest req1 = new LeaveRequest(emp1.getId(), emp1.getFirstName() + " " + emp1.getLastName(),
                    emp1.getDepartment(), "Annual Leave", today.plusDays(2), today.plusDays(4), 3, "Family vacation");
            req1.setStatus("APPROVED");
            req1.setReviewedBy("hr");
            req1.setReviewerComment("Approved. Enjoy your time off!");
            leaveRepo.save(req1);

            Employee emp2 = emps.get(2);
            LeaveRequest req2 = new LeaveRequest(emp2.getId(), emp2.getFirstName() + " " + emp2.getLastName(),
                    emp2.getDepartment(), "Casual Leave", today.plusDays(5), today.plusDays(6), 2, "Personal errand");
            req2.setStatus("PENDING");
            leaveRepo.save(req2);

            Employee emp3 = emps.get(3);
            LeaveRequest req3 = new LeaveRequest(emp3.getId(), emp3.getFirstName() + " " + emp3.getLastName(),
                    emp3.getDepartment(), "Sick Leave", today.minusDays(3), today.minusDays(2), 2, "Feeling unwell");
            req3.setStatus("APPROVED");
            req3.setReviewedBy("admin");
            req3.setReviewerComment("Get well soon!");
            leaveRepo.save(req3);
        }
    }

    private void seedAnnouncements(AnnouncementRepository repo) {
        if (repo.count() > 0) return;

        repo.save(new Announcement(
                "Welcome to HRMS Portal v2.0",
                "We're excited to launch the new advanced HRMS Portal with performance reviews, holiday calendar, announcements, leave balance tracking, document management, and audit trails. Explore the sidebar to discover all features!",
                "HIGH", "GENERAL", "admin"));

        repo.save(new Announcement(
                "Annual Performance Review Cycle Begins",
                "The Q3 2026 performance review cycle has commenced. Managers are requested to complete reviews for their direct reports by the end of this month. Please use the Performance Reviews module.",
                "URGENT", "HR", "hr"));

        repo.save(new Announcement(
                "Office Diwali Celebration — October 20th",
                "Join us for the grand Diwali celebration at the office campus on October 20th. There will be rangoli competitions, cultural performances, and a festive lunch. Dress code: Ethnic wear.",
                "NORMAL", "EVENT", "hr"));

        Announcement policy = new Announcement(
                "Updated Work From Home Policy",
                "Effective November 1st, employees can work from home up to 2 days per week with manager approval. Please refer to the updated HR policy document for details and eligibility criteria.",
                "HIGH", "POLICY", "admin");
        policy.setPinned(true);
        repo.save(policy);

        repo.save(new Announcement(
                "IT Maintenance Window — October 12th",
                "Scheduled maintenance on internal systems will occur on October 12th from 10 PM to 2 AM IST. Email and VPN services may be temporarily unavailable during this period.",
                "NORMAL", "IT", "admin"));
    }

    private void seedHolidays(HolidayRepository repo) {
        if (repo.count() > 0) return;

        int year = LocalDate.now().getYear();
        repo.save(new Holiday("Republic Day", LocalDate.of(year, 1, 26), "PUBLIC", "National holiday celebrating the Constitution"));
        repo.save(new Holiday("Holi", LocalDate.of(year, 3, 14), "PUBLIC", "Festival of colors"));
        repo.save(new Holiday("Good Friday", LocalDate.of(year, 4, 18), "PUBLIC", "Christian observance"));
        repo.save(new Holiday("May Day", LocalDate.of(year, 5, 1), "PUBLIC", "International Workers' Day"));
        repo.save(new Holiday("Independence Day", LocalDate.of(year, 8, 15), "PUBLIC", "National holiday"));
        repo.save(new Holiday("Gandhi Jayanti", LocalDate.of(year, 10, 2), "PUBLIC", "Birth anniversary of Mahatma Gandhi"));
        repo.save(new Holiday("Dussehra", LocalDate.of(year, 10, 12), "PUBLIC", "Victory of good over evil"));
        repo.save(new Holiday("Diwali", LocalDate.of(year, 10, 20), "PUBLIC", "Festival of lights"));
        repo.save(new Holiday("Diwali Holiday", LocalDate.of(year, 10, 21), "COMPANY", "Company holiday for Diwali celebration"));
        repo.save(new Holiday("Christmas", LocalDate.of(year, 12, 25), "PUBLIC", "Christian holiday"));
        repo.save(new Holiday("Company Foundation Day", LocalDate.of(year, 6, 15), "COMPANY", "Annual company celebration"));
        repo.save(new Holiday("Eid ul-Fitr", LocalDate.of(year, 3, 31), "RESTRICTED", "End of Ramadan — optional holiday"));
    }

    private void seedPerformanceReviews(PerformanceReviewRepository repo, List<Employee> emps) {
        if (repo.count() > 0 || emps.size() < 3) return;

        PerformanceReview r1 = new PerformanceReview();
        r1.setEmployeeId(emps.get(0).getId());
        r1.setEmployeeName(emps.get(0).getFirstName() + " " + emps.get(0).getLastName());
        r1.setDepartment(emps.get(0).getDepartment());
        r1.setReviewPeriod("H1 2026");
        r1.setRating(4);
        r1.setStrengths("Strong technical skills, proactive learner, excellent code quality");
        r1.setAreasOfImprovement("Could improve documentation and mentoring junior developers");
        r1.setGoals("Lead a feature module independently, contribute to architecture decisions");
        r1.setReviewerComments("Demo has shown consistent improvement. Recommend for senior developer track.");
        r1.setReviewedBy("hr");
        r1.setStatus("SUBMITTED");
        repo.save(r1);

        PerformanceReview r2 = new PerformanceReview();
        r2.setEmployeeId(emps.get(1).getId());
        r2.setEmployeeName(emps.get(1).getFirstName() + " " + emps.get(1).getLastName());
        r2.setDepartment(emps.get(1).getDepartment());
        r2.setReviewPeriod("H1 2026");
        r2.setRating(5);
        r2.setStrengths("Exceptional leadership, streamlined operations processes, high team morale");
        r2.setAreasOfImprovement("Delegation — tends to take on too much personally");
        r2.setGoals("Implement automated reporting, reduce operational overhead by 20%");
        r2.setReviewerComments("Outstanding performer. Promotion recommendation submitted.");
        r2.setReviewedBy("admin");
        r2.setStatus("ACKNOWLEDGED");
        repo.save(r2);

        PerformanceReview r3 = new PerformanceReview();
        r3.setEmployeeId(emps.get(2).getId());
        r3.setEmployeeName(emps.get(2).getFirstName() + " " + emps.get(2).getLastName());
        r3.setDepartment(emps.get(2).getDepartment());
        r3.setReviewPeriod("H1 2026");
        r3.setRating(3);
        r3.setStrengths("Reliable infrastructure management, good incident response time");
        r3.setAreasOfImprovement("Needs to improve CI/CD pipeline documentation and cross-team communication");
        r3.setGoals("Achieve 99.9% uptime target, complete cloud migration certification");
        r3.setReviewerComments("Meets expectations. Encourage certification path for growth.");
        r3.setReviewedBy("hr");
        r3.setStatus("DRAFT");
        repo.save(r3);
    }

    private void initializeLeaveBalances(LeaveBalanceService service, List<Employee> emps) {
        emps.forEach(emp -> service.initializeBalances(emp.getId()));
    }

    private void seedAuditLogs(AuditLogRepository repo) {
        if (repo.count() > 0) return;

        repo.save(new AuditLog("CREATE", "SYSTEM", null, "HRMS Portal v2.0 initialized with seed data", "system"));
        repo.save(new AuditLog("CREATE", "USER", 1L, "Admin account created", "system"));
        repo.save(new AuditLog("CREATE", "USER", 2L, "HR account created", "system"));
        repo.save(new AuditLog("CREATE", "USER", 3L, "Employee demo account created", "system"));
    }

    private User createUser(UserRepository repo, PasswordEncoder encoder, String username, String password,
                            String role, Long employeeId) {
        User user = repo.findByUsername(username).orElseGet(() ->
                repo.save(new User(username, encoder.encode(password), role))
        );
        if (employeeId != null && "EMPLOYEE".equals(user.getRole()) && user.getEmployeeId() == null) {
            user.setEmployeeId(employeeId);
            repo.save(user);
        }
        return user;
    }
}
