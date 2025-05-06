package com.chikere.bp.bptracker.service;

import com.chikere.bp.bptracker.exception.EntityNotFoundException;
import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.Reading;
import com.chikere.bp.bptracker.repository.PatientRepository;
import com.chikere.bp.bptracker.repository.ReadingRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RuleBasedRiskServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ReadingRepository readingRepository;

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private RuleBasedRiskService ruleBasedRiskService;

    private UUID patientId;
    private Patient patient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patientId = UUID.randomUUID();
        patient = new Patient();
        patient.setId(patientId);
        patient.setFullName("John Doe");
    }

    @Test
    void returnsMildHypertensiveWhenBloodPressureIsHigh() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        Reading reading = new Reading();
        reading.setSystolic(150);
        reading.setDiastolic(95);
        reading.setTimestamp(LocalDateTime.now());

        when(readingRepository.findFirstByPatientOrderByTimestampDesc(patient)).thenReturn(Optional.of(reading));

        String result = ruleBasedRiskService.captureAndAssessImmediateReading(patientId);

        assertEquals("MILD_HYPERTENSIVE", result);
        verify(patientRepository, times(1)).findById(patientId);
        verify(readingRepository, times(1)).findFirstByPatientOrderByTimestampDesc(patient);
    }

    @Test
    void returnsNormalWhenBloodPressureIsNormal() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        Reading reading = new Reading();
        reading.setSystolic(120);
        reading.setDiastolic(80);
        reading.setTimestamp(LocalDateTime.now());

        when(readingRepository.findFirstByPatientOrderByTimestampDesc(patient)).thenReturn(Optional.of(reading));

        String result = ruleBasedRiskService.captureAndAssessImmediateReading(patientId);

        assertEquals("NORMAL", result);
        verify(patientRepository, times(1)).findById(patientId);
        verify(readingRepository, times(1)).findFirstByPatientOrderByTimestampDesc(patient);
    }

    @Test
    void returnsSevereHypertensiveWhenBloodPressureIsVeryHigh() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        Reading reading = new Reading();
        reading.setSystolic(190);
        reading.setDiastolic(125);
        reading.setTimestamp(LocalDateTime.now());

        when(readingRepository.findFirstByPatientOrderByTimestampDesc(patient)).thenReturn(Optional.of(reading));

        String result = ruleBasedRiskService.captureAndAssessImmediateReading(patientId);

        assertEquals("SEVERE_HYPERTENSIVE", result);
        verify(patientRepository, times(1)).findById(patientId);
        verify(readingRepository, times(1)).findFirstByPatientOrderByTimestampDesc(patient);
    }

    @Test
    void throwsExceptionWhenNoReadingsFound() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(readingRepository.findFirstByPatientOrderByTimestampDesc(patient)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> ruleBasedRiskService.captureAndAssessImmediateReading(patientId));
        verify(patientRepository, times(1)).findById(patientId);
        verify(readingRepository, times(1)).findFirstByPatientOrderByTimestampDesc(patient);
    }

    @Test
    void throwsExceptionWhenPatientNotFound() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> ruleBasedRiskService.captureAndAssessImmediateReading(patientId));
        verify(patientRepository, times(1)).findById(patientId);
        verifyNoInteractions(readingRepository);
    }
}
