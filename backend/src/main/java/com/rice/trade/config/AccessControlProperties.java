package com.rice.trade.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.access")
public class AccessControlProperties {

    private boolean enabled = false;
    private List<String> pathPrefixes = new ArrayList<>(List.of("/api/"));
    private List<String> allowedCidrs = new ArrayList<>();
    private List<String> allowedRegions = new ArrayList<>();
    private List<String> regionHeaders = new ArrayList<>(List.of(
            "X-Client-Region",
            "X-Client-Province",
            "X-Ali-Region"
    ));
    private List<String> trustedProxyCidrs = new ArrayList<>(List.of("127.0.0.1/32", "::1/128"));

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getPathPrefixes() {
        return pathPrefixes;
    }

    public void setPathPrefixes(List<String> pathPrefixes) {
        this.pathPrefixes = pathPrefixes;
    }

    public List<String> getAllowedCidrs() {
        return allowedCidrs;
    }

    public void setAllowedCidrs(List<String> allowedCidrs) {
        this.allowedCidrs = allowedCidrs;
    }

    public List<String> getAllowedRegions() {
        return allowedRegions;
    }

    public void setAllowedRegions(List<String> allowedRegions) {
        this.allowedRegions = allowedRegions;
    }

    public List<String> getRegionHeaders() {
        return regionHeaders;
    }

    public void setRegionHeaders(List<String> regionHeaders) {
        this.regionHeaders = regionHeaders;
    }

    public List<String> getTrustedProxyCidrs() {
        return trustedProxyCidrs;
    }

    public void setTrustedProxyCidrs(List<String> trustedProxyCidrs) {
        this.trustedProxyCidrs = trustedProxyCidrs;
    }
}
