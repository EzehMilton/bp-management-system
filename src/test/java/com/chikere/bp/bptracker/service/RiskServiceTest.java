package com.chikere.bp.bptracker.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests for the RiskService facade.
 * This class tests that RiskService properly delegates to the specialized services.
 */
@ExtendWith(MockitoExtension.class)
class RiskServiceTest {

    private RuleBasedRiskService ruleBasedRiskService;
    private AIRiskAssessmentService aiRiskAssessmentService;
    private RiskService riskService;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        ruleBasedRiskService = mock(RuleBasedRiskService.class);
        aiRiskAssessmentService = mock(AIRiskAssessmentService.class);
        Timer ruleBasedRiskAssessmentTimer = mock(Timer.class);
        Timer aiRiskAssessmentTimer = mock(Timer.class);
        Counter riskLevelCounter = mock(Counter.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        // Setup timer to return the value passed to it
        when(ruleBasedRiskAssessmentTimer.record(any(Supplier.class))).thenAnswer(invocation -> {
            return ((Supplier<String>) invocation.getArgument(0)).get();
        });
        when(aiRiskAssessmentTimer.record(any(Supplier.class))).thenAnswer(invocation -> {
            return ((Supplier<String>) invocation.getArgument(0)).get();
        });

        riskService = new RiskService(
            ruleBasedRiskService, 
            aiRiskAssessmentService,
                ruleBasedRiskAssessmentTimer,
                aiRiskAssessmentTimer,
                riskLevelCounter,
                meterRegistry
        );
        patientId = UUID.randomUUID();
    }

    @Test
    void delegatesCaptureAndAssessImmediateReadingToRuleBasedService() {
        // Arrange
        when(ruleBasedRiskService.captureAndAssessImmediateReading(patientId)).thenReturn("NORMAL");

        // Act
        String result = riskService.captureAndAssessImmediateReading(patientId);

        // Assert
        assertEquals("NORMAL", result);
        verify(ruleBasedRiskService, times(1)).captureAndAssessImmediateReading(patientId);
        verifyNoInteractions(aiRiskAssessmentService);
    }

    @Test
    void delegatesAccessRiskWithAIToAIService() {
        // Arrange
        when(aiRiskAssessmentService.assessRiskWithAI(patientId)).thenReturn("MILD_HYPERTENSIVE");

        // Act
        String result = riskService.accessRiskWithAI(patientId);

        // Assert
        assertEquals("MILD_HYPERTENSIVE", result);
        verify(aiRiskAssessmentService, times(1)).assessRiskWithAI(patientId);
        verifyNoInteractions(ruleBasedRiskService);
    }
}
