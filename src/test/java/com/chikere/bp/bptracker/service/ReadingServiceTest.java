package com.chikere.bp.bptracker.service;

import com.chikere.bp.bptracker.dto.NewReadingDto;
import com.chikere.bp.bptracker.dto.ReadingDto;
import com.chikere.bp.bptracker.exception.EntityNotFoundException;
import com.chikere.bp.bptracker.mapper.ReadingMapper;
import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.Reading;
import com.chikere.bp.bptracker.repository.PatientRepository;
import com.chikere.bp.bptracker.repository.ReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReadingServiceTest {

    @Mock
    private ReadingRepository readingRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ReadingMapper readingMapper;

    @InjectMocks
    private ReadingService readingService;

    private Reading reading;
    private ReadingDto readingDto;
    private NewReadingDto newReadingDto;
    private Patient patient;
    private UUID readingId;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup IDs
        readingId = UUID.randomUUID();
        patientId = UUID.randomUUID();

        // Setup patient
        patient = new Patient();
        patient.setId(patientId);

        // Setup reading entity
        reading = new Reading();
        reading.setId(readingId);
        reading.setSystolic(120);
        reading.setDiastolic(80);
        reading.setPatient(patient);

        // Setup reading DTO
        readingDto = new ReadingDto();
        readingDto.setId(readingId);
        readingDto.setPatientId(patientId);
        readingDto.setSystolic(120);
        readingDto.setDiastolic(80);

        // Setup new reading DTO
        newReadingDto = new NewReadingDto();
        newReadingDto.setPatientId(patientId);
        newReadingDto.setSystolic(120);
        newReadingDto.setDiastolic(80);
    }

    @Test
    void createsReadingSuccessfully() {
        // Setup mocks
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(readingMapper.toEntity(newReadingDto)).thenReturn(reading);
        when(readingRepository.save(any(Reading.class))).thenReturn(reading);
        when(readingMapper.toDto(reading)).thenReturn(readingDto);

        // Call service method
        ReadingDto result = readingService.create(newReadingDto);

        // Verify results
        assertEquals(readingDto, result);
        verify(patientRepository, times(1)).findById(patientId);
        verify(readingMapper, times(1)).toEntity(newReadingDto);
        verify(readingRepository, times(1)).save(any(Reading.class));
        verify(readingMapper, times(1)).toDto(reading);
    }

    @Test
    void createsReadingThrowsExceptionWhenPatientNotFound() {
        // Setup mocks
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // Call service method and verify exception
        assertThrows(EntityNotFoundException.class, () -> readingService.create(newReadingDto));

        // Verify repository was called
        verify(patientRepository, times(1)).findById(patientId);
    }

    @Test
    void updatesReadingSuccessfully() {
        // Setup mocks
        when(readingRepository.existsById(readingId)).thenReturn(true);
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(readingMapper.toEntity(readingDto)).thenReturn(reading);
        when(readingRepository.save(any(Reading.class))).thenReturn(reading);
        when(readingMapper.toDto(reading)).thenReturn(readingDto);

        // Call service method
        ReadingDto result = readingService.update(readingId, readingDto);

        // Verify results
        assertEquals(readingDto, result);
        verify(readingRepository, times(1)).existsById(readingId);
        verify(patientRepository, times(1)).findById(patientId);
        verify(readingMapper, times(1)).toEntity(readingDto);
        verify(readingRepository, times(1)).save(any(Reading.class));
        verify(readingMapper, times(1)).toDto(reading);
    }

    @Test
    void updateThrowsExceptionWhenReadingNotFound() {
        // Setup mocks
        when(readingRepository.existsById(readingId)).thenReturn(false);

        // Call service method and verify exception
        assertThrows(EntityNotFoundException.class, () -> readingService.update(readingId, readingDto));

        // Verify repository was called
        verify(readingRepository, times(1)).existsById(readingId);
    }
}
