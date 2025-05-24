package com.chikere.bp.bptracker.controller;

import com.chikere.bp.bptracker.service.ReadingService;
import com.chikere.bp.bptracker.service.RiskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ApiControllerTest {

    @Mock
    private ReadingService readingService;

    @Mock
    private RiskService riskService;

    @InjectMocks
    private ApiController apiController;

    private MockMvc mockMvc;

    private UUID patientId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(apiController).build();
        patientId = UUID.randomUUID();
    }

    @Test
    void analyzeRiskWithAIShouldReturnRiskLevel() throws Exception {
        when(readingService.hasAtLeastThreeReadings(patientId)).thenReturn(true);
        when(riskService.accessRiskWithAI(patientId)).thenReturn("NORMAL");

        mockMvc.perform(get("/v1/api/risk/{patientId}/analyzeAI", patientId))
                .andExpect(status().isOk())
                .andExpect(content().string("NORMAL"));
    }

    @Test
    void analyzeRiskWithAIWithNotEnoughReadingsShouldReturnBadRequest() throws Exception {
        when(readingService.hasAtLeastThreeReadings(patientId)).thenReturn(false);

        mockMvc.perform(get("/v1/api/risk/{patientId}/analyzeAI", patientId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Patient needs at least 3 readings for risk assessment"));
    }

    @Test
    void analyzeRiskWithAIWithExceptionShouldReturnInternalServerError() throws Exception {
        when(readingService.hasAtLeastThreeReadings(patientId)).thenReturn(true);
        when(riskService.accessRiskWithAI(patientId)).thenThrow(new RuntimeException("Test exception"));

        mockMvc.perform(get("/v1/api/risk/{patientId}/analyzeAI", patientId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error performing AI risk analysis: Test exception"));
    }
}