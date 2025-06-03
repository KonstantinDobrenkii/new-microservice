package com.aholddelhaize.iwmsservice.common.rest.templates;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ExtendedRestTemplate extends RestTemplate {

    private final RestTemplateCanaryCookieInterceptor restTemplateCanaryCookieInterceptor;

    public ExtendedRestTemplate(RestTemplateCanaryCookieInterceptor restTemplateCanaryCookieInterceptor) {
        this.restTemplateCanaryCookieInterceptor = restTemplateCanaryCookieInterceptor;
    }

    @PostConstruct
    public void addCanaryCookieInterceptor() {
        getInterceptors().add(restTemplateCanaryCookieInterceptor);
    }
}
