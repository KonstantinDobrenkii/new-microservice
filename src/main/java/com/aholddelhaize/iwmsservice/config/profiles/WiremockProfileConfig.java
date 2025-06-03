package com.aholddelhaize.iwmsservice.config.profiles;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("wiremock")
public class WiremockProfileConfig {

    private WireMockServer wireMockServer;

    @PostConstruct
    public void init() {
        WireMockConfiguration config = new WireMockConfiguration()
                .port(9999)
                .globalTemplating(true)
                .notifier(new ConsoleNotifier(false))
                .usingFilesUnderDirectory("src/main/resources/wiremock");
        wireMockServer = new WireMockServer(config);
        wireMockServer.start();
    }

    @PreDestroy
    public void destroy() {
        wireMockServer.stop();
    }
}
