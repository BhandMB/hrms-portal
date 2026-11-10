package com.hrms.portal.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        // Disables XOR-masking to allow raw cookie token comparison for SPAs and JavaScript
        requestHandler.setCsrfRequestAttributeName(null);

        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfRepository)
                .csrfTokenRequestHandler(requestHandler)
                .ignoringRequestMatchers("/login")
            )
            .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/favicon.ico", "/favicon.svg", "/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .requestMatchers("/employee-form").hasAnyRole("ADMIN", "HR")
                .requestMatchers("/user-management", "/audit-log").hasRole("ADMIN")
                .requestMatchers("/api/users", "/api/users/**", "/api/v1/users", "/api/v1/users/**").hasRole("ADMIN")
                .requestMatchers("/api/audit-logs", "/api/audit-logs/**", "/api/v1/audit-logs", "/api/v1/audit-logs/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers("/api/employees", "/api/employees/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers("/api/departments", "/api/departments/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers("/api/attendance", "/api/attendance/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers("/api/leaves", "/api/leaves/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers("/api/payroll", "/api/payroll/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers("/api/reports", "/api/reports/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers("/api/announcements", "/api/announcements/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers("/api/holidays", "/api/holidays/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers("/api/documents", "/api/documents/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers("/api/performance-reviews", "/api/performance-reviews/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers("/api/leave-balances", "/api/leave-balances/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers("/api/dashboard/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers("/api/settings/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers("/api/search").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers("/", "/dashboard", "/employees", "/employee-profile", "/departments",
                        "/attendance", "/leave", "/payroll", "/reports", "/announcements", "/holidays",
                        "/performance", "/settings", "/api/me").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"));

        return http.build();
    }

    private static final class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                // Render the token value to cookie by causing the deferred token to be loaded
                csrfToken.getToken();
            }
            filterChain.doFilter(request, response);
        }
    }
}
