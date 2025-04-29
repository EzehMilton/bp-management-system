package com.chikere.bp.bptracker.controller;

import com.chikere.bp.bptracker.dto.NewReadingDto;
import com.chikere.bp.bptracker.dto.ReadingDto;
import com.chikere.bp.bptracker.service.ReadingService;
import com.chikere.bp.bptracker.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
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
public class ReadingController {
    private final ReadingService readingService;

    /**
     * Create a new reading
     */
    @PostMapping
    public ResponseEntity<ReadingDto> create(@RequestBody NewReadingDto newReadingDto) {
        ReadingDto created = readingService.create(newReadingDto);
        return ResponseUtil.created(created, created.getId());
    }

    /**
     * Update an existing reading
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReadingDto> update(@PathVariable UUID id, @RequestBody ReadingDto readingDto) {
        return ResponseEntity.ok(readingService.update(id, readingDto));
    }

    /**
     * Get a reading by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReadingDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(readingService.getById(id));
    }

    /**
     * Get all readings
     */
    @GetMapping
    public ResponseEntity<List<ReadingDto>> getAll() {
        return ResponseEntity.ok(readingService.findAll());
    }

    /**
     * Delete a reading
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        readingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get recent readings for a patient
     */
    @GetMapping("/patient/{patientId}/recent")
    public ResponseEntity<List<ReadingDto>> getRecentReadingsForPatient(@PathVariable UUID patientId) {
        return ResponseEntity.ok(readingService.getRecentReadingsForPatient(patientId));
    }

    /**
     * Get the latest reading for a patient
     */
    @GetMapping("/patient/{patientId}/latest")
    public ResponseEntity<ReadingDto> getLatestReadingForPatient(@PathVariable UUID patientId) {
        return ResponseEntity.ok(readingService.getLatestReadingForPatient(patientId));
    }
}
