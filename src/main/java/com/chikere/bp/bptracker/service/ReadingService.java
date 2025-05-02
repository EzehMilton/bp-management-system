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

import java.time.format.DateTimeFormatter;
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

    /**
     * Get all readings for a patient as CSV
     */
    public String getAllReadingsForPatientAsCsv(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with ID: " + patientId));

        List<Reading> readings = readingRepository.findAllByPatientOrderByTimestampDesc(patient);

        if (readings.isEmpty()) {
            throw new EntityNotFoundException("No readings found for patient with ID: " + patientId);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        StringBuilder csv = new StringBuilder();
        // Add CSV header
        csv.append("Date,Time,Systolic,Diastolic,Heart Rate,Body Position,Arm,Notes,Device ID\n");

        // Add readings data
        for (Reading reading : readings) {
            csv.append(reading.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append(",");
            csv.append(reading.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append(",");
            csv.append(reading.getSystolic()).append(",");
            csv.append(reading.getDiastolic()).append(",");
            csv.append(reading.getHeartRate()).append(",");
            csv.append(reading.getBodyPosition()).append(",");
            csv.append(reading.getArm()).append(",");
            csv.append(reading.getNotes() != null ? "\"" + reading.getNotes().replace("\"", "\"\"") + "\"" : "").append(",");
            csv.append(reading.getDeviceId() != null ? reading.getDeviceId() : "").append("\n");
        }

        return csv.toString();
    }

    /**
     * Check if a patient has any readings
     */
    public boolean hasReadings(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with ID: " + patientId));

        return readingRepository.countByPatient(patient) > 0;
    }

    /**
     * Get all readings as CSV, including patient information
     */
    public String getAllReadingsAsCsv() {
        List<Reading> readings = readingRepository.findAll();

        if (readings.isEmpty()) {
            throw new EntityNotFoundException("No readings found");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        StringBuilder csv = new StringBuilder();
        // Add CSV header with patient information
        csv.append("Date,Time,Patient Name,Systolic,Diastolic,Heart Rate,Body Position,Arm,Notes,Device ID\n");

        // Add readings data
        for (Reading reading : readings) {
            csv.append(reading.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append(",");
            csv.append(reading.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append(",");
            csv.append("\"").append(reading.getPatient().getFullName().replace("\"", "\"\"")).append("\"").append(",");
            csv.append(reading.getSystolic()).append(",");
            csv.append(reading.getDiastolic()).append(",");
            csv.append(reading.getHeartRate()).append(",");
            csv.append(reading.getBodyPosition()).append(",");
            csv.append(reading.getArm()).append(",");
            csv.append(reading.getNotes() != null ? "\"" + reading.getNotes().replace("\"", "\"\"") + "\"" : "").append(",");
            csv.append(reading.getDeviceId() != null ? reading.getDeviceId() : "").append("\n");
        }

        return csv.toString();
    }
}
