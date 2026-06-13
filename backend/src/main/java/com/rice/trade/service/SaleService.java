package com.rice.trade.service;

import com.rice.trade.dto.CreateSaleRequest;
import com.rice.trade.dto.CreateSaleSettlementRequest;
import com.rice.trade.dto.SaleResponse;
import com.rice.trade.dto.SaleSettlementResponse;
import com.rice.trade.entity.InventoryItem;
import com.rice.trade.entity.InventoryTransaction;
import com.rice.trade.entity.ProductUnit;
import com.rice.trade.entity.SaleOrder;
import com.rice.trade.entity.SaleSettlement;
import com.rice.trade.entity.Warehouse;
import com.rice.trade.enums.BusinessType;
import com.rice.trade.enums.TransactionType;
import com.rice.trade.exception.BusinessException;
import com.rice.trade.exception.ResourceNotFoundException;
import com.rice.trade.mapper.InventoryTransactionMapper;
import com.rice.trade.mapper.SaleOrderMapper;
import com.rice.trade.mapper.SaleSettlementMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaleService {

    private final SaleOrderMapper saleOrderMapper;
    private final SaleSettlementMapper saleSettlementMapper;
    private final InventoryTransactionMapper inventoryTransactionMapper;
    private final WarehouseService warehouseService;
    private final InventoryService inventoryService;
    private final ProductCategoryService productCategoryService;
    private final ProductUnitService productUnitService;

    public SaleService(
            SaleOrderMapper saleOrderMapper,
            SaleSettlementMapper saleSettlementMapper,
            InventoryTransactionMapper inventoryTransactionMapper,
            WarehouseService warehouseService,
            InventoryService inventoryService,
            ProductCategoryService productCategoryService,
            ProductUnitService productUnitService
    ) {
        this.saleOrderMapper = saleOrderMapper;
        this.saleSettlementMapper = saleSettlementMapper;
        this.inventoryTransactionMapper = inventoryTransactionMapper;
        this.warehouseService = warehouseService;
        this.inventoryService = inventoryService;
        this.productCategoryService = productCategoryService;
        this.productUnitService = productUnitService;
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> search(String productType, Long warehouseId, String keyword) {
        return saleOrderMapper.search(productType, warehouseId, cleanKeyword(keyword)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SaleResponse create(CreateSaleRequest request) {
        ProductUnit unit = productUnitService.requireEnabledUnit(request.unitName());
        UnitConversion conversion = UnitConversion.from(
                request.quantity(),
                unit.getName(),
                unit.getUnitToJin(),
                request.unitPrice(),
                request.weightJin(),
                request.pricePerJin()
        );
        InventoryItem inventoryItem = resolveInventoryItem(request);
        Warehouse warehouse = inventoryItem == null ? warehouseService.requireWarehouse(requireWarehouseId(request)) : inventoryItem.getWarehouse();
        String productType = inventoryItem == null
                ? productCategoryService.requireEnabledCode(request.productType())
                : productCategoryService.requireEnabledCode(inventoryItem.getProductType());
        String productName = inventoryItem == null ? requireProductName(request.productName()) : inventoryItem.getProductName();

        inventoryService.decrease(warehouse, productType, productName, conversion.weightJin());

        SaleOrder order = new SaleOrder();
        order.setProductType(productType);
        order.setProductName(productName);
        order.setWarehouse(warehouse);
        order.setBuyerName(normalizeName(request.buyerName()));
        order.setBuyerPhone(trim(request.buyerPhone()));
        order.setBuyerProvince(trim(request.buyerProvince()));
        order.setBuyerCity(trim(request.buyerCity()));
        order.setBuyerCounty(trim(request.buyerCounty()));
        order.setBuyerAddressDetail(trim(request.buyerAddressDetail()));
        order.setQuantity(conversion.quantity());
        order.setUnitName(conversion.unitName());
        order.setUnitToJin(conversion.unitToJin());
        order.setUnitPrice(conversion.unitPrice());
        order.setWeightJin(conversion.weightJin());
        order.setPricePerJin(conversion.pricePerJin());
        order.setTotalAmount(conversion.totalAmount());
        order.setSoldAt(request.soldAt());
        order.setRemark(trim(request.remark()));
        saleOrderMapper.insert(order);
        SaleOrder saved = saleOrderMapper.findById(order.getId());

        inventoryTransactionMapper.insert(transaction(saved, warehouse));
        return toResponse(saved);
    }

    @Transactional
    public SaleResponse createSettlement(Long saleOrderId, CreateSaleSettlementRequest request) {
        SaleOrder order = saleOrderMapper.findById(saleOrderId);
        if (order == null) {
            throw new ResourceNotFoundException("销售记录不存在：" + saleOrderId);
        }

        BigDecimal amount = request.amount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal currentSettled = settledAmount(saleOrderId);
        if (currentSettled.add(amount).compareTo(order.getTotalAmount()) > 0) {
            throw new BusinessException("结账金额不能超过销售总金额，剩余未结账 " + order.getTotalAmount().subtract(currentSettled).setScale(2, RoundingMode.HALF_UP));
        }

        SaleSettlement settlement = new SaleSettlement();
        settlement.setSaleOrderId(saleOrderId);
        settlement.setAmount(amount);
        settlement.setChannel(normalizeChannel(request.channel()));
        settlement.setSettledAt(request.settledAt() == null ? LocalDateTime.now() : request.settledAt());
        settlement.setRemark(trim(request.remark()));
        saleSettlementMapper.insert(settlement);
        return toResponse(saleOrderMapper.findById(saleOrderId));
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
        List<SaleSettlementResponse> settlements = saleSettlementMapper.findBySaleOrderId(order.getId()).stream()
                .map(this::toSettlementResponse)
                .toList();
        BigDecimal settledAmount = settlements.stream()
                .map(SaleSettlementResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal unsettledAmount = order.getTotalAmount().subtract(settledAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        return new SaleResponse(
                order.getId(),
                order.getProductType(),
                productCategoryService.labelOf(order.getProductType()),
                order.getProductName(),
                order.getWarehouse().getId(),
                order.getWarehouse().getName(),
                order.getBuyerName(),
                order.getBuyerPhone(),
                order.getBuyerProvince(),
                order.getBuyerCity(),
                order.getBuyerCounty(),
                order.getBuyerAddressDetail(),
                addressDisplay(
                        order.getBuyerProvince(),
                        order.getBuyerCity(),
                        order.getBuyerCounty(),
                        order.getBuyerAddressDetail()
                ),
                order.getQuantity(),
                order.getUnitName(),
                order.getUnitToJin(),
                order.getUnitPrice(),
                order.getWeightJin(),
                order.getPricePerJin(),
                order.getTotalAmount(),
                settledAmount,
                unsettledAmount,
                settlementChannels(settlements),
                settlements,
                order.getSoldAt(),
                order.getRemark(),
                order.getCreatedAt()
        );
    }

    private SaleSettlementResponse toSettlementResponse(SaleSettlement settlement) {
        return new SaleSettlementResponse(
                settlement.getId(),
                settlement.getSaleOrderId(),
                settlement.getAmount(),
                settlement.getChannel(),
                channelLabel(settlement.getChannel()),
                settlement.getSettledAt(),
                settlement.getRemark(),
                settlement.getCreatedAt()
        );
    }

    private BigDecimal settledAmount(Long saleOrderId) {
        BigDecimal amount = saleSettlementMapper.sumAmountBySaleOrderId(saleOrderId);
        return amount == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String settlementChannels(List<SaleSettlementResponse> settlements) {
        LinkedHashSet<String> labels = new LinkedHashSet<>();
        for (SaleSettlementResponse settlement : settlements) {
            labels.add(settlement.channelLabel());
        }
        return labels.isEmpty() ? "-" : String.join("、", labels);
    }

    private String normalizeChannel(String channel) {
        String normalized = channel == null ? "" : channel.trim().toUpperCase();
        return switch (normalized) {
            case "BANK_CARD", "TRANSFER", "CASH", "OTHER" -> normalized;
            default -> throw new BusinessException("结账渠道不支持：" + channel);
        };
    }

    private String channelLabel(String channel) {
        return switch (channel) {
            case "BANK_CARD" -> "银行卡";
            case "TRANSFER" -> "微信/支付宝转账";
            case "CASH" -> "现金";
            default -> "其他";
        };
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
        if (request.productType() != null && !productCategoryService.requireEnabledCode(request.productType()).equals(item.getProductType())) {
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

    private String addressDisplay(String province, String city, String county, String detail) {
        String value = blankToEmpty(province) + blankToEmpty(city) + blankToEmpty(county) + blankToEmpty(detail);
        return value.isBlank() ? null : value;
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

}
