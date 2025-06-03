package com.aholddelhaize.iwmsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.aholddelhaize.iwmsservice.common",
        "com.aholddelhaize.iwmsservice.config"
})
public class IwmsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IwmsServiceApplication.class, args);
    }

}
