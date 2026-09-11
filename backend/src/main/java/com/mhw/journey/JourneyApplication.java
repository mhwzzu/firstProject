package com.mhw.journey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "file:../.env.local", ignoreResourceNotFound = true)
public class JourneyApplication {
    public static void main(String[] args) {
        SpringApplication.run(JourneyApplication.class, args);
    }
}

