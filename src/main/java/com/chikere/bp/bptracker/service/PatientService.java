package com.chikere.bp.bptracker.service;

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
        return patientRepository.save(patient);
    }

    public Patient findById(UUID id){
        return patientRepository.findById(id).orElse(null);
    }

    public List<Patient> search(String name) {
        return patientRepository.findByFullNameContainingIgnoreCase(name);
    }

    public Patient get(UUID id) {
        return patientRepository.findById(id).orElseThrow();
    }

    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

}
