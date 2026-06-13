package com.rice.trade.service;

import com.rice.trade.dto.SystemUserRequest;
import com.rice.trade.dto.SystemUserResponse;
import com.rice.trade.entity.SystemUser;
import com.rice.trade.exception.BusinessException;
import com.rice.trade.exception.ResourceNotFoundException;
import com.rice.trade.mapper.SystemUserMapper;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemUserService {

    private static final Set<String> BUILTIN_USERNAMES = Set.of("admin", "user");

    private final SystemUserMapper systemUserMapper;
    private final SystemRoleService systemRoleService;
    private final PasswordEncoder passwordEncoder;

    public SystemUserService(
            SystemUserMapper systemUserMapper,
            SystemRoleService systemRoleService,
            PasswordEncoder passwordEncoder
    ) {
        this.systemUserMapper = systemUserMapper;
        this.systemRoleService = systemRoleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<SystemUserResponse> list() {
        return systemUserMapper.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SystemUserResponse create(SystemUserRequest request) {
        String username = normalizeUsername(request.username());
        if (systemUserMapper.findByUsername(username) != null) {
            throw new BusinessException("用户名已存在：" + username);
        }
        if (isBlank(request.password())) {
            throw new BusinessException("创建用户时密码不能为空");
        }

        SystemUser user = new SystemUser();
        fillBasic(user, request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        systemUserMapper.insert(user);
        replaceRoles(user.getId(), request.roleCodes());
        return toResponse(requireById(user.getId()));
    }

    @Transactional
    public SystemUserResponse update(Long id, SystemUserRequest request) {
        SystemUser user = requireById(id);
        String username = normalizeUsername(request.username());
        SystemUser existing = systemUserMapper.findByUsername(username);
        if (existing != null && !existing.getId().equals(id)) {
            throw new BusinessException("用户名已存在：" + username);
        }

        fillBasic(user, request);
        user.setId(id);
        systemUserMapper.updateBasic(user);
        if (!isBlank(request.password())) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
            systemUserMapper.updatePassword(user);
        }
        replaceRoles(id, request.roleCodes());
        return toResponse(requireById(id));
    }

    @Transactional
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的用户");
        }
        List<Long> distinctIds = ids.stream().distinct().toList();
        for (Long id : distinctIds) {
            SystemUser user = requireById(id);
            if (BUILTIN_USERNAMES.contains(user.getUsername())) {
                throw new BusinessException("系统默认用户不能删除：" + user.getUsername());
            }
        }
        systemUserMapper.deleteRolesByUserIds(distinctIds);
        systemUserMapper.deleteByIds(distinctIds);
    }

    @Transactional(readOnly = true)
    public SystemUser requireById(Long id) {
        SystemUser user = systemUserMapper.findById(id);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在：" + id);
        }
        return user;
    }

    public SystemUserResponse toResponse(SystemUser user) {
        return new SystemUserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEnabled(),
                systemUserMapper.findRoleCodesByUserId(user.getId()),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private void fillBasic(SystemUser user, SystemUserRequest request) {
        user.setUsername(normalizeUsername(request.username()));
        user.setDisplayName(trim(request.displayName()));
        user.setEnabled(request.enabled() == null || request.enabled());
    }

    private void replaceRoles(Long userId, List<String> roleCodes) {
        List<String> normalizedCodes = roleCodes.stream()
                .map(this::normalizeRoleCode)
                .distinct()
                .toList();
        normalizedCodes.forEach(systemRoleService::requireByCode);

        systemUserMapper.deleteRoles(userId);
        normalizedCodes.forEach(roleCode -> systemUserMapper.insertRole(userId, roleCode));
    }

    private String normalizeUsername(String username) {
        return trim(username).toLowerCase(Locale.ROOT);
    }

    private String normalizeRoleCode(String roleCode) {
        return trim(roleCode).toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
