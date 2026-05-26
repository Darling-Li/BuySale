package com.rice.trade.controller;

import com.rice.trade.dto.WarehouseRequest;
import com.rice.trade.dto.WarehouseResponse;
import com.rice.trade.service.WarehouseService;
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
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    public List<WarehouseResponse> list() {
        return warehouseService.list();
    }

    @GetMapping("/{id}")
    public WarehouseResponse get(@PathVariable Long id) {
        return warehouseService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WarehouseResponse create(@Valid @RequestBody WarehouseRequest request) {
        return warehouseService.create(request);
    }

    @PutMapping("/{id}")
    public WarehouseResponse update(@PathVariable Long id, @Valid @RequestBody WarehouseRequest request) {
        return warehouseService.update(id, request);
    }
}

