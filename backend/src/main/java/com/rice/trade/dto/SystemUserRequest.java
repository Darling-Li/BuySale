package com.rice.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SystemUserRequest(
        @NotBlank(message = "用户名不能为空")
        String username,

        String password,
        String displayName,
        Boolean enabled,

        @NotEmpty(message = "用户角色不能为空")
        List<String> roleCodes
) {
}
