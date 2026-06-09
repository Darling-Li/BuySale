package com.rice.trade.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateSaleRequest(
        @NotBlank(message = "商品类型不能为空")
        @Size(max = 32, message = "商品类型不能超过32个字符")
        String productType,

        Long inventoryItemId,

        @Size(max = 80, message = "商品名称不能超过80个字符")
        String productName,

        Long warehouseId,

        @NotBlank(message = "客户姓名不能为空")
        @Size(max = 80, message = "客户姓名不能超过80个字符")
        String buyerName,

        @Size(max = 30, message = "电话不能超过30个字符")
        String buyerPhone,

        @Size(max = 255, message = "家庭住址不能超过255个字符")
        String buyerAddress,

        @DecimalMin(value = "0.01", message = "数量必须大于0")
        BigDecimal quantity,

        @Size(max = 20, message = "单位不能超过20个字符")
        String unitName,

        @DecimalMin(value = "0.0001", message = "单位换算必须大于0")
        BigDecimal unitToJin,

        @DecimalMin(value = "0.0001", message = "单位价格必须大于0")
        BigDecimal unitPrice,

        @DecimalMin(value = "0.01", message = "重量必须大于0")
        BigDecimal weightJin,

        @DecimalMin(value = "0.0001", message = "每斤价格必须大于0")
        BigDecimal pricePerJin,

        @NotNull(message = "销售日期不能为空")
        LocalDate soldAt,

        @Size(max = 500, message = "备注不能超过500个字符")
        String remark
) {
}
