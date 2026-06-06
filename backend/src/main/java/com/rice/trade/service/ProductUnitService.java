package com.rice.trade.service;

import com.rice.trade.dto.ProductUnitRequest;
import com.rice.trade.dto.ProductUnitResponse;
import com.rice.trade.entity.ProductUnit;
import com.rice.trade.exception.BusinessException;
import com.rice.trade.exception.ResourceNotFoundException;
import com.rice.trade.mapper.ProductUnitMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductUnitService {

    private final ProductUnitMapper productUnitMapper;

    public ProductUnitService(ProductUnitMapper productUnitMapper) {
        this.productUnitMapper = productUnitMapper;
    }

    @Transactional(readOnly = true)
    public List<ProductUnitResponse> listAll() {
        return productUnitMapper.listAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductUnitResponse> listEnabled() {
        return productUnitMapper.listEnabled().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductUnit requireEnabledUnit(String name) {
        String normalizedName = normalizeName(name);
        if (normalizedName.isBlank()) {
            normalizedName = "斤";
        }
        ProductUnit unit = productUnitMapper.findByName(normalizedName);
        if (unit == null || !unit.isEnabled()) {
            throw new BusinessException("单位不存在或已禁用：" + normalizedName);
        }
        return unit;
    }

    @Transactional
    public ProductUnitResponse create(ProductUnitRequest request) {
        ProductUnit unit = new ProductUnit();
        apply(unit, request);
        unit.setSystemBuiltin(false);
        assertNameAvailable(unit.getName(), null);
        productUnitMapper.insert(unit);
        return toResponse(productUnitMapper.findById(unit.getId()));
    }

    @Transactional
    public ProductUnitResponse update(Long id, ProductUnitRequest request) {
        ProductUnit unit = productUnitMapper.findById(id);
        if (unit == null) {
            throw new ResourceNotFoundException("单位不存在：" + id);
        }
        if (unit.isSystemBuiltin()) {
            throw new BusinessException("系统固定单位不能修改");
        }
        apply(unit, request);
        assertNameAvailable(unit.getName(), id);
        productUnitMapper.update(unit);
        return toResponse(productUnitMapper.findById(id));
    }

    private void apply(ProductUnit unit, ProductUnitRequest request) {
        unit.setName(normalizeName(request.name()));
        unit.setUnitToJin(request.unitToJin().setScale(4, RoundingMode.HALF_UP));
        unit.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        unit.setEnabled(request.enabled() == null || request.enabled());
        unit.setRemark(trim(request.remark()));
    }

    private void assertNameAvailable(String name, Long currentId) {
        ProductUnit existing = productUnitMapper.findByName(name);
        if (existing != null && !existing.getId().equals(currentId)) {
            throw new BusinessException("单位名称已存在：" + name);
        }
    }

    private ProductUnitResponse toResponse(ProductUnit unit) {
        return new ProductUnitResponse(
                unit.getId(),
                unit.getName(),
                unit.getUnitToJin(),
                unit.isSystemBuiltin(),
                unit.getSortOrder(),
                unit.isEnabled(),
                unit.getRemark(),
                unit.getCreatedAt(),
                unit.getUpdatedAt()
        );
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.trim();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
