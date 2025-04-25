package com.chikere.bp.bptracker.service;

import com.chikere.bp.bptracker.exception.EntityNotFoundException;
import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PatientService {
    private final PatientRepository patientRepository;

    public Patient createPatient(Patient patient){
        // this should be used to create a new patient
        return patientRepository.save(patient);
    }

    public Patient findById(UUID id){
        // this should be used to find a patient by their ID
        return patientRepository.findById(id).orElse(null);
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
        return patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with ID: " + id));
    }

}
