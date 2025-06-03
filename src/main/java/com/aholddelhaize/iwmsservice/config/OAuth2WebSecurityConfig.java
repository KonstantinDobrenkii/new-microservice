package com.aholddelhaize.iwmsservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Configuration
@EnableWebSecurity
public class OAuth2WebSecurityConfig {

    @Value("${iwms.service.oauth2.remote.auth.enabled:true}")
    private boolean isRemoteAuthEnabled;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> {
            web.ignoring()
                    .requestMatchers(HttpMethod.GET, "/web/**")
                    .requestMatchers(HttpMethod.GET, "/actuator/**")
                    .requestMatchers("/error**");

            if (!isRemoteAuthEnabled) {
                web.ignoring().requestMatchers("/**");
            }

        };
    }
}
