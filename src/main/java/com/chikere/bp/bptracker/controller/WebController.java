package com.chikere.bp.bptracker.controller;

import com.chikere.bp.bptracker.dto.NewPatientDTO;
import com.chikere.bp.bptracker.dto.NewReadingDto;
import com.chikere.bp.bptracker.dto.PatientDTO;
import com.chikere.bp.bptracker.dto.ReadingDto;
import com.chikere.bp.bptracker.mapper.PatientMapper;
import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.enums.Arm;
import com.chikere.bp.bptracker.model.enums.BodyPosition;
import com.chikere.bp.bptracker.model.enums.Gender;
import com.chikere.bp.bptracker.service.PatientService;
import com.chikere.bp.bptracker.service.ReadingService;
import com.chikere.bp.bptracker.service.RiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for handling web views using Thymeleaf templates.
 */
@Controller
@RequiredArgsConstructor
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
        model.addAttribute("latestReading", readingService.getLatestReadingForPatient(id));
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
        Patient patientEntity = patientMapper.toEntity(patient);
        Patient savedPatient = patientService.createPatient(patientEntity);
        return "redirect:/patients/" + savedPatient.getId();
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
        ReadingDto savedReading = readingService.create(reading);
        return "redirect:/readings/" + savedReading.getId();
    }

    /**
     * Risk assessment page
     */
    @GetMapping("/patients/{patientId}/risk")
    public String riskAssessment(@PathVariable UUID patientId, 
                                @RequestParam(required = false) Boolean analyze,
                                Model model) {
        model.addAttribute("patient", patientService.get(patientId));
        model.addAttribute("latestReading", readingService.getLatestReadingForPatient(patientId));

        // Only perform AI analysis if explicitly requested
        if (Boolean.TRUE.equals(analyze)) {
            try {
                String aiRisk = riskService.accessRiskWithAI(patientId);
                model.addAttribute("aiRisk", aiRisk);
            } catch (Exception e) {
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
        try {
            String riskLevel = riskService.captureAndAssessImmediateReading(patientId);
            model.addAttribute("immediateRisk", riskLevel != null ? riskLevel : "NORMAL");
        } catch (Exception e) {
            model.addAttribute("immediateRiskError", e.getMessage());
        }
        return "redirect:/patients/" + patientId + "/risk";
    }
}
