package com.rice.trade.controller;

import com.rice.trade.dto.AuthMeResponse;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public AuthMeResponse me(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .toList();
        return new AuthMeResponse(authentication.getName(), roles, roles.contains("ADMIN"));
    }
}

