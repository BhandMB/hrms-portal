package com.hrms.portal.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;

@Controller
public class AuthController {
    @GetMapping("/login")
    public String login(Model model) { return "login"; }

    @org.springframework.web.bind.annotation.RequestMapping("/access-denied")
    public String denied() { return "access-denied"; }

    @GetMapping("/api/me")
    @ResponseBody
    public Map<String, Object> me(Authentication auth) {
        String role = auth.getAuthorities().stream().findFirst().map(a -> a.getAuthority().replace("ROLE_", "")).orElse("USER");
        return Map.of("username", auth.getName(), "role", role);
    }
}
