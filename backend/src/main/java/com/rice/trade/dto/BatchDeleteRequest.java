package com.rice.trade.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BatchDeleteRequest(
        @NotEmpty(message = "请选择要删除的数据")
        List<Long> ids
) {
}
