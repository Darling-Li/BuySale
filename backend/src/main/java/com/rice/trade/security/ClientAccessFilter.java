package com.rice.trade.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rice.trade.config.AccessControlProperties;
import com.rice.trade.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public class ClientAccessFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final AccessControlProperties properties;
    private final List<CidrBlock> allowedCidrs;
    private final List<CidrBlock> trustedProxyCidrs;
    private final Set<String> allowedRegions;

    public ClientAccessFilter(ObjectMapper objectMapper, AccessControlProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.allowedCidrs = parseCidrs(properties.getAllowedCidrs());
        this.trustedProxyCidrs = parseCidrs(properties.getTrustedProxyCidrs());
        this.allowedRegions = cleanList(properties.getAllowedRegions()).stream()
                .map(ClientAccessFilter::normalize)
                .collect(Collectors.toSet());
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

        AccessDecision decision = accessDecision(request);
        if (!decision.permitted()) {
            reject(response, decision.reason());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private AccessDecision accessDecision(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr();
        boolean trustedProxy = matchesAny(trustedProxyCidrs, remoteAddress);
        String clientIp = trustedProxy
                ? firstHeaderValue(request, "X-Forwarded-For").map(value -> value.split(",")[0].trim()).orElse(remoteAddress)
                : remoteAddress;

        if (!allowedCidrs.isEmpty() && !matchesAny(allowedCidrs, clientIp)) {
            return AccessDecision.denied("IP_NOT_ALLOWED");
        }

        if (!allowedRegions.isEmpty()) {
            Optional<String> region = trustedProxy ? firstRegionHeader(request) : Optional.empty();
            if (region.isEmpty() || !allowedRegions.contains(normalize(region.get()))) {
                return AccessDecision.denied("REGION_NOT_ALLOWED");
            }
        }

        return AccessDecision.allow();
    }

    private Optional<String> firstRegionHeader(HttpServletRequest request) {
        return cleanList(properties.getRegionHeaders()).stream()
                .map(name -> firstHeaderValue(request, name))
                .flatMap(Optional::stream)
                .findFirst();
    }

    private Optional<String> firstHeaderValue(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(value.trim());
    }

    private boolean matchesProtectedPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        List<String> prefixes = cleanList(properties.getPathPrefixes());
        if (prefixes.isEmpty()) {
            return path.startsWith("/api/");
        }
        return prefixes.stream().anyMatch(path::startsWith);
    }

    private boolean isPreflight(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private void reject(HttpServletResponse response, String reason) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.failure(
                HttpStatus.FORBIDDEN.value(),
                "当前访问来源不在允许范围内",
                Map.of("reason", reason)
        ));
    }

    private boolean matchesAny(List<CidrBlock> cidrs, String address) {
        return cidrs.stream().anyMatch(cidr -> cidr.contains(address));
    }

    private List<CidrBlock> parseCidrs(List<String> values) {
        return cleanList(values).stream()
                .map(CidrBlock::parse)
                .toList();
    }

    private static List<String> cleanList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .toList();
    }

    private static String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private record AccessDecision(boolean permitted, String reason) {
        static AccessDecision allow() {
            return new AccessDecision(true, "");
        }

        static AccessDecision denied(String reason) {
            return new AccessDecision(false, reason);
        }
    }

    private record CidrBlock(BigInteger network, BigInteger mask, int bits, String source) {
        static CidrBlock parse(String value) {
            try {
                String[] parts = value.split("/", 2);
                InetAddress address = InetAddress.getByName(parts[0]);
                int bits = address.getAddress().length * 8;
                int prefixLength = parts.length == 2 ? Integer.parseInt(parts[1]) : bits;
                if (prefixLength < 0 || prefixLength > bits) {
                    throw new IllegalArgumentException("Invalid CIDR prefix length: " + value);
                }
                BigInteger mask = mask(bits, prefixLength);
                BigInteger network = toBigInteger(address).and(mask);
                return new CidrBlock(network, mask, bits, value);
            } catch (UnknownHostException | NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid CIDR value: " + value, ex);
            }
        }

        boolean contains(String value) {
            try {
                InetAddress address = InetAddress.getByName(value);
                if (address.getAddress().length * 8 != bits) {
                    return false;
                }
                return toBigInteger(address).and(mask).equals(network);
            } catch (UnknownHostException ex) {
                return false;
            }
        }

        private static BigInteger mask(int bits, int prefixLength) {
            if (prefixLength == 0) {
                return BigInteger.ZERO;
            }
            return BigInteger.ONE.shiftLeft(bits)
                    .subtract(BigInteger.ONE)
                    .shiftRight(bits - prefixLength)
                    .shiftLeft(bits - prefixLength);
        }

        private static BigInteger toBigInteger(InetAddress address) {
            return new BigInteger(1, address.getAddress());
        }
    }
}
