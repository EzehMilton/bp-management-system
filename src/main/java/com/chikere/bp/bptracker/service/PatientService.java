package com.chikere.bp.bptracker.service;

import com.chikere.bp.bptracker.exception.EntityNotFoundException;
import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class PatientService {
    private final PatientRepository patientRepository;

    public Patient createPatient(Patient patient){
        // this should be used to create a new patient
        log.debug("Creating new patient: {}", patient);
        Patient savedPatient = patientRepository.save(patient);
        log.info("Patient created successfully with ID: {}", savedPatient.getId());
        return savedPatient;
    }

    public Patient findById(UUID id){
        // this should be used to find a patient by their ID
        log.debug("Finding patient by ID: {}", id);
        Patient patient = patientRepository.findById(id).orElse(null);
        if (patient == null) {
            log.debug("No patient found with ID: {}", id);
        } else {
            log.debug("Found patient with ID: {}", id);
        }
        return patient;
    }

    public List<Patient> search(String name) {
        // this should be used to search for a patient by their name
        return patientRepository.findByFullNameContainingIgnoreCase(name);
    }

    public Patient updatePatient(UUID id, Patient patient) {
        // this should be used to update a patient's information
        patient.setId(id);
        return patientRepository.save(patient);
    }

    public List<Patient> findAll() {
        // this
        return patientRepository.findAll();
    }

    public void deletePatient(UUID id) {
        // this should be used to delete a patient
        patientRepository.deleteById(id);
    }

    public void deleteAllPatients() {
        // this should be used to delete all patients
        patientRepository.deleteAll();
    }

    public Patient get(UUID id) {
        log.debug("Getting patient with ID: {}", id);
        try {
            Patient patient = patientRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found with ID: " + id));
            log.debug("Found patient with ID: {}", id);
            return patient;
        } catch (EntityNotFoundException e) {
            log.error("Failed to find patient with ID: {}", id, e);
            throw e;
        }
    }

}
