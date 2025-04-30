package com.chikere.bp.bptracker.service;

import com.chikere.bp.bptracker.dto.NewReadingDto;
import com.chikere.bp.bptracker.dto.ReadingDto;
import com.chikere.bp.bptracker.exception.EntityNotFoundException;
import com.chikere.bp.bptracker.mapper.ReadingMapper;
import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.Reading;
import com.chikere.bp.bptracker.repository.PatientRepository;
import com.chikere.bp.bptracker.repository.ReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReadingService {
    private final ReadingRepository readingRepository;
    private final PatientRepository patientRepository;
    private final ReadingMapper readingMapper;

    /**
     * Create a new reading from DTO
     */
    public ReadingDto create(NewReadingDto newReadingDto) {
        // Find the patient
        Patient patient = patientRepository.findById(newReadingDto.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with ID: " + newReadingDto.getPatientId()));

        // Convert DTO to entity and set patient
        Reading reading = readingMapper.toEntity(newReadingDto);
        reading.setPatient(patient);

        // Save and return as DTO
        Reading saved = readingRepository.save(reading);
        return readingMapper.toDto(saved);
    }

    /**
     * Update an existing reading
     */
    public ReadingDto update(UUID id, ReadingDto readingDto) {
        // Verify reading exists
        if (!readingRepository.existsById(id)) {
            throw new EntityNotFoundException("Reading not found with ID: " + id);
        }

        // Set ID and convert to entity
        readingDto.setId(id);
        Reading reading = readingMapper.toEntity(readingDto);

        // Find patient and set it
        Patient patient = patientRepository.findById(readingDto.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with ID: " + readingDto.getPatientId()));
        reading.setPatient(patient);

        // Save and return as DTO
        Reading updated = readingRepository.save(reading);
        return readingMapper.toDto(updated);
    }

    /**
     * Get a reading by ID
     */
    public ReadingDto getById(UUID id) {
        Reading reading = readingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reading not found with ID: " + id));
        return readingMapper.toDto(reading);
    }

    /**
     * Get all readings
     */
    public List<ReadingDto> findAll() {
        return readingRepository.findAll().stream()
                .map(readingMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Delete a reading
     */
    public void delete(UUID id) {
        if (!readingRepository.existsById(id)) {
            throw new EntityNotFoundException("Reading not found with ID: " + id);
        }
        readingRepository.deleteById(id);
    }

    /**
     * Get recent readings for a patient
     */
    public List<ReadingDto> getRecentReadingsForPatient(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with ID: " + patientId));

        return readingRepository.findTop3ByPatientOrderByTimestampDesc(patient).stream()
                .map(readingMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get the most recent reading for a patient
     */
    public ReadingDto getLatestReadingForPatient(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with ID: " + patientId));

        Reading reading = readingRepository.findFirstByPatientOrderByTimestampDesc(patient)
                .orElseThrow(() -> new EntityNotFoundException("No readings found for patient with ID: " + patientId));

        return readingMapper.toDto(reading);
    }

    /**
     * Check if a patient has at least 3 readings
     */
    public boolean hasAtLeastThreeReadings(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with ID: " + patientId));

        return readingRepository.countByPatient(patient) >= 3;
    }
}
