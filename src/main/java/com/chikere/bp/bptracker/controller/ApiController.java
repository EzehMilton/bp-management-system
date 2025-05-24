package com.chikere.bp.bptracker.controller;

import com.chikere.bp.bptracker.service.ReadingService;
import com.chikere.bp.bptracker.service.RiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for handling API endpoints
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "API", description = "API endpoints for AI Analysis")
public class ApiController {
    private final ReadingService readingService;
    private final RiskService riskService;

    /**
     * API endpoint for AI risk analysis
     */
    @Operation(
        summary = "Analyze patient risk with AI",
        description = "Performs an AI-based risk assessment for a patient based on their blood pressure readings. " +
                      "Requires at least 3 readings for the patient."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Risk assessment successful",
            content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Patient does not have enough readings",
            content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Error performing risk analysis",
            content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
        )
    })
    @GetMapping("/v1/api/risk/{patientId}/analyzeAI")
    public ResponseEntity<String> analyzeRiskWithAI(
            @Parameter(description = "ID of the patient to analyze", required = true)
            @PathVariable UUID patientId) {
        log.debug("API request to analyze risk with AI for patient with ID: {}", patientId);
        try {
            // Check if a patient has enough readings for AI risk assessment
            if (!readingService.hasAtLeastThreeReadings(patientId)) {
                log.warn("Patient with ID: {} does not have enough readings for AI risk assessment", patientId);
                return ResponseEntity.badRequest().body("Patient needs at least 3 readings for AI risk assessment");
            }

            String aiRisk = riskService.accessRiskWithAI(patientId);
            log.info("AI risk analysis completed for patient with ID: {}, risk level: {}", patientId, aiRisk);
            return ResponseEntity.ok(aiRisk);
        } catch (Exception e) {
            log.error("Error performing AI risk analysis for patient with ID: {}", patientId, e);
            return ResponseEntity.status(500).body("Error performing AI risk analysis: " + e.getMessage());
        }
    }
}