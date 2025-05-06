package com.chikere.bp.bptracker.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Micrometer metrics.
 * Defines custom metrics and counters used across the application.
 */
@Configuration
public class MetricsConfiguration {

    /**
     * Timer for rule-based risk assessment operations.
     */
    @Bean
    public Timer ruleBasedRiskAssessmentTimer(MeterRegistry registry) {
        return Timer.builder("risk.assessment.rule.based")
                .description("Timer for rule-based risk assessment operations")
                .register(registry);
    }

    /**
     * Timer for AI-based risk assessment operations.
     */
    @Bean
    public Timer aiRiskAssessmentTimer(MeterRegistry registry) {
        return Timer.builder("risk.assessment.ai.based")
                .description("Timer for AI-based risk assessment operations")
                .register(registry);
    }

    /**
     * Counter for successful AI service calls.
     */
    @Bean
    public Counter aiServiceSuccessCounter(MeterRegistry registry) {
        return Counter.builder("ai.service.calls.success")
                .description("Number of successful AI service calls")
                .register(registry);
    }

    /**
     * Counter for failed AI service calls.
     */
    @Bean
    public Counter aiServiceFailureCounter(MeterRegistry registry) {
        return Counter.builder("ai.service.calls.failure")
                .description("Number of failed AI service calls")
                .register(registry);
    }

    /**
     * Counter for risk level assessments by type.
     */
    @Bean
    public Counter riskLevelCounter(MeterRegistry registry) {
        return Counter.builder("risk.level.assessment")
                .description("Number of risk level assessments by type")
                .tag("level", "unknown") // Default tag, will be replaced in service
                .register(registry);
    }
}