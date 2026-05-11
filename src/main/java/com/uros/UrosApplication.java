package com.uros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UrosApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrosApplication.class, args);
    }
}

