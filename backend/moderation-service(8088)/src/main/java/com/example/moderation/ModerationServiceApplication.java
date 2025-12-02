package com.example.moderation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient  // Pour Eureka Client
public class ModerationServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ModerationServiceApplication.class, args);
    }
}