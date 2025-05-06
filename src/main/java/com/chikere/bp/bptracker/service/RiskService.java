package com.chikere.bp.bptracker.service;

import com.chikere.bp.bptracker.model.enums.RiskLevel;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Facade service for risk assessment of blood pressure readings.
 * Delegates to specialized services for different types of risk assessment.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RiskService {

    private final RuleBasedRiskService ruleBasedRiskService;
    private final AIRiskAssessmentService aiRiskAssessmentService;
    private final Timer ruleBasedRiskAssessmentTimer;
    private final Timer aiRiskAssessmentTimer;
    private final Counter riskLevelCounter;
    private final MeterRegistry meterRegistry;

    private static final String UNKNOWN = "UNKNOWN";

    /**
     * Assesses the risk level based on the patient's most recent blood pressure reading.
     * Delegates to RuleBasedRiskService.
     *
     * @param patientId The ID of the patient
     * @return The risk level as a string
     */
    public String captureAndAssessImmediateReading(UUID patientId) {
        log.info("Delegating immediate risk assessment for patient with ID: {} to RuleBasedRiskService", patientId);
        return ruleBasedRiskAssessmentTimer.record(() -> {
            String riskLevel = ruleBasedRiskService.captureAndAssessImmediateReading(patientId);
            // Increment counter with risk level tag
            Counter.builder("risk.level.assessment")
                    .tag("level", riskLevel)
                    .tag("method", "rule-based")
                    .register(meterRegistry)
                    .increment();
            return riskLevel;
        });
    }

    /**
     * Uses AI to assess patient risk based on their last 3 readings.
     * Delegates to AIRiskAssessmentService.
     *
     * @param patientId The ID of the patient
     * @return The risk level as a string
     */
    public String accessRiskWithAI(UUID patientId) {
        log.info("Delegating AI-based risk assessment for patient with ID: {} to AIRiskAssessmentService", patientId);
        return aiRiskAssessmentTimer.record(() -> {
            String riskLevel = aiRiskAssessmentService.assessRiskWithAI(patientId);
            // Increment counter with risk level tag
            Counter.builder("risk.level.assessment")
                    .tag("level", riskLevel)
                    .tag("method", "ai-based")
                    .register(meterRegistry)
                    .increment();
            return riskLevel;
        });
    }
}
