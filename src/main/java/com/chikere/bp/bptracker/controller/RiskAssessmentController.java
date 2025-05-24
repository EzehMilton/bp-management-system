package com.chikere.bp.bptracker.controller;

import com.chikere.bp.bptracker.exception.EntityNotFoundException;
import com.chikere.bp.bptracker.service.PatientService;
import com.chikere.bp.bptracker.service.ReadingService;
import com.chikere.bp.bptracker.service.RiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

/**
 * Controller for handling risk assessment operations
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Risk Assessment", description = "API for risk assessment of patients")
public class RiskAssessmentController {
    private final PatientService patientService;
    private final ReadingService readingService;
    private final RiskService riskService;

    /**
     * Risk assessment page
     */
    @Operation(
        summary = "Display risk assessment for a patient",
        description = "Shows risk assessment information for a patient based on their blood pressure readings. " +
                      "Optionally performs AI-based risk analysis if requested."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully displayed risk assessment",
            content = @Content(mediaType = "text/html")
        ),
        @ApiResponse(
            responseCode = "302", 
            description = "Not enough readings for risk assessment, redirects to patient details",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Patient not found",
            content = @Content
        )
    })
    @GetMapping("/patients/{patientId}/risk")
    public String riskAssessment(
            @Parameter(description = "ID of the patient to assess", required = true)
            @PathVariable UUID patientId, 
            @Parameter(description = "Whether to perform AI analysis", required = false)
            @RequestParam(required = false) Boolean analyze,
            Model model,
            RedirectAttributes redirectAttributes) {
        log.debug("Web request for risk assessment for patient with ID: {}, analyze: {}", patientId, analyze);

        // Check if patient has enough readings for risk assessment
        if (!readingService.hasAtLeastThreeReadings(patientId)) {
            log.warn("Patient with ID: {} does not have enough readings for risk assessment", patientId);
            redirectAttributes.addFlashAttribute("error", "Patient needs at least 3 readings for risk assessment");
            return "redirect:/patients/" + patientId;
        }

        model.addAttribute("patient", patientService.get(patientId));

        try {
            model.addAttribute("latestReading", readingService.getLatestReadingForPatient(patientId));
        } catch (EntityNotFoundException e) {
            // If no readings found, set latestReading to null
            log.warn("No readings found for patient with ID: {}", patientId);
            model.addAttribute("latestReading", null);
        }

        // Only perform AI analysis if explicitly requested
        if (Boolean.TRUE.equals(analyze)) {
            log.info("Performing AI risk analysis for patient with ID: {}", patientId);
            try {
                String aiRisk = riskService.accessRiskWithAI(patientId);
                log.info("AI risk analysis completed for patient with ID: {}, risk level: {}", patientId, aiRisk);
                model.addAttribute("aiRisk", aiRisk);
            } catch (Exception e) {
                log.error("Error performing AI risk analysis for patient with ID: {}", patientId, e);
                model.addAttribute("aiRiskError", e.getMessage());
            }
        }

        return "risk/assessment";
    }

    /**
     * Immediate risk capture and assessment
     */
    @Operation(
        summary = "Capture and assess immediate reading",
        description = "Captures a new blood pressure reading and performs immediate risk assessment"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "302", 
            description = "Reading captured and assessed, redirects to risk assessment page",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Patient not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Error capturing or assessing reading",
            content = @Content
        )
    })
    @PostMapping("/patients/{patientId}/risk/immediate")
    public String captureAndAssessImmediateReading(
            @Parameter(description = "ID of the patient to capture reading for", required = true)
            @PathVariable UUID patientId, 
            Model model) {
        log.debug("Web request to capture and assess immediate reading for patient with ID: {}", patientId);
        try {
            String riskLevel = riskService.captureAndAssessImmediateReading(patientId);
            log.info("Immediate risk assessment completed for patient with ID: {}, risk level: {}", 
                    patientId, riskLevel != null ? riskLevel : "NORMAL");
            model.addAttribute("immediateRisk", riskLevel != null ? riskLevel : "NORMAL");
        } catch (Exception e) {
            log.error("Error assessing immediate reading for patient with ID: {}", patientId, e);
            model.addAttribute("immediateRiskError", e.getMessage());
        }
        return "redirect:/patients/" + patientId + "/risk";
    }
}