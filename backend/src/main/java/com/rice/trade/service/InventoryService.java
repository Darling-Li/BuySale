package com.rice.trade.service;

import com.rice.trade.dto.InventoryResponse;
import com.rice.trade.entity.InventoryItem;
import com.rice.trade.entity.Warehouse;
import com.rice.trade.exception.BusinessException;
import com.rice.trade.exception.ResourceNotFoundException;
import com.rice.trade.mapper.InventoryItemMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final InventoryItemMapper inventoryItemMapper;
    private final ProductCategoryService productCategoryService;

    public InventoryService(InventoryItemMapper inventoryItemMapper, ProductCategoryService productCategoryService) {
        this.inventoryItemMapper = inventoryItemMapper;
        this.productCategoryService = productCategoryService;
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> search(String productType, Long warehouseId, String keyword) {
        return inventoryItemMapper.search(productType, warehouseId, cleanKeyword(keyword)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public InventoryItem increase(
            Warehouse warehouse,
            String productType,
            String productName,
            BigDecimal weightJin,
            BigDecimal pricePerJin
    ) {
        String normalizedName = normalizeName(productName);
        BigDecimal weight = quantity(weightJin);
        BigDecimal price = price(pricePerJin);
        InventoryItem item = inventoryItemMapper.findByScope(warehouse.getId(), productType, normalizedName);
        if (item == null) {
            item = createItem(warehouse, productType, normalizedName);
            item.setQuantityJin(weight);
            item.setAverageCostPerJin(price);
            inventoryItemMapper.insert(item);
            return inventoryItemMapper.findById(item.getId());
        }

        BigDecimal oldQuantity = item.getQuantityJin();
        BigDecimal oldCost = oldQuantity.multiply(item.getAverageCostPerJin());
        BigDecimal addedCost = weight.multiply(price);
        BigDecimal newQuantity = oldQuantity.add(weight).setScale(2, RoundingMode.HALF_UP);
        BigDecimal newAverageCost = oldCost.add(addedCost).divide(newQuantity, 4, RoundingMode.HALF_UP);

        item.setQuantityJin(newQuantity);
        item.setAverageCostPerJin(newAverageCost);
        inventoryItemMapper.update(item);
        return inventoryItemMapper.findById(item.getId());
    }

    @Transactional
    public InventoryItem decrease(
            Warehouse warehouse,
            String productType,
            String productName,
            BigDecimal weightJin
    ) {
        String normalizedName = normalizeName(productName);
        BigDecimal weight = quantity(weightJin);
        InventoryItem item = inventoryItemMapper.findByScope(warehouse.getId(), productType, normalizedName);
        if (item == null) {
            throw new BusinessException("库存不存在，无法销售：" + normalizedName);
        }

        if (item.getQuantityJin().compareTo(weight) < 0) {
            throw new BusinessException("库存不足，当前库存 " + item.getQuantityJin() + " 斤，销售重量 " + weight + " 斤");
        }

        item.setQuantityJin(item.getQuantityJin().subtract(weight).setScale(2, RoundingMode.HALF_UP));
        inventoryItemMapper.update(item);
        return inventoryItemMapper.findById(item.getId());
    }

    @Transactional(readOnly = true)
    public InventoryItem requireItem(Long id) {
        InventoryItem item = inventoryItemMapper.findById(id);
        if (item == null) {
            throw new ResourceNotFoundException("库存不存在：" + id);
        }
        return item;
    }

    public InventoryResponse toResponse(InventoryItem item) {
        return new InventoryResponse(
                item.getId(),
                item.getWarehouse().getId(),
                item.getWarehouse().getName(),
                item.getProductType(),
                productCategoryService.labelOf(item.getProductType()),
                item.getProductName(),
                item.getQuantityJin(),
                item.getAverageCostPerJin(),
                item.getUpdatedAt()
        );
    }

    private InventoryItem createItem(Warehouse warehouse, String productType, String productName) {
        InventoryItem item = new InventoryItem();
        item.setWarehouse(warehouse);
        item.setProductType(productType);
        item.setProductName(productName);
        item.setQuantityJin(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        item.setAverageCostPerJin(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        return item;
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.trim();
    }

    private String cleanKeyword(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private BigDecimal quantity(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal price(BigDecimal value) {
        return value.setScale(4, RoundingMode.HALF_UP);
    }
}
