package com.rice.trade.service;

import com.rice.trade.exception.BusinessException;
import java.math.BigDecimal;
import java.math.RoundingMode;

record UnitConversion(
        BigDecimal quantity,
        String unitName,
        BigDecimal unitToJin,
        BigDecimal unitPrice,
        BigDecimal weightJin,
        BigDecimal pricePerJin
) {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE.setScale(4, RoundingMode.HALF_UP);

    static UnitConversion from(
            BigDecimal quantity,
            String unitName,
            BigDecimal unitToJin,
            BigDecimal unitPrice,
            BigDecimal fallbackWeightJin,
            BigDecimal fallbackPricePerJin
    ) {
        if (quantity != null || unitToJin != null || unitPrice != null || (unitName != null && !unitName.isBlank())) {
            BigDecimal normalizedQuantity = positive(quantity, "数量不能为空且必须大于0").setScale(2, RoundingMode.HALF_UP);
            BigDecimal normalizedUnitToJin = positive(unitToJin, "单位换算不能为空且必须大于0").setScale(4, RoundingMode.HALF_UP);
            BigDecimal normalizedUnitPrice = positive(unitPrice, "单位价格不能为空且必须大于0").setScale(4, RoundingMode.HALF_UP);
            BigDecimal weightJin = normalizedQuantity.multiply(normalizedUnitToJin).setScale(2, RoundingMode.HALF_UP);
            BigDecimal pricePerJin = normalizedUnitPrice.divide(normalizedUnitToJin, 4, RoundingMode.HALF_UP);
            return new UnitConversion(
                    normalizedQuantity,
                    normalizeUnitName(unitName),
                    normalizedUnitToJin,
                    normalizedUnitPrice,
                    weightJin,
                    pricePerJin
            );
        }

        if (fallbackWeightJin != null && fallbackPricePerJin != null) {
            BigDecimal weightJin = positive(fallbackWeightJin, "重量不能为空且必须大于0").setScale(2, RoundingMode.HALF_UP);
            BigDecimal pricePerJin = positive(fallbackPricePerJin, "每斤价格不能为空且必须大于0").setScale(4, RoundingMode.HALF_UP);
            return new UnitConversion(weightJin, "斤", ONE, pricePerJin, weightJin, pricePerJin);
        }

        throw new BusinessException("请填写数量、单位换算和单位价格");
    }

    BigDecimal totalAmount() {
        return quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal positive(BigDecimal value, String message) {
        if (value == null || value.compareTo(ZERO) <= 0) {
            throw new BusinessException(message);
        }
        return value;
    }

    private static String normalizeUnitName(String value) {
        if (value == null || value.isBlank()) {
            return "斤";
        }
        String normalized = value.trim();
        if (normalized.length() > 20) {
            throw new BusinessException("单位名称不能超过20个字符");
        }
        return normalized;
    }
}
