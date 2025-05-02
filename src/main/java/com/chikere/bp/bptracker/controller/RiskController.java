package com.chikere.bp.bptracker.controller;

import com.chikere.bp.bptracker.exception.EntityNotFoundException;
import com.chikere.bp.bptracker.service.RiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/risk")
@Slf4j
public class RiskController {
    private final RiskService riskService;

    /**
     * Handles the immediate capture and assessment of a patient's blood pressure reading.
     *
     * @param patientId The unique identifier of the patient (UUID).
     * @return A ResponseEntity containing:
     *         - The risk level as a string ("NORMAL" if no risk level is determined).
     *         - A bad request response with an error message if the patient is not found or no readings exist.
     * @throws EntityNotFoundException If the patient with the given ID does not exist or no readings are found.
     */
    @PostMapping("/{patientId}/immediate")
    public ResponseEntity<String> captureAndAssessImmediateReading(
            @PathVariable UUID patientId) {
        log.debug("REST request to capture and assess immediate reading for patient with ID: {}", patientId);
        try {
            String riskLevel = riskService.captureAndAssessImmediateReading(patientId);
            log.info("Immediate risk assessment completed for patient with ID: {}, risk level: {}", 
                    patientId, riskLevel != null ? riskLevel : "NORMAL");
            return ResponseEntity.ok(riskLevel != null ? riskLevel : "NORMAL");
        } catch (EntityNotFoundException e) {
            log.error("Failed to assess immediate reading for patient with ID: {}", patientId, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Performs an AI-based risk analysis for a patient.
     *
     * @param patientId The unique identifier of the patient (UUID).
     * @return A ResponseEntity containing:
     *         - The risk level as a string if the analysis is successful.
     *         - A bad request response with an error message if the patient is not found.
     * @throws EntityNotFoundException If the patient with the given ID does not exist.
     */
    @GetMapping("/{patientId}/analyzeAI")
    public ResponseEntity<String> riskCheckWithAi(@PathVariable UUID patientId) {
        log.debug("REST request to perform AI-based risk analysis for patient with ID: {}", patientId);
        try {
            String riskLevelString = riskService.accessRiskWithAI(patientId);
            RiskService.RiskLevel level = RiskService.RiskLevel.valueOf(riskLevelString);
            log.info("AI-based risk analysis completed for patient with ID: {}, risk level: {}", patientId, level.name());
            return ResponseEntity.ok(level.name());
        } catch (EntityNotFoundException e) {
            log.error("Failed to perform AI-based risk analysis for patient with ID: {}", patientId, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
