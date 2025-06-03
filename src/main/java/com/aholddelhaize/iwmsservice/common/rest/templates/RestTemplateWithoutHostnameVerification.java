package com.aholddelhaize.iwmsservice.common.rest.templates;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;


/**
 * RestTemplate implementation meant to be used only locally during development
 * in order to bypass SSL verification error for local Hybris instance.
 * For this reason, all Sonar security warnings are ignored for this class.
 */
@SuppressWarnings({"java:S4423", "java:S5527"})
@Log4j2
@Component
public class RestTemplateWithoutHostnameVerification extends ExtendedRestTemplate {

    public RestTemplateWithoutHostnameVerification(RestTemplateCanaryCookieInterceptor restTemplateCanaryCookieInterceptor) {
        super(restTemplateCanaryCookieInterceptor);
        try {
            customizeErrorHandler();
            customizeRequestFactory();
        } catch (Exception e) {
            log.info("Failed to customize RestTemplateWithoutSSLVerification", e);
        }
    }

    /**
     * Error handler customization provided in
     * org.springframework.security.oauth2.provider.token.RemoteTokenServices#RemoteTokenServices()
     */
    private void customizeErrorHandler() {
        this.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(@NonNull ClientHttpResponse response) throws IOException {
                if (response.getStatusCode().value() != 400) {
                    super.handleError(response);
                }
            }
        });
    }

    /**
     * This request factory is intended to allow all connections
     */
    private void customizeRequestFactory() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) { } //NOSONAR

            public void checkServerTrusted(X509Certificate[] certs, String authType) { } //NOSONAR
        }};

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        this.setRequestFactory(new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(@NonNull HttpURLConnection connection, @NonNull String httpMethod) throws IOException {
                if (connection instanceof HttpsURLConnection) {
                    HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
                }
                super.prepareConnection(connection, httpMethod);
            }
        });
    }
}
