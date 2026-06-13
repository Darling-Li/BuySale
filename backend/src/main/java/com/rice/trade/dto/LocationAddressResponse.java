package com.rice.trade.dto;

public record LocationAddressResponse(
        String province,
        String city,
        String county,
        String addressDetail,
        String fullAddress,
        Double latitude,
        Double longitude
) {
}
