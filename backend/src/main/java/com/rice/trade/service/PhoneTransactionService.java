package com.rice.trade.service;

import com.rice.trade.dto.PhoneTransactionResponse;
import com.rice.trade.entity.PurchaseOrder;
import com.rice.trade.entity.SaleOrder;
import com.rice.trade.exception.BusinessException;
import com.rice.trade.mapper.PurchaseOrderMapper;
import com.rice.trade.mapper.SaleOrderMapper;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PhoneTransactionService {

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final SaleOrderMapper saleOrderMapper;
    private final ProductCategoryService productCategoryService;

    public PhoneTransactionService(
            PurchaseOrderMapper purchaseOrderMapper,
            SaleOrderMapper saleOrderMapper,
            ProductCategoryService productCategoryService
    ) {
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.saleOrderMapper = saleOrderMapper;
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
                order.getPurchasedAt(),
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
                order.getRemark(),
                order.getCreatedAt()
        );
    }

    private PhoneTransactionResponse toSaleResponse(SaleOrder order) {
        return new PhoneTransactionResponse(
                "SALE",
                "销售出库",
                order.getId(),
                order.getSoldAt(),
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
                order.isSettled(),
                order.getRemark(),
                order.getCreatedAt()
        );
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.trim();
    }
}
