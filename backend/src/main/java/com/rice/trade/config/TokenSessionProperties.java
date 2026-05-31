package com.rice.trade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.token-session")
public class TokenSessionProperties {

    private boolean enabled = true;
    private String keyPrefix = "rice-trade:auth:token:";
    private long ttlSeconds = 1800L;
    private String loginHeader = "X-Login-Request";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public String getLoginHeader() {
        return loginHeader;
    }

    public void setLoginHeader(String loginHeader) {
        this.loginHeader = loginHeader;
    }
}
