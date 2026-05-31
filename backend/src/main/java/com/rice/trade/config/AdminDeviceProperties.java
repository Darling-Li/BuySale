package com.rice.trade.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin-device")
public class AdminDeviceProperties {

    private boolean enabled = false;
    private String headerName = "X-Admin-Device-Token";
    private List<String> allowedTokenHashes = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public List<String> getAllowedTokenHashes() {
        return allowedTokenHashes;
    }

    public void setAllowedTokenHashes(List<String> allowedTokenHashes) {
        this.allowedTokenHashes = allowedTokenHashes;
    }
}
