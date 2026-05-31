package com.rice.trade.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rice.trade.config.AccessControlProperties;
import com.rice.trade.config.AdminDeviceProperties;
import com.rice.trade.config.ApiCryptoProperties;
import com.rice.trade.config.DdosProtectionProperties;
import com.rice.trade.config.TokenSessionProperties;
import com.rice.trade.dto.ApiResponse;
import com.rice.trade.mapper.AuditLogMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
@EnableConfigurationProperties({
        AccessControlProperties.class,
        AdminDeviceProperties.class,
        ApiCryptoProperties.class,
        DdosProtectionProperties.class,
        TokenSessionProperties.class
})
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuditLogMapper auditLogMapper,
            ObjectMapper objectMapper,
            AccessControlProperties accessControlProperties,
            AdminDeviceProperties adminDeviceProperties,
            ApiCryptoProperties apiCryptoProperties,
            DdosProtectionProperties ddosProtectionProperties,
            TokenSessionProperties tokenSessionProperties,
            TokenSessionService tokenSessionService,
            StringRedisTemplate redisTemplate
    ) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> writeError(
                                response,
                                objectMapper,
                                HttpStatus.UNAUTHORIZED,
                                "请先登录"
                        ))
                        .accessDeniedHandler((request, response, accessDeniedException) -> writeError(
                                response,
                                objectMapper,
                                HttpStatus.FORBIDDEN,
                                "没有访问权限"
                        ))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/system/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/audit-logs").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .addFilterBefore(new DdosProtectionFilter(objectMapper, ddosProtectionProperties, redisTemplate), AuthorizationFilter.class)
                .addFilterBefore(new ClientAccessFilter(objectMapper, accessControlProperties), AuthorizationFilter.class)
                .addFilterBefore(new ApiCryptoFilter(objectMapper, apiCryptoProperties), AuthorizationFilter.class)
                .addFilterAfter(new AdminDeviceFilter(objectMapper, adminDeviceProperties), BasicAuthenticationFilter.class)
                .addFilterAfter(new TokenSessionFilter(objectMapper, tokenSessionProperties, tokenSessionService), BasicAuthenticationFilter.class)
                .addFilterAfter(new AdminOperationLogFilter(auditLogMapper, objectMapper), AuthorizationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void writeError(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            HttpStatus status,
            String message
    ) throws java.io.IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.failure(status.value(), message, null));
    }
}
