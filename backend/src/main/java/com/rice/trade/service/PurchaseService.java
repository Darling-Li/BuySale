package com.rice.trade.service;

import com.rice.trade.dto.CreatePurchaseRequest;
import com.rice.trade.dto.PurchaseResponse;
import com.rice.trade.entity.InventoryTransaction;
import com.rice.trade.entity.ProductUnit;
import com.rice.trade.entity.PurchaseOrder;
import com.rice.trade.entity.Warehouse;
import com.rice.trade.enums.BusinessType;
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
    private final ProductCategoryService productCategoryService;
    private final ProductUnitService productUnitService;

    public PurchaseService(
            PurchaseOrderMapper purchaseOrderMapper,
            InventoryTransactionMapper inventoryTransactionMapper,
            WarehouseService warehouseService,
            InventoryService inventoryService,
            ProductCategoryService productCategoryService,
            ProductUnitService productUnitService
    ) {
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.inventoryTransactionMapper = inventoryTransactionMapper;
        this.warehouseService = warehouseService;
        this.inventoryService = inventoryService;
        this.productCategoryService = productCategoryService;
        this.productUnitService = productUnitService;
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> search(String productType, Long warehouseId, String keyword) {
        return purchaseOrderMapper.search(productType, warehouseId, cleanKeyword(keyword)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PurchaseResponse create(CreatePurchaseRequest request) {
        Warehouse warehouse = warehouseService.requireWarehouse(request.warehouseId());
        ProductUnit unit = productUnitService.requireEnabledUnit(request.unitName());
        UnitConversion conversion = UnitConversion.from(
                request.quantity(),
                unit.getName(),
                unit.getUnitToJin(),
                request.unitPrice(),
                request.weightJin(),
                request.pricePerJin()
        );
        String productName = normalizeName(request.productName());
        String productType = productCategoryService.requireEnabledCode(request.productType());

        PurchaseOrder order = new PurchaseOrder();
        order.setProductType(productType);
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

        inventoryService.increase(warehouse, productType, productName, conversion.weightJin(), conversion.pricePerJin());
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
                productCategoryService.labelOf(order.getProductType()),
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
