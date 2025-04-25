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

@RestController
@RequestMapping("v1/api/patient")
@RequiredArgsConstructor
public class PatientController {
    // TODO Review this controller and remove things that are not needed
    private final PatientService patientService;
    private final PatientMapper patientMapper;

    @PostMapping
    public ResponseEntity<PatientDTO> addPatient(@RequestBody NewPatientDTO newPatientDTO) {

        /**
         * FLOW
         *
         * Request Body to DTO: The incoming JSON payload from the request body is deserialized into a NewPatientDTO object using the @RequestBody annotation.
         *
         *
         * DTO to Entity: The NewPatientDTO is mapped to a Patient entity using patientMapper.toEntity(newPatientDTO).
         *
         *
         * Entity Saved: The Patient entity is persisted to the database using patientService.createPatient(...).
         *
         *
         * Entity to DTO: The saved Patient entity is converted back to a PatientDTO using patientMapper.toDto(saved).
         *
         *
         * Response: A 201 Created response is returned with the PatientDTO in the body and a Location header pointing to the resource's URI.
         */

        // 1. Convert DTO → JPA entity and Persist the new patient
        Patient saved = patientService.createPatient(patientMapper.toEntity(newPatientDTO));
        // 2. Convert saved entity → DTO (includes id, registeredAt, etc.)
        PatientDTO resultDto = patientMapper.toDto(saved);
        // 3. Return 201 Created with Location header and body
        return ResponseUtil.created(resultDto, saved.getId());
    }

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

    /** Get one patient by ID **/
    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatient(
            @PathVariable("id") UUID id
    ) {
        // will throw if not found, triggering your global exception handler
        Patient p = patientService.get(id);
        return ResponseEntity.ok(patientMapper.toDto(p));
    }

    /** Get one patient by ID (returns null if not found) **/
    @GetMapping("/find/{id}")
    public ResponseEntity<PatientDTO> findPatientById(@PathVariable("id") UUID id) {
        Patient patient = patientService.findById(id);
        if (patient == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(patientMapper.toDto(patient));
    }

}
