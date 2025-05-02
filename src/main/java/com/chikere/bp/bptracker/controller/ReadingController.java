package com.chikere.bp.bptracker.controller;

import com.chikere.bp.bptracker.dto.NewReadingDto;
import com.chikere.bp.bptracker.dto.ReadingDto;
import com.chikere.bp.bptracker.service.ReadingService;
import com.chikere.bp.bptracker.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


/**
 * Controller for managing reading-related operations.
 * Handles requests for creating, updating, retrieving, searching, deleting and managing BP readings for patients.
 */
@RestController
@RequestMapping("v1/api/reading")
@RequiredArgsConstructor
@Slf4j
public class ReadingController {
    private final ReadingService readingService;

    /**
     * Create a new reading
     */
    @PostMapping
    public ResponseEntity<ReadingDto> create(@RequestBody NewReadingDto newReadingDto) {
        log.debug("REST request to create reading: {}", newReadingDto);
        ReadingDto created = readingService.create(newReadingDto);
        log.info("Reading created successfully with ID: {}", created.getId());
        return ResponseUtil.created(created, created.getId());
    }

    /**
     * Update an existing reading
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReadingDto> update(@PathVariable UUID id, @RequestBody ReadingDto readingDto) {
        log.debug("REST request to update reading with ID: {}, data: {}", id, readingDto);
        ReadingDto updated = readingService.update(id, readingDto);
        log.info("Reading updated successfully with ID: {}", id);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get a reading by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReadingDto> getById(@PathVariable UUID id) {
        log.debug("REST request to get reading with ID: {}", id);
        ReadingDto reading = readingService.getById(id);
        log.info("Retrieved reading with ID: {}", id);
        return ResponseEntity.ok(reading);
    }

    /**
     * Get all readings
     */
    @GetMapping
    public ResponseEntity<List<ReadingDto>> getAll() {
        log.debug("REST request to get all readings");
        List<ReadingDto> readings = readingService.findAll();
        log.info("Retrieved {} readings", readings.size());
        return ResponseEntity.ok(readings);
    }

    /**
     * Delete a reading
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.debug("REST request to delete reading with ID: {}", id);
        readingService.delete(id);
        log.info("Reading deleted successfully with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get recent readings for a patient
     */
    @GetMapping("/patient/{patientId}/recent")
    public ResponseEntity<List<ReadingDto>> getRecentReadingsForPatient(@PathVariable UUID patientId) {
        log.debug("REST request to get recent readings for patient with ID: {}", patientId);
        List<ReadingDto> readings = readingService.getRecentReadingsForPatient(patientId);
        log.info("Retrieved {} recent readings for patient with ID: {}", readings.size(), patientId);
        return ResponseEntity.ok(readings);
    }

    /**
     * Get the latest reading for a patient
     */
    @GetMapping("/patient/{patientId}/latest")
    public ResponseEntity<ReadingDto> getLatestReadingForPatient(@PathVariable UUID patientId) {
        log.debug("REST request to get latest reading for patient with ID: {}", patientId);
        ReadingDto reading = readingService.getLatestReadingForPatient(patientId);
        log.info("Retrieved latest reading for patient with ID: {}", patientId);
        return ResponseEntity.ok(reading);
    }
}
