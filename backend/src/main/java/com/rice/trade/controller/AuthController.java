package com.rice.trade.controller;

import com.rice.trade.dto.AuthMeResponse;
import com.rice.trade.security.TokenSessionService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final TokenSessionService tokenSessionService;

    public AuthController(TokenSessionService tokenSessionService) {
        this.tokenSessionService = tokenSessionService;
    }

    @GetMapping("/me")
    public AuthMeResponse me(Authentication authentication, HttpServletRequest request) {
        tokenSessionService.cacheToken(request.getHeader("Authorization"), authentication);
        List<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .toList();
        return new AuthMeResponse(authentication.getName(), roles, roles.contains("ADMIN"));
    }
}
