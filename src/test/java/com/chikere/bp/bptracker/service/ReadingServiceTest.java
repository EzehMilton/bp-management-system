package com.chikere.bp.bptracker.service;

import com.chikere.bp.bptracker.model.Reading;
import com.chikere.bp.bptracker.repository.ReadingRepository;
import com.chikere.bp.bptracker.service.ReadingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ReadingServiceTest {

    @Mock
    private ReadingRepository readingRepository;

    @InjectMocks
    private ReadingService readingService;

    private Reading reading;
    private UUID readingId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        readingId = UUID.randomUUID();
        reading = new Reading();
        reading.setId(readingId);
        reading.setSystolic(120);
        reading.setDiastolic(80);
    }

    @Test
    void createsReadingSuccessfully() {
        when(readingRepository.save(reading)).thenReturn(reading);

        Reading result = readingService.create(reading);

        assertEquals(reading, result);
        verify(readingRepository, times(1)).save(reading);
    }

    @Test
    void updatesReadingSuccessfully() {
        when(readingRepository.save(reading)).thenReturn(reading);

        Reading result = readingService.update(readingId, reading);

        assertEquals(readingId, result.getId());
        assertEquals(reading, result);
        verify(readingRepository, times(1)).save(reading);
    }

    @Test
    void updateSetsCorrectId() {
        Reading updatedReading = new Reading();
        updatedReading.setSystolic(130);
        updatedReading.setDiastolic(85);

        when(readingRepository.save(updatedReading)).thenReturn(updatedReading);

        Reading result = readingService.update(readingId, updatedReading);

        assertEquals(readingId, result.getId());
        verify(readingRepository, times(1)).save(updatedReading);
    }
}