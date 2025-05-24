package com.chikere.bp.bptracker.controller;

import com.chikere.bp.bptracker.dto.NewPatientDTO;
import com.chikere.bp.bptracker.dto.PatientDTO;
import com.chikere.bp.bptracker.exception.EntityNotFoundException;
import com.chikere.bp.bptracker.mapper.PatientMapper;
import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.enums.Gender;
import com.chikere.bp.bptracker.service.PatientService;
import com.chikere.bp.bptracker.service.ReadingService;
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
 * Controller for handling patient-related operations
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Patients", description = "API for managing patients")
public class PatientController {
    public static final String PATIENTS = "patients";
    public static final String PATIENT = "patient";
    private final PatientService patientService;
    private final ReadingService readingService;
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
        model.addAttribute(PATIENTS, patientService.findAll());
        return "index";
    }

    /**
     *
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
        model.addAttribute(PATIENTS, patientService.findAll());
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
        model.addAttribute(PATIENT, patientService.get(id));
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
        model.addAttribute(PATIENT, new NewPatientDTO());
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
        model.addAttribute(PATIENT, patientDTO);
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
        model.addAttribute(PATIENTS, patientService.search(name));
        model.addAttribute("searchTerm", name);
        return "patients/list";
    }
}