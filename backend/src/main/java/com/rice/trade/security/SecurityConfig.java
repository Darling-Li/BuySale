package com.rice.trade.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rice.trade.mapper.AuditLogMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuditLogMapper auditLogMapper,
            ObjectMapper objectMapper
    ) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/audit-logs").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .addFilterAfter(new AdminOperationLogFilter(auditLogMapper, objectMapper), AuthorizationFilter.class)
                .build();
    }

    @Bean
    UserDetailsService userDetailsService(
            @Value("${app.security.admin.username}") String adminUsername,
            @Value("${app.security.admin.password}") String adminPassword,
            @Value("${app.security.viewer.username}") String viewerUsername,
            @Value("${app.security.viewer.password}") String viewerPassword,
            PasswordEncoder passwordEncoder
    ) {
        return new InMemoryUserDetailsManager(
                User.withUsername(adminUsername)
                        .password(passwordEncoder.encode(adminPassword))
                        .roles("ADMIN")
                        .build(),
                User.withUsername(viewerUsername)
                        .password(passwordEncoder.encode(viewerPassword))
                        .roles("USER")
                        .build()
        );
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
