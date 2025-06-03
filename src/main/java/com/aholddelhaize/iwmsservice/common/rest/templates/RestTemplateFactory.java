package com.aholddelhaize.iwmsservice.common.rest.templates;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateFactory {

    @Resource
    private RestTemplateWithoutHostnameVerification restTemplateWithoutHostnameVerification;

    @Resource
    private ExtendedRestTemplate extendedRestTemplate;

    @Value("${hostname.verification.disabled:false}")
    private boolean isHostnameVerificationDisabled;

    public RestTemplate getRestTemplate() {
        if (isHostnameVerificationDisabled) {
            return restTemplateWithoutHostnameVerification;
        } else {
            return extendedRestTemplate;
        }
    }
}
