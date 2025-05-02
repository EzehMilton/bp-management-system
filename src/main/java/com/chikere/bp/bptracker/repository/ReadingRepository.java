package com.chikere.bp.bptracker.repository;

import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.Reading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadingRepository extends JpaRepository<Reading, UUID> {
    List<Reading> findTop3ByPatientOrderByTimestampDesc(Patient patient);
    Optional<Reading> findFirstByPatientOrderByTimestampDesc(Patient patient);
    int countByPatient(Patient patient);
    List<Reading> findAllByPatientOrderByTimestampDesc(Patient patient);
}
