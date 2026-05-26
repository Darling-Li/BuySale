package com.rice.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WarehouseRequest(
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 80, message = "仓库名称不能超过80个字符")
        String name,

        @Size(max = 255, message = "仓库地址不能超过255个字符")
        String address,

        @Size(max = 60, message = "联系人不能超过60个字符")
        String contactName,

        @Size(max = 30, message = "联系电话不能超过30个字符")
        String contactPhone,

        @Size(max = 500, message = "备注不能超过500个字符")
        String remark
) {
}

