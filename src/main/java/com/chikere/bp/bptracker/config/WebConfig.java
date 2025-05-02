package com.chikere.bp.bptracker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // This class is kept for potential future configuration needs
    // CORS configuration has been removed as it's only needed for REST API access
}
