package com.rice.trade.service;

import com.rice.trade.dto.MonthlyTrendItem;
import com.rice.trade.dto.MonthlyTrendResponse;
import com.rice.trade.entity.PurchaseOrder;
import com.rice.trade.entity.SaleOrder;
import com.rice.trade.mapper.PurchaseOrderMapper;
import com.rice.trade.mapper.SaleOrderMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final SaleOrderMapper saleOrderMapper;
    private final ProductCategoryService productCategoryService;

    public DashboardService(
            PurchaseOrderMapper purchaseOrderMapper,
            SaleOrderMapper saleOrderMapper,
            ProductCategoryService productCategoryService
    ) {
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.saleOrderMapper = saleOrderMapper;
        this.productCategoryService = productCategoryService;
    }

    @Transactional(readOnly = true)
    public MonthlyTrendResponse monthlyTrend(int year, String productType) {
        LocalDate currentYearStart = LocalDate.of(year, 1, 1);
        LocalDate currentYearEnd = LocalDate.of(year, 12, 31);
        LocalDate trendStart = LocalDate.of(year - 1, 1, 1);

        List<PurchaseOrder> purchases = purchaseOrderMapper.findForTrend(currentYearStart, currentYearEnd, productType);
        List<SaleOrder> sales = saleOrderMapper.findForTrend(trendStart, currentYearEnd, productType);

        Map<YearMonth, BigDecimal> purchaseWeight = new HashMap<>();
        Map<YearMonth, BigDecimal> purchaseAmount = new HashMap<>();
        Map<YearMonth, BigDecimal> saleWeight = new HashMap<>();
        Map<YearMonth, BigDecimal> saleAmount = new HashMap<>();

        for (PurchaseOrder purchase : purchases) {
            YearMonth month = YearMonth.from(purchase.getPurchasedAt());
            add(purchaseWeight, month, purchase.getWeightJin());
            add(purchaseAmount, month, purchase.getTotalAmount());
        }

        for (SaleOrder sale : sales) {
            YearMonth month = YearMonth.from(sale.getSoldAt());
            add(saleWeight, month, sale.getWeightJin());
            add(saleAmount, month, sale.getTotalAmount());
        }

        List<MonthlyTrendItem> items = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            YearMonth current = YearMonth.of(year, month);
            boolean hasCurrentSaleAmount = saleAmount.containsKey(current);
            BigDecimal currentSaleAmount = value(saleAmount, current);
            items.add(new MonthlyTrendItem(
                    month,
                    value(purchaseWeight, current),
                    value(purchaseAmount, current),
                    value(saleWeight, current),
                    currentSaleAmount,
                    hasCurrentSaleAmount ? rate(currentSaleAmount, value(saleAmount, current.minusYears(1))) : null,
                    hasCurrentSaleAmount ? rate(currentSaleAmount, value(saleAmount, current.minusMonths(1))) : null
            ));
        }

        return new MonthlyTrendResponse(
                year,
                productType,
                productType == null ? "全部" : productCategoryService.labelOf(productType),
                items
        );
    }

    private void add(Map<YearMonth, BigDecimal> map, YearMonth key, BigDecimal value) {
        map.merge(key, value, BigDecimal::add);
    }

    private BigDecimal value(Map<YearMonth, BigDecimal> map, YearMonth key) {
        return map.getOrDefault(key, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal rate(BigDecimal current, BigDecimal base) {
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return current.subtract(base)
                .multiply(BigDecimal.valueOf(100))
                .divide(base, 2, RoundingMode.HALF_UP);
    }
}
