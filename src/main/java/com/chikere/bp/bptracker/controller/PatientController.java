package com.chikere.bp.bptracker.controller;

import com.chikere.bp.bptracker.dto.NewPatientDTO;
import com.chikere.bp.bptracker.dto.PatientDTO;
import com.chikere.bp.bptracker.mapper.PatientMapper;
import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.service.PatientService;
import com.chikere.bp.bptracker.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for managing patient-related operations.
 * Handles requests for creating, retrieving, searching, and managing patient records.
 */
@RestController
@RequestMapping("v1/api/patient")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;
    private final PatientMapper patientMapper;

    /**
     * Create a new patient
     */
    @PostMapping
    public ResponseEntity<PatientDTO> addPatient(@RequestBody NewPatientDTO newPatientDTO) {
        // Convert DTO → JPA entity and persist the new patient
        Patient saved = patientService.createPatient(patientMapper.toEntity(newPatientDTO));
        // Convert saved entity → DTO and return 201 Created response
        PatientDTO resultDto = patientMapper.toDto(saved);
        return ResponseUtil.created(resultDto, saved.getId());
    }

    /**
     * Get all patients
     */
    @GetMapping
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        List<PatientDTO> dtos = patientService.findAll().stream()
                .map(patientMapper::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    /** Search patients by name (case-insensitive contains) **/
    @GetMapping("/search")
    public ResponseEntity<List<PatientDTO>> searchPatients(
            @RequestParam("name") String name
    ) {
        List<PatientDTO> dtos = patientService.search(name).stream()
                .map(patientMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get a patient by ID
     * 
     * @param id The patient ID
     * @return The patient DTO or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatient(@PathVariable("id") UUID id) {
        try {
            // This will throw if not found, triggering the global exception handler
            Patient patient = patientService.get(id);
            return ResponseEntity.ok(patientMapper.toDto(patient));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}
