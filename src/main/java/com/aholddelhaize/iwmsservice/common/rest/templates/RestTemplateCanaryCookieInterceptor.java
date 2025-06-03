package com.aholddelhaize.iwmsservice.common.rest.templates;

import com.aholddelhaize.iwmsservice.config.CanaryCookiesConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestTemplateCanaryCookieInterceptor implements ClientHttpRequestInterceptor {

    private static final String CANARY_HYBRIS = "CANARY_HYBRIS";
    private static final String COOKIE_HEADER = "Cookie";

    private final CanaryCookiesConfig canaryCookieConfig;

    public RestTemplateCanaryCookieInterceptor(CanaryCookiesConfig canaryCookieConfig) {
        this.canaryCookieConfig = canaryCookieConfig;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (isCanaryHybrisCookieEnabled()) {
            enrichRequestWithCanaryHybrisCookie(request);
        }
        return execution.execute(request, body);
    }

    private boolean isCanaryHybrisCookieEnabled() {
        return canaryCookieConfig.isCanaryHybrisCookieEnabled();
    }

    private void enrichRequestWithCanaryHybrisCookie(HttpRequest request) {
        final HttpHeaders headers = request.getHeaders();
        headers.set(COOKIE_HEADER, String.format("%s=%s", CANARY_HYBRIS, canaryCookieConfig.getCanaryHybrisCookieValue()));
    }
}
