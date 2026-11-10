package com.hrms.portal.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Provides transparent backward- and forward-compatibility for /api/v1/ endpoints
 * by forwarding requests targeting /api/v1/* to /api/*.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiVersioningFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/v1/")) {
            String forwardTarget = uri.replaceFirst("^/api/v1/", "/api/");
            request.getRequestDispatcher(forwardTarget).forward(request, response);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
