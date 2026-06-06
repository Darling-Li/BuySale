package com.rice.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductCategoryRequest(
        @NotBlank(message = "种类编码不能为空")
        @Pattern(regexp = "^[A-Za-z0-9_]{1,32}$", message = "种类编码只能包含字母、数字和下划线")
        String code,

        @NotBlank(message = "种类名称不能为空")
        @Size(max = 80, message = "种类名称不能超过80个字符")
        String name,

        Integer sortOrder,

        Boolean enabled,

        @Size(max = 500, message = "备注不能超过500个字符")
        String remark
) {
}
