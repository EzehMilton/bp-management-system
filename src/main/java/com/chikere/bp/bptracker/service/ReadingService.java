package com.chikere.bp.bptracker.service;

import com.chikere.bp.bptracker.model.Reading;
import com.chikere.bp.bptracker.repository.ReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ReadingService {
    private final ReadingRepository readingRepository;

    public Reading create(Reading r) {
        // This will add the reading to the DB. Once added, it may be used by the Risk service for analysis.

        return readingRepository.save(r);
    }

    public Reading update(UUID id, Reading r) {
        // This should reset the readings if a mistake was made
        r.setId(id);
        return readingRepository.save(r);
    }

}
