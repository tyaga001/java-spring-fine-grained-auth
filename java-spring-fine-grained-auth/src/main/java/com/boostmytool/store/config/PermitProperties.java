package com.boostmytool.store.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "permit")
public class PermitProperties {
    private String apiKey;
    private String pdpUrl;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getPdpUrl() {
        return pdpUrl;
    }

    public void setPdpUrl(String pdpUrl) {
        this.pdpUrl = pdpUrl;
    }
}
