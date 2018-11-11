package com.glovoapp.backender;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Configuration
@ComponentScan("com.glovoapp.backender")
@EnableAutoConfiguration
@PropertySource("classpath:application.properties")
class API {
    private final String welcomeMessage;

    API(@Value("${backender.welcome_message}") String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public static void main(String[] args) {
        SpringApplication.run(API.class);
    }

    @GetMapping("/")
    @ResponseBody
    String root() {
        return welcomeMessage;
    }
}
