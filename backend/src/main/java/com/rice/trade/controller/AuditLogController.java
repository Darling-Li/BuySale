package com.rice.trade.controller;

import com.rice.trade.dto.AuditLogResponse;
import com.rice.trade.entity.AuditLog;
import com.rice.trade.mapper.AuditLogMapper;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogMapper auditLogMapper;

    public AuditLogController(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @GetMapping
    public List<AuditLogResponse> recent() {
        return auditLogMapper.findRecent().stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getUsername(),
                log.getRoleName(),
                log.getMethod(),
                log.getPath(),
                log.getActionName(),
                log.getRequestParams(),
                log.getStatusCode(),
                log.getIpAddress(),
                log.getUserAgent(),
                log.getOccurredAt()
        );
    }
}
