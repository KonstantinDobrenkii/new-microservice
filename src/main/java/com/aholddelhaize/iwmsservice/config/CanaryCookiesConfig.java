package com.aholddelhaize.iwmsservice.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class CanaryCookiesConfig {

    @Value("${canary.hybris.cookie.enabled:false}")
    private boolean isCanaryHybrisCookieEnabled;

    @Value("${canary.hybris.cookie.value}")
    private String canaryHybrisCookieValue;
}
