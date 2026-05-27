package com.rice.trade.controller;

import com.rice.trade.dto.SystemUserRequest;
import com.rice.trade.dto.SystemUserResponse;
import com.rice.trade.service.SystemUserService;
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
@RequestMapping("/api/system/users")
public class SystemUserController {

    private final SystemUserService systemUserService;

    public SystemUserController(SystemUserService systemUserService) {
        this.systemUserService = systemUserService;
    }

    @GetMapping
    public List<SystemUserResponse> list() {
        return systemUserService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SystemUserResponse create(@Valid @RequestBody SystemUserRequest request) {
        return systemUserService.create(request);
    }

    @PutMapping("/{id}")
    public SystemUserResponse update(@PathVariable Long id, @Valid @RequestBody SystemUserRequest request) {
        return systemUserService.update(id, request);
    }
}
