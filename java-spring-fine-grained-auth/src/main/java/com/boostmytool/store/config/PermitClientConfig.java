package com.boostmytool.store.config;

import io.permit.sdk.Permit;
import io.permit.sdk.PermitConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration  // Marks this class as a source of bean definitions for Spring IoC container
public class PermitClientConfig {

    // Injects the Permit API key from the application properties
    @Value("${permit.api-key}")
    private String apiKey;

    // Injects the Permit PDP (Policy Decision Point) URL from the application properties
    @Value("${permit.pdp-url}")
    private String pdpUrl;

    // Defines a Spring Bean for the Permit client with a custom configuration
    @Bean
    public Permit permit() {
        return new Permit(
                new PermitConfig.Builder(apiKey)  // Initialize PermitConfig with the API key
                        .withPdpAddress(pdpUrl)   // Set the PDP address using the injected URL
                        .withDebugMode(true)      // Enable debug mode for detailed logging
                        .build()                  // Build the PermitConfig object
        );
    }
}

