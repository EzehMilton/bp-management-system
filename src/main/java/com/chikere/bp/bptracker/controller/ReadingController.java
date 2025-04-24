package com.chikere.bp.bptracker.controller;

import com.chikere.bp.bptracker.model.Reading;
import com.chikere.bp.bptracker.service.ReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ReadingController {
    private final ReadingService readingService;


    @PostMapping
    public ResponseEntity<Reading> create(@RequestBody Reading r) {
        return ResponseEntity.ok(readingService.create(r));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reading> update(@PathVariable UUID id, @RequestBody Reading r) {
        return ResponseEntity.ok(readingService.update(id, r));
    }
}
