package com.rice.trade.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ddos")
public class DdosProtectionProperties {

    private boolean enabled = false;
    private List<String> pathPrefixes = new ArrayList<>(List.of("/api/"));
    private List<String> clientIpHeaders = new ArrayList<>(List.of("X-Forwarded-For", "X-Real-IP"));
    private String bloomKey = "rice-trade:ddos:blocked-ip:bloom";
    private String counterPrefix = "rice-trade:ddos:req:";
    private long bloomSize = 8_388_608L;
    private int hashCount = 7;
    private long windowSeconds = 60L;
    private long maxRequestsPerWindow = 300L;

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

    public List<String> getClientIpHeaders() {
        return clientIpHeaders;
    }

    public void setClientIpHeaders(List<String> clientIpHeaders) {
        this.clientIpHeaders = clientIpHeaders;
    }

    public String getBloomKey() {
        return bloomKey;
    }

    public void setBloomKey(String bloomKey) {
        this.bloomKey = bloomKey;
    }

    public String getCounterPrefix() {
        return counterPrefix;
    }

    public void setCounterPrefix(String counterPrefix) {
        this.counterPrefix = counterPrefix;
    }

    public long getBloomSize() {
        return bloomSize;
    }

    public void setBloomSize(long bloomSize) {
        this.bloomSize = bloomSize;
    }

    public int getHashCount() {
        return hashCount;
    }

    public void setHashCount(int hashCount) {
        this.hashCount = hashCount;
    }

    public long getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(long windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public long getMaxRequestsPerWindow() {
        return maxRequestsPerWindow;
    }

    public void setMaxRequestsPerWindow(long maxRequestsPerWindow) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
    }
}
