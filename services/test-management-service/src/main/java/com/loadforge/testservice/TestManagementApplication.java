package com.loadforge.testservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TestManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestManagementApplication.class, args);
    }
}
