package com.chikere.bp.bptracker.controller;

import com.chikere.bp.bptracker.dto.NewPatientDTO;
import com.chikere.bp.bptracker.dto.NewReadingDto;
import com.chikere.bp.bptracker.dto.PatientDTO;
import com.chikere.bp.bptracker.dto.ReadingDto;
import com.chikere.bp.bptracker.exception.EntityNotFoundException;
import com.chikere.bp.bptracker.mapper.PatientMapper;
import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.enums.Arm;
import com.chikere.bp.bptracker.model.enums.BodyPosition;
import com.chikere.bp.bptracker.model.enums.Gender;
import com.chikere.bp.bptracker.service.PatientService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

/**
 * Controller for handling web views using Thymeleaf templates and API endpoints.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Blood Pressure Tracker", description = "API for managing patients, readings, and risk assessments")
public class WebController {
    private final PatientService patientService;
    private final ReadingService readingService;
    private final RiskService riskService;
    private final PatientMapper patientMapper;

    /**
     * Home page / dashboard
     */
    @Operation(
        summary = "Home page",
        description = "Displays the dashboard with a list of all patients"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully displayed home page",
            content = @Content(mediaType = "text/html")
        )
    })
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("patients", patientService.findAll());
        return "index";
    }

    /**
     * Patient list page
     */
    @Operation(
        summary = "List all patients",
        description = "Displays a page with a list of all patients in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully displayed patient list",
            content = @Content(mediaType = "text/html")
        )
    })
    @GetMapping("/patients")
    public String listPatients(Model model) {
        model.addAttribute("patients", patientService.findAll());
        return "patients/list";
    }

    /**
     * Patient details page
     */
    @Operation(
        summary = "View patient details",
        description = "Displays detailed information about a specific patient including their recent readings"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully displayed patient details",
            content = @Content(mediaType = "text/html")
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Patient not found",
            content = @Content
        )
    })
    @GetMapping("/patients/{id}")
    public String viewPatient(
            @Parameter(description = "ID of the patient to view", required = true)
            @PathVariable UUID id, Model model) {
        model.addAttribute("patient", patientService.get(id));
        model.addAttribute("readings", readingService.getRecentReadingsForPatient(id));

        try {
            model.addAttribute("latestReading", readingService.getLatestReadingForPatient(id));
        } catch (EntityNotFoundException e) {
            // If no readings found, set latestReading to null
            model.addAttribute("latestReading", null);
        }

        // Check if patient has at least 3 readings for risk assessment
        model.addAttribute("hasEnoughReadingsForRisk", readingService.hasAtLeastThreeReadings(id));

        // Check if patient has any readings for CSV download button
        model.addAttribute("hasReadings", readingService.hasReadings(id));

        return "patients/view";
    }

    /**
     * New patient form
     */
    @Operation(
        summary = "Display new patient form",
        description = "Shows a form for creating a new patient"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully displayed new patient form",
            content = @Content(mediaType = "text/html")
        )
    })
    @GetMapping("/patients/new")
    public String newPatientForm(Model model) {
        model.addAttribute("patient", new NewPatientDTO());
        model.addAttribute("genders", Gender.values());
        return "patients/new";
    }

    /**
     * Create new patient
     */
    @Operation(
        summary = "Create a new patient",
        description = "Creates a new patient with the provided information"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "302", 
            description = "Patient created successfully, redirects to home page",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid patient data",
            content = @Content
        )
    })
    @PostMapping("/patients/new")
    public String createPatient(
            @Parameter(description = "Patient information", required = true)
            @ModelAttribute NewPatientDTO patient) {
        log.debug("Web request to create new patient: {}", patient);
        Patient patientEntity = patientMapper.toEntity(patient);
        Patient savedPatient = patientService.createPatient(patientEntity);
        log.info("Patient created successfully with ID: {}", savedPatient.getId());
        return "redirect:/";
    }

    /**
     * Edit patient form
     */
    @Operation(
        summary = "Display edit patient form",
        description = "Shows a form for editing an existing patient's information"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully displayed edit patient form",
            content = @Content(mediaType = "text/html")
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Patient not found",
            content = @Content
        )
    })
    @GetMapping("/patients/{id}/edit")
    public String editPatientForm(
            @Parameter(description = "ID of the patient to edit", required = true)
            @PathVariable UUID id, Model model) {
        Patient patient = patientService.get(id);
        PatientDTO patientDTO = patientMapper.toDto(patient);
        model.addAttribute("patient", patientDTO);
        model.addAttribute("genders", Gender.values());
        return "patients/edit";
    }

    /**
     * Update patient
     */
    @Operation(
        summary = "Update patient information",
        description = "Updates an existing patient's information with the provided data"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "302", 
            description = "Patient updated successfully, redirects to patient details page",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Patient not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid patient data",
            content = @Content
        )
    })
    @PostMapping("/patients/{id}/edit")
    public String updatePatient(
            @Parameter(description = "ID of the patient to update", required = true)
            @PathVariable UUID id, 
            @Parameter(description = "Updated patient information", required = true)
            @ModelAttribute PatientDTO patient, 
            RedirectAttributes redirectAttributes) {
        log.debug("Web request to update patient with ID: {}, data: {}", id, patient);
        patient.setId(id); // Ensure ID is set
        Patient patientEntity = patientMapper.toEntity(patient);
        patientService.updatePatient(id, patientEntity);
        log.info("Patient updated successfully with ID: {}", id);
        redirectAttributes.addFlashAttribute("success", "Patient updated successfully");
        return "redirect:/patients/" + id;
    }

    /**
     * Delete patient
     */
    @Operation(
        summary = "Delete a patient",
        description = "Permanently removes a patient and all their associated data from the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "302", 
            description = "Patient deleted successfully, redirects to patient list",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Patient not found",
            content = @Content
        )
    })
    @PostMapping("/patients/{id}/delete")
    public String deletePatient(
            @Parameter(description = "ID of the patient to delete", required = true)
            @PathVariable UUID id, 
            RedirectAttributes redirectAttributes) {
        log.debug("Web request to delete patient with ID: {}", id);
        patientService.deletePatient(id);
        log.info("Patient deleted successfully with ID: {}", id);
        redirectAttributes.addFlashAttribute("success", "Patient deleted successfully");
        return "redirect:/patients";
    }

    /**
     * Search patients
     */
    @Operation(
        summary = "Search for patients",
        description = "Searches for patients by name and displays matching results"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Search completed successfully",
            content = @Content(mediaType = "text/html")
        )
    })
    @GetMapping("/patients/search")
    public String searchPatients(
            @Parameter(description = "Name to search for", required = true)
            @RequestParam String name, 
            Model model) {
        model.addAttribute("patients", patientService.search(name));
        model.addAttribute("searchTerm", name);
        return "patients/list";
    }

    /**
     * Readings list page
     */
    @Operation(
        summary = "List all readings",
        description = "Displays a page with a list of all blood pressure readings in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully displayed readings list",
            content = @Content(mediaType = "text/html")
        )
    })
    @GetMapping("/readings")
    public String listReadings(Model model) {
        model.addAttribute("readings", readingService.findAll());
        return "readings/list";
    }

    /**
     * Reading details page
     */
    @Operation(
        summary = "View reading details",
        description = "Displays detailed information about a specific blood pressure reading"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully displayed reading details",
            content = @Content(mediaType = "text/html")
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Reading not found",
            content = @Content
        )
    })
    @GetMapping("/readings/{id}")
    public String viewReading(
            @Parameter(description = "ID of the reading to view", required = true)
            @PathVariable UUID id, 
            Model model) {
        model.addAttribute("reading", readingService.getById(id));
        return "readings/view";
    }

    /**
     * New reading form
     */
    @Operation(
        summary = "Display new reading form",
        description = "Shows a form for creating a new blood pressure reading"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully displayed new reading form",
            content = @Content(mediaType = "text/html")
        )
    })
    @GetMapping("/readings/new")
    public String newReadingForm(Model model) {
        model.addAttribute("reading", new NewReadingDto());
        model.addAttribute("patients", patientService.findAll());
        model.addAttribute("bodyPositions", BodyPosition.values());
        model.addAttribute("arms", Arm.values());
        return "readings/new";
    }

    /**
     * New reading form for a specific patient
     */
    @Operation(
        summary = "Display new reading form for a specific patient",
        description = "Shows a form for creating a new blood pressure reading for a specific patient"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully displayed new reading form",
            content = @Content(mediaType = "text/html")
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Patient not found",
            content = @Content
        )
    })
    @GetMapping("/patients/{patientId}/readings/new")
    public String newReadingForPatient(
            @Parameter(description = "ID of the patient to create a reading for", required = true)
            @PathVariable UUID patientId, 
            Model model) {
        NewReadingDto reading = new NewReadingDto();
        reading.setPatientId(patientId);
        model.addAttribute("reading", reading);
        model.addAttribute("patient", patientService.get(patientId));
        model.addAttribute("bodyPositions", BodyPosition.values());
        model.addAttribute("arms", Arm.values());
        return "readings/new";
    }

    /**
     * Create new reading
     */
    @Operation(
        summary = "Create a new reading",
        description = "Creates a new blood pressure reading with the provided information"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "302", 
            description = "Reading created successfully, redirects to reading details page",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid reading data",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Patient not found",
            content = @Content
        )
    })
    @PostMapping("/readings/new")
    public String createReading(
            @Parameter(description = "Reading information", required = true)
            @ModelAttribute NewReadingDto reading) {
        log.debug("Web request to create new reading: {}", reading);
        ReadingDto savedReading = readingService.create(reading);
        log.info("Reading created successfully with ID: {} for patient ID: {}", 
                savedReading.getId(), reading.getPatientId());
        return "redirect:/readings/" + savedReading.getId();
    }

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

    /**
     * Download all readings as CSV
     */
    @Operation(
        summary = "Download all readings as CSV",
        description = "Exports all blood pressure readings in the system as a CSV file"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "CSV file generated successfully",
            content = @Content(mediaType = "text/csv", schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "204", 
            description = "No readings found to export",
            content = @Content
        )
    })
    @GetMapping("/readings/download-csv")
    public ResponseEntity<String> downloadAllReadingsAsCsv() {
        log.debug("Web request to download all readings as CSV");
        try {
            String csvContent = readingService.getAllReadingsAsCsv();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "all_readings.csv");

            log.info("Generated CSV for all readings");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvContent);
        } catch (EntityNotFoundException e) {
            log.warn("No readings found for CSV download");
            return ResponseEntity.noContent().build();
        }
    }

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
    @ResponseBody
    public ResponseEntity<String> analyzeRiskWithAI(
            @Parameter(description = "ID of the patient to analyze", required = true)
            @PathVariable UUID patientId) {
        log.debug("API request to analyze risk with AI for patient with ID: {}", patientId);
        try {
            // Check if patient has enough readings for risk assessment
            if (!readingService.hasAtLeastThreeReadings(patientId)) {
                log.warn("Patient with ID: {} does not have enough readings for risk assessment", patientId);
                return ResponseEntity.badRequest().body("Patient needs at least 3 readings for risk assessment");
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
