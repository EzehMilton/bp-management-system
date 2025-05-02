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
 * Controller for handling web views using Thymeleaf templates.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebController {
    private final PatientService patientService;
    private final ReadingService readingService;
    private final RiskService riskService;
    private final PatientMapper patientMapper;

    /**
     * Home page / dashboard
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("patients", patientService.findAll());
        return "index";
    }

    /**
     * Patient list page
     */
    @GetMapping("/patients")
    public String listPatients(Model model) {
        model.addAttribute("patients", patientService.findAll());
        return "patients/list";
    }

    /**
     * Patient details page
     */
    @GetMapping("/patients/{id}")
    public String viewPatient(@PathVariable UUID id, Model model) {
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
    @GetMapping("/patients/new")
    public String newPatientForm(Model model) {
        model.addAttribute("patient", new NewPatientDTO());
        model.addAttribute("genders", Gender.values());
        return "patients/new";
    }

    /**
     * Create new patient
     */
    @PostMapping("/patients/new")
    public String createPatient(@ModelAttribute NewPatientDTO patient) {
        log.debug("Web request to create new patient: {}", patient);
        Patient patientEntity = patientMapper.toEntity(patient);
        Patient savedPatient = patientService.createPatient(patientEntity);
        log.info("Patient created successfully with ID: {}", savedPatient.getId());
        return "redirect:/";
    }

    /**
     * Edit patient form
     */
    @GetMapping("/patients/{id}/edit")
    public String editPatientForm(@PathVariable UUID id, Model model) {
        Patient patient = patientService.get(id);
        PatientDTO patientDTO = patientMapper.toDto(patient);
        model.addAttribute("patient", patientDTO);
        model.addAttribute("genders", Gender.values());
        return "patients/edit";
    }

    /**
     * Update patient
     */
    @PostMapping("/patients/{id}/edit")
    public String updatePatient(@PathVariable UUID id, @ModelAttribute PatientDTO patient, RedirectAttributes redirectAttributes) {
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
    @PostMapping("/patients/{id}/delete")
    public String deletePatient(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        log.debug("Web request to delete patient with ID: {}", id);
        patientService.deletePatient(id);
        log.info("Patient deleted successfully with ID: {}", id);
        redirectAttributes.addFlashAttribute("success", "Patient deleted successfully");
        return "redirect:/patients";
    }

    /**
     * Search patients
     */
    @GetMapping("/patients/search")
    public String searchPatients(@RequestParam String name, Model model) {
        model.addAttribute("patients", patientService.search(name));
        model.addAttribute("searchTerm", name);
        return "patients/list";
    }

    /**
     * Readings list page
     */
    @GetMapping("/readings")
    public String listReadings(Model model) {
        model.addAttribute("readings", readingService.findAll());
        return "readings/list";
    }

    /**
     * Reading details page
     */
    @GetMapping("/readings/{id}")
    public String viewReading(@PathVariable UUID id, Model model) {
        model.addAttribute("reading", readingService.getById(id));
        return "readings/view";
    }

    /**
     * New reading form
     */
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
    @GetMapping("/patients/{patientId}/readings/new")
    public String newReadingForPatient(@PathVariable UUID patientId, Model model) {
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
    @PostMapping("/readings/new")
    public String createReading(@ModelAttribute NewReadingDto reading) {
        log.debug("Web request to create new reading: {}", reading);
        ReadingDto savedReading = readingService.create(reading);
        log.info("Reading created successfully with ID: {} for patient ID: {}", 
                savedReading.getId(), reading.getPatientId());
        return "redirect:/readings/" + savedReading.getId();
    }

    /**
     * Risk assessment page
     */
    @GetMapping("/patients/{patientId}/risk")
    public String riskAssessment(@PathVariable UUID patientId, 
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
    @PostMapping("/patients/{patientId}/risk/immediate")
    public String captureAndAssessImmediateReading(@PathVariable UUID patientId, Model model) {
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
