package com.chikere.bp.bptracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAPI documentation (Swagger)
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bpTrackerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Blood Pressure Tracker Application")
                        .description("Application for tracking and analyzing blood pressure readings")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Milton Chikere Ezeh")
                                .email("chikere@gmail.com")
                                .url("https://chikere.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}