package com.rice.trade.dto;

import java.util.List;

public record AuthMeResponse(
        String username,
        List<String> roles,
        boolean admin
) {
}

