package com.chikere.bp.bptracker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Make  backend “CORS-friendly” so that a front-end running on a different host/port (like your Flutter Web or emulator proxy)
    // can successfully call your REST API without running into browser security blocks.
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/v1/api/**")           // ← matches your /v1/api/patient
                .allowedOrigins("http://localhost:53703", // ← your Flutter web URL
                        "http://localhost:8081",
                        "http://localhost:8080",
                        "*")  // you can use "*" during dev
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}