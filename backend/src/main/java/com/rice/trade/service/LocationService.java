package com.rice.trade.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rice.trade.config.LocationProperties;
import com.rice.trade.dto.LocationAddressResponse;
import com.rice.trade.exception.BusinessException;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class LocationService {

    private final LocationProperties properties;
    private final RestClient restClient;

    public LocationService(LocationProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    public LocationAddressResponse reverseGeocode(Double latitude, Double longitude) {
        validateCoordinates(latitude, longitude);
        if (!properties.isEnabled() || properties.getAmapKey() == null || properties.getAmapKey().isBlank()) {
            throw new BusinessException("定位解析未配置，请先设置 APP_LOCATION_ENABLED=true 和 APP_LOCATION_AMAP_KEY");
        }

        String location = String.format(Locale.ROOT, "%.6f,%.6f", longitude, latitude);
        JsonNode body;
        try {
            body = restClient.get()
                    .uri(properties.getAmapUrl() + "?key={key}&location={location}&extensions=base&output=JSON",
                            properties.getAmapKey(),
                            location)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException ex) {
            throw new BusinessException("定位解析服务调用失败，请稍后重试");
        }

        if (body == null || !"1".equals(text(body, "status"))) {
            throw new BusinessException("定位解析失败：" + text(body, "info"));
        }

        JsonNode regeocode = body.path("regeocode");
        JsonNode component = regeocode.path("addressComponent");
        String province = clean(text(component, "province"));
        String city = clean(text(component, "city"));
        if (city.isBlank()) {
            city = province;
        }
        String county = clean(text(component, "district"));
        String fullAddress = clean(text(regeocode, "formatted_address"));
        String detail = removeAreaPrefix(fullAddress, province, city, county);
        return new LocationAddressResponse(
                emptyToNull(province),
                emptyToNull(city),
                emptyToNull(county),
                emptyToNull(detail),
                emptyToNull(fullAddress),
                latitude,
                longitude
        );
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new BusinessException("定位经纬度不能为空");
        }
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new BusinessException("定位经纬度不合法");
        }
    }

    private String removeAreaPrefix(String value, String province, String city, String county) {
        String detail = clean(value);
        detail = removePrefix(detail, province);
        if (!city.equals(province)) {
            detail = removePrefix(detail, city);
        }
        return removePrefix(detail, county);
    }

    private String removePrefix(String value, String prefix) {
        if (value == null || prefix == null || prefix.isBlank()) {
            return clean(value);
        }
        return value.startsWith(prefix) ? clean(value.substring(prefix.length())) : clean(value);
    }

    private String text(JsonNode node, String field) {
        if (node == null || node.path(field).isMissingNode() || node.path(field).isArray()) {
            return "";
        }
        return node.path(field).asText("");
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
