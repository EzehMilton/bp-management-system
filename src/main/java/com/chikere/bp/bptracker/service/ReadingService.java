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
        return readingRepository.save(r);
    }

    public Reading update(UUID id, Reading r) {
        r.setId(id);
        return readingRepository.save(r);
    }

}
