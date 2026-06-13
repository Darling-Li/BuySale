package com.rice.trade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.location")
public class LocationProperties {

    private boolean enabled = false;
    private String amapKey = "";
    private String amapUrl = "https://restapi.amap.com/v3/geocode/regeo";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAmapKey() {
        return amapKey;
    }

    public void setAmapKey(String amapKey) {
        this.amapKey = amapKey;
    }

    public String getAmapUrl() {
        return amapUrl;
    }

    public void setAmapUrl(String amapUrl) {
        this.amapUrl = amapUrl;
    }
}
