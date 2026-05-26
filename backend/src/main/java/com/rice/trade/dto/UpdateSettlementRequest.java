package com.rice.trade.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateSettlementRequest(
        @NotNull(message = "结算状态不能为空")
        Boolean settled
) {
}

