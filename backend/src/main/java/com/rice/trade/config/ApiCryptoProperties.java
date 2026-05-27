package com.rice.trade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.crypto")
public class ApiCryptoProperties {

    private boolean enabled = false;
    private String sharedKey = "";
    private boolean requireEncryptedRequests = true;
    private String requestHeader = "X-Encrypted";
    private String responseHeader = "X-Encrypted";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSharedKey() {
        return sharedKey;
    }

    public void setSharedKey(String sharedKey) {
        this.sharedKey = sharedKey;
    }

    public boolean isRequireEncryptedRequests() {
        return requireEncryptedRequests;
    }

    public void setRequireEncryptedRequests(boolean requireEncryptedRequests) {
        this.requireEncryptedRequests = requireEncryptedRequests;
    }

    public String getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(String requestHeader) {
        this.requestHeader = requestHeader;
    }

    public String getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(String responseHeader) {
        this.responseHeader = responseHeader;
    }
}
