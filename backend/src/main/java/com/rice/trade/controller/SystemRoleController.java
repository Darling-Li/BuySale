package com.rice.trade.controller;

import com.rice.trade.dto.SystemRoleRequest;
import com.rice.trade.dto.SystemRoleResponse;
import com.rice.trade.service.SystemRoleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system/roles")
public class SystemRoleController {

    private final SystemRoleService systemRoleService;

    public SystemRoleController(SystemRoleService systemRoleService) {
        this.systemRoleService = systemRoleService;
    }

    @GetMapping
    public List<SystemRoleResponse> list() {
        return systemRoleService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SystemRoleResponse create(@Valid @RequestBody SystemRoleRequest request) {
        return systemRoleService.create(request);
    }

    @PutMapping("/{id}")
    public SystemRoleResponse update(@PathVariable Long id, @Valid @RequestBody SystemRoleRequest request) {
        return systemRoleService.update(id, request);
    }
}
