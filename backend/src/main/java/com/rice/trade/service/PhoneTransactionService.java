package com.rice.trade.service;

import com.rice.trade.dto.PhoneTransactionResponse;
import com.rice.trade.entity.PurchaseOrder;
import com.rice.trade.entity.SaleOrder;
import com.rice.trade.exception.BusinessException;
import com.rice.trade.mapper.PurchaseOrderMapper;
import com.rice.trade.mapper.SaleOrderMapper;
import com.rice.trade.mapper.SaleSettlementMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PhoneTransactionService {

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final SaleOrderMapper saleOrderMapper;
    private final SaleSettlementMapper saleSettlementMapper;
    private final ProductCategoryService productCategoryService;

    public PhoneTransactionService(
            PurchaseOrderMapper purchaseOrderMapper,
            SaleOrderMapper saleOrderMapper,
            SaleSettlementMapper saleSettlementMapper,
            ProductCategoryService productCategoryService
    ) {
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.saleOrderMapper = saleOrderMapper;
        this.saleSettlementMapper = saleSettlementMapper;
        this.productCategoryService = productCategoryService;
    }

    @Transactional(readOnly = true)
    public List<PhoneTransactionResponse> search(String phone) {
        String normalizedPhone = normalizePhone(phone);
        if (normalizedPhone.isBlank()) {
            throw new BusinessException("手机号不能为空");
        }

        List<PhoneTransactionResponse> purchases = purchaseOrderMapper.findByCounterpartyPhone(normalizedPhone).stream()
                .map(this::toPurchaseResponse)
                .toList();
        List<PhoneTransactionResponse> sales = saleOrderMapper.findByBuyerPhone(normalizedPhone).stream()
                .map(this::toSaleResponse)
                .toList();

        return Stream.concat(purchases.stream(), sales.stream())
                .sorted(Comparator.comparing(PhoneTransactionResponse::transactionDate).reversed()
                        .thenComparing(PhoneTransactionResponse::businessId, Comparator.reverseOrder()))
                .toList();
    }

    private PhoneTransactionResponse toPurchaseResponse(PurchaseOrder order) {
        return new PhoneTransactionResponse(
                "PURCHASE",
                "采购入库",
                order.getId(),
                transactionDateTime(order.getPurchasedAt(), order.getCreatedAt()),
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
                null,
                null,
                "-",
                order.getRemark(),
                order.getCreatedAt()
        );
    }

    private PhoneTransactionResponse toSaleResponse(SaleOrder order) {
        BigDecimal settledAmount = saleSettlementMapper.sumAmountBySaleOrderId(order.getId());
        settledAmount = settledAmount == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : settledAmount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal unsettledAmount = order.getTotalAmount().subtract(settledAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        return new PhoneTransactionResponse(
                "SALE",
                "销售出库",
                order.getId(),
                transactionDateTime(order.getSoldAt(), order.getCreatedAt()),
                order.getProductType(),
                productCategoryService.labelOf(order.getProductType()),
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
                settledAmount,
                unsettledAmount,
                settlementChannels(order.getId()),
                order.getRemark(),
                order.getCreatedAt()
        );
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.trim();
    }

    private LocalDateTime transactionDateTime(LocalDate businessDate, LocalDateTime createdAt) {
        if (businessDate == null) {
            return createdAt;
        }
        LocalTime time = createdAt == null ? LocalTime.MIDNIGHT : createdAt.toLocalTime();
        return LocalDateTime.of(businessDate, time);
    }

    private String settlementChannels(Long saleOrderId) {
        LinkedHashSet<String> labels = new LinkedHashSet<>();
        saleSettlementMapper.findBySaleOrderId(saleOrderId).forEach(settlement -> labels.add(channelLabel(settlement.getChannel())));
        return labels.isEmpty() ? "-" : String.join("、", labels);
    }

    private String channelLabel(String channel) {
        return switch (channel) {
            case "BANK_CARD" -> "银行卡";
            case "TRANSFER" -> "微信/支付宝转账";
            case "CASH" -> "现金";
            default -> "其他";
        };
    }
}
