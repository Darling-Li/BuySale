package com.rice.trade.service;

import com.rice.trade.dto.CreatePurchaseRequest;
import com.rice.trade.dto.PurchaseResponse;
import com.rice.trade.entity.InventoryTransaction;
import com.rice.trade.entity.PurchaseOrder;
import com.rice.trade.entity.Warehouse;
import com.rice.trade.enums.BusinessType;
import com.rice.trade.enums.ProductType;
import com.rice.trade.enums.TransactionType;
import com.rice.trade.mapper.InventoryTransactionMapper;
import com.rice.trade.mapper.PurchaseOrderMapper;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseService {

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final InventoryTransactionMapper inventoryTransactionMapper;
    private final WarehouseService warehouseService;
    private final InventoryService inventoryService;

    public PurchaseService(
            PurchaseOrderMapper purchaseOrderMapper,
            InventoryTransactionMapper inventoryTransactionMapper,
            WarehouseService warehouseService,
            InventoryService inventoryService
    ) {
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.inventoryTransactionMapper = inventoryTransactionMapper;
        this.warehouseService = warehouseService;
        this.inventoryService = inventoryService;
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> search(ProductType productType, Long warehouseId, String keyword) {
        return purchaseOrderMapper.search(productType, warehouseId, cleanKeyword(keyword)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PurchaseResponse create(CreatePurchaseRequest request) {
        Warehouse warehouse = warehouseService.requireWarehouse(request.warehouseId());
        UnitConversion conversion = UnitConversion.from(
                request.quantity(),
                request.unitName(),
                request.unitToJin(),
                request.unitPrice(),
                request.weightJin(),
                request.pricePerJin()
        );
        String productName = normalizeName(request.productName());

        PurchaseOrder order = new PurchaseOrder();
        order.setProductType(request.productType());
        order.setProductName(productName);
        order.setWarehouse(warehouse);
        order.setCounterpartyName(normalizeName(request.counterpartyName()));
        order.setCounterpartyPhone(trim(request.counterpartyPhone()));
        order.setCounterpartyAddress(trim(request.counterpartyAddress()));
        order.setQuantity(conversion.quantity());
        order.setUnitName(conversion.unitName());
        order.setUnitToJin(conversion.unitToJin());
        order.setUnitPrice(conversion.unitPrice());
        order.setWeightJin(conversion.weightJin());
        order.setPricePerJin(conversion.pricePerJin());
        order.setTotalAmount(conversion.totalAmount());
        order.setPurchasedAt(request.purchasedAt());
        order.setRemark(trim(request.remark()));
        purchaseOrderMapper.insert(order);
        PurchaseOrder saved = purchaseOrderMapper.findById(order.getId());

        inventoryService.increase(warehouse, request.productType(), productName, conversion.weightJin(), conversion.pricePerJin());
        inventoryTransactionMapper.insert(transaction(saved, warehouse));
        return toResponse(purchaseOrderMapper.findById(saved.getId()));
    }

    private InventoryTransaction transaction(PurchaseOrder order, Warehouse warehouse) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionType(TransactionType.INBOUND);
        transaction.setBusinessType(BusinessType.PURCHASE);
        transaction.setBusinessId(order.getId());
        transaction.setProductType(order.getProductType());
        transaction.setProductName(order.getProductName());
        transaction.setWarehouse(warehouse);
        transaction.setWeightJin(order.getWeightJin());
        transaction.setPricePerJin(order.getPricePerJin());
        transaction.setOccurredAt(LocalDateTime.of(order.getPurchasedAt(), LocalTime.now()));
        transaction.setRemark("采购入库");
        return transaction;
    }

    public PurchaseResponse toResponse(PurchaseOrder order) {
        return new PurchaseResponse(
                order.getId(),
                order.getProductType(),
                order.getProductType().getLabel(),
                order.getProductName(),
                order.getWarehouse().getId(),
                order.getWarehouse().getName(),
                order.getCounterpartyName(),
                order.getCounterpartyPhone(),
                order.getCounterpartyAddress(),
                order.getQuantity(),
                order.getUnitName(),
                order.getUnitToJin(),
                order.getUnitPrice(),
                order.getWeightJin(),
                order.getPricePerJin(),
                order.getTotalAmount(),
                order.getPurchasedAt(),
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

    private String normalizeName(String value) {
        return value == null ? "" : value.trim();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
