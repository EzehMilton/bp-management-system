package com.chikere.bp.bptracker.controller;

import com.chikere.bp.bptracker.dto.NewReadingDto;
import com.chikere.bp.bptracker.dto.ReadingDto;
import com.chikere.bp.bptracker.exception.EntityNotFoundException;
import com.chikere.bp.bptracker.model.enums.Arm;
import com.chikere.bp.bptracker.model.enums.BodyPosition;
import com.chikere.bp.bptracker.service.PatientService;
import com.chikere.bp.bptracker.service.ReadingService;
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

import java.util.UUID;

/**
 * Controller for handling reading-related operations
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Readings", description = "API for managing blood pressure readings")
public class ReadingController {
    public static final String READING = "reading";
    private final ReadingService readingService;
    private final PatientService patientService;

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
        model.addAttribute(READING, readingService.getById(id));
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
        model.addAttribute(READING, new NewReadingDto());
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
        model.addAttribute(READING, reading);
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
}