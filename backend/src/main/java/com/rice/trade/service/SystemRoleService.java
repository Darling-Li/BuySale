package com.rice.trade.service;

import com.rice.trade.dto.SystemRoleRequest;
import com.rice.trade.dto.SystemRoleResponse;
import com.rice.trade.entity.SystemRole;
import com.rice.trade.exception.BusinessException;
import com.rice.trade.exception.ResourceNotFoundException;
import com.rice.trade.mapper.SystemRoleMapper;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemRoleService {

    private final SystemRoleMapper systemRoleMapper;

    public SystemRoleService(SystemRoleMapper systemRoleMapper) {
        this.systemRoleMapper = systemRoleMapper;
    }

    @Transactional(readOnly = true)
    public List<SystemRoleResponse> list() {
        return systemRoleMapper.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SystemRole requireByCode(String code) {
        SystemRole role = systemRoleMapper.findByCode(normalizeCode(code));
        if (role == null) {
            throw new ResourceNotFoundException("角色不存在：" + code);
        }
        return role;
    }

    @Transactional
    public SystemRoleResponse create(SystemRoleRequest request) {
        String code = normalizeCode(request.code());
        if (systemRoleMapper.findByCode(code) != null) {
            throw new BusinessException("角色编码已存在：" + code);
        }

        SystemRole role = new SystemRole();
        fill(role, request);
        systemRoleMapper.insert(role);
        return toResponse(systemRoleMapper.findById(role.getId()));
    }

    @Transactional
    public SystemRoleResponse update(Long id, SystemRoleRequest request) {
        SystemRole role = requireById(id);
        String code = normalizeCode(request.code());
        SystemRole existing = systemRoleMapper.findByCode(code);
        if (existing != null && !existing.getId().equals(id)) {
            throw new BusinessException("角色编码已存在：" + code);
        }

        fill(role, request);
        role.setId(id);
        systemRoleMapper.update(role);
        return toResponse(requireById(id));
    }

    @Transactional(readOnly = true)
    public SystemRole requireById(Long id) {
        SystemRole role = systemRoleMapper.findById(id);
        if (role == null) {
            throw new ResourceNotFoundException("角色不存在：" + id);
        }
        return role;
    }

    public SystemRoleResponse toResponse(SystemRole role) {
        return new SystemRoleResponse(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.getEnabled(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    private void fill(SystemRole role, SystemRoleRequest request) {
        role.setCode(normalizeCode(request.code()));
        role.setName(trim(request.name()));
        role.setDescription(trim(request.description()));
        role.setEnabled(request.enabled() == null || request.enabled());
    }

    private String normalizeCode(String code) {
        return trim(code).toUpperCase(Locale.ROOT);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
