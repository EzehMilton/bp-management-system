package com.chikere.bp.bptracker.repository;

import com.chikere.bp.bptracker.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {
    List<Patient> findByFullNameContainingIgnoreCase(String fullName);
}
