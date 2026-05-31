package com.rice.trade.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rice.trade.config.DdosProtectionProperties;
import com.rice.trade.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public class DdosProtectionFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(DdosProtectionFilter.class);

    private final ObjectMapper objectMapper;
    private final DdosProtectionProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final RedisBloomFilter bloomFilter;

    public DdosProtectionFilter(
            ObjectMapper objectMapper,
            DdosProtectionProperties properties,
            StringRedisTemplate redisTemplate
    ) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.redisTemplate = redisTemplate;
        this.bloomFilter = new RedisBloomFilter(redisTemplate, properties);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!properties.isEnabled() || !matchesProtectedPath(request) || isPreflight(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = clientIp(request);
        try {
            if (bloomFilter.mightContain(clientIp)) {
                reject(response, clientIp, "BLOOM_BLOCKED");
                return;
            }

            long count = increaseWindowCount(clientIp);
            if (count > Math.max(1L, properties.getMaxRequestsPerWindow())) {
                bloomFilter.add(clientIp);
                reject(response, clientIp, "RATE_LIMIT_BLOCKED");
                return;
            }
        } catch (RuntimeException ex) {
            log.warn("Redis DDoS protection failed, request is allowed temporarily", ex);
        }

        filterChain.doFilter(request, response);
    }

    private long increaseWindowCount(String clientIp) {
        String key = properties.getCounterPrefix() + clientIp;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(Math.max(1L, properties.getWindowSeconds())));
        }
        return count == null ? 0L : count;
    }

    private boolean matchesProtectedPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        List<String> prefixes = cleanList(properties.getPathPrefixes());
        if (prefixes.isEmpty()) {
            return path.startsWith("/api/");
        }
        return prefixes.stream().anyMatch(path::startsWith);
    }

    private String clientIp(HttpServletRequest request) {
        for (String header : cleanList(properties.getClientIpHeaders())) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank()) {
                return value.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private boolean isPreflight(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private void reject(HttpServletResponse response, String clientIp, String reason) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.failure(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "请求过于频繁，请稍后再试",
                Map.of("clientIp", clientIp, "reason", reason)
        ));
    }

    private List<String> cleanList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .toList();
    }
}
