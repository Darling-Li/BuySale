package com.rice.trade.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateSaleSettlementRequest(
        @NotNull(message = "结账金额不能为空")
        @DecimalMin(value = "0.01", message = "结账金额必须大于0")
        BigDecimal amount,

        @NotBlank(message = "结账渠道不能为空")
        @Size(max = 32, message = "结账渠道不能超过32个字符")
        String channel,

        LocalDateTime settledAt,

        @Size(max = 500, message = "备注不能超过500个字符")
        String remark
) {
}
