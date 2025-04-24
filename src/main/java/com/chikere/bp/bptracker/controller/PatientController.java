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
    private final PatientService patientService;
    private final PatientMapper patientMapper;

    @PostMapping
    public ResponseEntity<PatientDTO> addPatient(@RequestBody NewPatientDTO newPatientDTO) {

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
