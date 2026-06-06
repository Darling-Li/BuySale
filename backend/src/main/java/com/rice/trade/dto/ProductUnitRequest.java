package com.rice.trade.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductUnitRequest(
        @NotBlank(message = "单位名称不能为空")
        @Size(max = 20, message = "单位名称不能超过20个字符")
        String name,

        @NotNull(message = "每单位折合斤不能为空")
        @DecimalMin(value = "0.0001", message = "每单位折合斤必须大于0")
        BigDecimal unitToJin,

        Integer sortOrder,

        Boolean enabled,

        @Size(max = 500, message = "备注不能超过500个字符")
        String remark
) {
}
