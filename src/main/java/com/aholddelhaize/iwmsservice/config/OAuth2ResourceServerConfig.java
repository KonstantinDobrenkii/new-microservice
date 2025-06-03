package com.aholddelhaize.iwmsservice.config;

import com.aholddelhaize.iwmsservice.auth.CachingOpaqueTokenIntrospector;
import com.aholddelhaize.iwmsservice.auth.InMemoryOAuth2AuthenticatedPrincipalStorage;
import com.aholddelhaize.iwmsservice.common.rest.api.RestAccessDeniedHandler;
import com.aholddelhaize.iwmsservice.common.rest.api.RestAuthenticationEntryPoint;
import com.aholddelhaize.iwmsservice.common.rest.templates.RestTemplateFactory;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

import static com.aholddelhaize.iwmsservice.constants.IwmsServiceConstants.SCOPE_AUTHORITY_PREFIX;

@Configuration
public class OAuth2ResourceServerConfig {

    @Value("${iwms.service.oauth2.server.manageScope}")
    private String manageScope;

    @Value("${iwms.service.oauth2.server.readScope}")
    private String readScope;

    @Value("${iwms.service.oauth2.server.check.token.url}")
    private String oauthServerCheckTokenUrl;

    @Value("${iwms.service.oauth2.server.clientId}")
    private String clientId;

    @Value("${iwms.service.oauth2.server.clientSecret}")
    private String clientSecret;

    @Resource
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Resource
    private RestAccessDeniedHandler restAccessDeniedHandler;

    @Resource
    private RestTemplateFactory restTemplateFactory;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/**").hasAnyAuthority(readAuthority(), writeAuthority())
                        .requestMatchers(HttpMethod.POST, "/**").hasAuthority(writeAuthority())
                        .requestMatchers(HttpMethod.PUT, "/**").hasAuthority(writeAuthority())
                        .requestMatchers(HttpMethod.DELETE, "/**").hasAuthority(writeAuthority()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.NEVER))
                .exceptionHandling(handlingConfig -> handlingConfig
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(token -> token.introspector(opaqueTokenIntrospector())))
                .build();
    }

    @Bean
    public OpaqueTokenIntrospector opaqueTokenIntrospector() {
        OpaqueTokenIntrospector customTokenIntrospector = buildCustomTokenIntrospector();
        InMemoryOAuth2AuthenticatedPrincipalStorage storage = new InMemoryOAuth2AuthenticatedPrincipalStorage();
        return new CachingOpaqueTokenIntrospector(storage, customTokenIntrospector);
    }

    private OpaqueTokenIntrospector buildCustomTokenIntrospector() {
        RestTemplate restTemplate = restTemplateFactory.getRestTemplate();
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(clientId, clientSecret));
        return new SpringOpaqueTokenIntrospector(oauthServerCheckTokenUrl, restTemplate);
    }

    private String readAuthority() {
        return SCOPE_AUTHORITY_PREFIX + readScope;
    }

    private String writeAuthority() {
        return SCOPE_AUTHORITY_PREFIX + manageScope;
    }
}
