package com.rice.trade.service;

import com.rice.trade.dto.WarehouseRequest;
import com.rice.trade.dto.WarehouseResponse;
import com.rice.trade.entity.Warehouse;
import com.rice.trade.exception.ResourceNotFoundException;
import com.rice.trade.mapper.WarehouseMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WarehouseService {

    private final WarehouseMapper warehouseMapper;

    public WarehouseService(WarehouseMapper warehouseMapper) {
        this.warehouseMapper = warehouseMapper;
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> list() {
        return warehouseMapper.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WarehouseResponse get(Long id) {
        return toResponse(requireWarehouse(id));
    }

    @Transactional
    public WarehouseResponse create(WarehouseRequest request) {
        Warehouse warehouse = new Warehouse();
        fill(warehouse, request);
        warehouseMapper.insert(warehouse);
        return toResponse(requireWarehouse(warehouse.getId()));
    }

    @Transactional
    public WarehouseResponse update(Long id, WarehouseRequest request) {
        Warehouse warehouse = requireWarehouse(id);
        fill(warehouse, request);
        warehouse.setId(id);
        warehouseMapper.update(warehouse);
        return toResponse(requireWarehouse(id));
    }

    @Transactional(readOnly = true)
    public Warehouse requireWarehouse(Long id) {
        Warehouse warehouse = warehouseMapper.findById(id);
        if (warehouse == null) {
            throw new ResourceNotFoundException("仓库不存在：" + id);
        }
        return warehouse;
    }

    private void fill(Warehouse warehouse, WarehouseRequest request) {
        warehouse.setName(trim(request.name()));
        warehouse.setAddress(trim(request.address()));
        warehouse.setContactName(trim(request.contactName()));
        warehouse.setContactPhone(trim(request.contactPhone()));
        warehouse.setRemark(trim(request.remark()));
    }

    public WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getAddress(),
                warehouse.getContactName(),
                warehouse.getContactPhone(),
                warehouse.getRemark(),
                warehouse.getCreatedAt(),
                warehouse.getUpdatedAt()
        );
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
