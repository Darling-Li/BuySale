package com.rice.trade.service;

import com.rice.trade.dto.CreateSaleRequest;
import com.rice.trade.dto.SaleResponse;
import com.rice.trade.dto.UpdateSettlementRequest;
import com.rice.trade.entity.InventoryItem;
import com.rice.trade.entity.InventoryTransaction;
import com.rice.trade.entity.SaleOrder;
import com.rice.trade.entity.Warehouse;
import com.rice.trade.enums.BusinessType;
import com.rice.trade.enums.ProductType;
import com.rice.trade.enums.TransactionType;
import com.rice.trade.exception.BusinessException;
import com.rice.trade.exception.ResourceNotFoundException;
import com.rice.trade.mapper.InventoryTransactionMapper;
import com.rice.trade.mapper.SaleOrderMapper;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaleService {

    private final SaleOrderMapper saleOrderMapper;
    private final InventoryTransactionMapper inventoryTransactionMapper;
    private final WarehouseService warehouseService;
    private final InventoryService inventoryService;

    public SaleService(
            SaleOrderMapper saleOrderMapper,
            InventoryTransactionMapper inventoryTransactionMapper,
            WarehouseService warehouseService,
            InventoryService inventoryService
    ) {
        this.saleOrderMapper = saleOrderMapper;
        this.inventoryTransactionMapper = inventoryTransactionMapper;
        this.warehouseService = warehouseService;
        this.inventoryService = inventoryService;
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> search(ProductType productType, Long warehouseId, Boolean settled, String keyword) {
        return saleOrderMapper.search(productType, warehouseId, settled, cleanKeyword(keyword)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SaleResponse create(CreateSaleRequest request) {
        UnitConversion conversion = UnitConversion.from(
                request.quantity(),
                request.unitName(),
                request.unitToJin(),
                request.unitPrice(),
                request.weightJin(),
                request.pricePerJin()
        );
        InventoryItem inventoryItem = resolveInventoryItem(request);
        Warehouse warehouse = inventoryItem == null ? warehouseService.requireWarehouse(requireWarehouseId(request)) : inventoryItem.getWarehouse();
        ProductType productType = inventoryItem == null ? request.productType() : inventoryItem.getProductType();
        String productName = inventoryItem == null ? requireProductName(request.productName()) : inventoryItem.getProductName();

        inventoryService.decrease(warehouse, productType, productName, conversion.weightJin());

        SaleOrder order = new SaleOrder();
        order.setProductType(productType);
        order.setProductName(productName);
        order.setWarehouse(warehouse);
        order.setBuyerName(normalizeName(request.buyerName()));
        order.setBuyerPhone(trim(request.buyerPhone()));
        order.setBuyerAddress(trim(request.buyerAddress()));
        order.setQuantity(conversion.quantity());
        order.setUnitName(conversion.unitName());
        order.setUnitToJin(conversion.unitToJin());
        order.setUnitPrice(conversion.unitPrice());
        order.setWeightJin(conversion.weightJin());
        order.setPricePerJin(conversion.pricePerJin());
        order.setTotalAmount(conversion.totalAmount());
        order.setSoldAt(request.soldAt());
        order.setSettled(request.settled());
        order.setRemark(trim(request.remark()));
        saleOrderMapper.insert(order);
        SaleOrder saved = saleOrderMapper.findById(order.getId());

        inventoryTransactionMapper.insert(transaction(saved, warehouse));
        return toResponse(saved);
    }

    @Transactional
    public SaleResponse updateSettlement(Long id, UpdateSettlementRequest request) {
        SaleOrder order = saleOrderMapper.findById(id);
        if (order == null) {
            throw new ResourceNotFoundException("销售记录不存在：" + id);
        }
        saleOrderMapper.updateSettlement(id, request.settled());
        return toResponse(saleOrderMapper.findById(id));
    }

    private InventoryTransaction transaction(SaleOrder order, Warehouse warehouse) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionType(TransactionType.OUTBOUND);
        transaction.setBusinessType(BusinessType.SALE);
        transaction.setBusinessId(order.getId());
        transaction.setProductType(order.getProductType());
        transaction.setProductName(order.getProductName());
        transaction.setWarehouse(warehouse);
        transaction.setWeightJin(order.getWeightJin());
        transaction.setPricePerJin(order.getPricePerJin());
        transaction.setOccurredAt(LocalDateTime.of(order.getSoldAt(), LocalTime.now()));
        transaction.setRemark("销售出库");
        return transaction;
    }

    public SaleResponse toResponse(SaleOrder order) {
        return new SaleResponse(
                order.getId(),
                order.getProductType(),
                order.getProductType().getLabel(),
                order.getProductName(),
                order.getWarehouse().getId(),
                order.getWarehouse().getName(),
                order.getBuyerName(),
                order.getBuyerPhone(),
                order.getBuyerAddress(),
                order.getQuantity(),
                order.getUnitName(),
                order.getUnitToJin(),
                order.getUnitPrice(),
                order.getWeightJin(),
                order.getPricePerJin(),
                order.getTotalAmount(),
                order.isSettled(),
                order.getSoldAt(),
                order.getRemark(),
                order.getCreatedAt()
        );
    }

    private String cleanKeyword(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private InventoryItem resolveInventoryItem(CreateSaleRequest request) {
        if (request.inventoryItemId() == null) {
            return null;
        }

        InventoryItem item = inventoryService.requireItem(request.inventoryItemId());
        if (request.productType() != null && request.productType() != item.getProductType()) {
            throw new BusinessException("库存来源与商品类型不一致");
        }
        return item;
    }

    private Long requireWarehouseId(CreateSaleRequest request) {
        if (request.warehouseId() == null) {
            throw new BusinessException("仓库不能为空");
        }
        return request.warehouseId();
    }

    private String requireProductName(String value) {
        String productName = normalizeName(value);
        if (productName.isBlank()) {
            throw new BusinessException("商品名称不能为空");
        }
        return productName;
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.trim();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
