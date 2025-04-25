package com.chikere.bp.bptracker.service;

import com.chikere.bp.bptracker.exception.EntityNotFoundException;
import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.Reading;
import com.chikere.bp.bptracker.repository.PatientRepository;
import com.chikere.bp.bptracker.repository.ReadingRepository;
import com.chikere.bp.bptracker.service.RiskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RiskServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ReadingRepository readingRepository;

    @InjectMocks
    private RiskService riskService;

    private UUID patientId;
    private Patient patient;
    private List<Reading> readings;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patientId = UUID.randomUUID();
        patient = new Patient();
        patient.setId(patientId);
        patient.setFullName("John Doe");

        readings = List.of(
                createReading(120, 80, "2023-10-01T10:00:00"),
                createReading(130, 85, "2023-10-02T10:00:00"),
                createReading(140, 90, "2023-10-03T10:00:00")
        );
    }

    @Test
    void returnsUnknownWhenNotEnoughReadings() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(readingRepository.findTop3ByPatientOrderByTimestampDesc(patient)).thenReturn(List.of());

        String result = riskService.accessRiskWithAI(patientId);

        assertEquals("UNKNOWN", result);
        verify(patientRepository, times(1)).findById(patientId);
        verify(readingRepository, times(1)).findTop3ByPatientOrderByTimestampDesc(patient);
    }

    @Test
    void throwsExceptionWhenPatientNotFound() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> riskService.accessRiskWithAI(patientId));
        verify(patientRepository, times(1)).findById(patientId);
        verifyNoInteractions(readingRepository);
    }

    @Test
    void returnsAtRiskWhenBloodPressureIsHigh() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        Reading reading = new Reading();
        reading.setSystolic(150);
        reading.setDiastolic(95);
        reading.setTimestamp(LocalDateTime.now());

        when(readingRepository.findFirstByPatientOrderByTimestampDesc(patient)).thenReturn(Optional.of(reading));

        String result = riskService.captureAndAssessImmediateReading(patientId);

        assertEquals("AT_RISK", result);
        verify(patientRepository, times(1)).findById(patientId);
        verify(readingRepository, times(1)).findFirstByPatientOrderByTimestampDesc(patient);
    }

    @Test
    void returnsNullWhenBloodPressureIsNormal() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        Reading reading = new Reading();
        reading.setSystolic(120);
        reading.setDiastolic(80);
        reading.setTimestamp(LocalDateTime.now());

        when(readingRepository.findFirstByPatientOrderByTimestampDesc(patient)).thenReturn(Optional.of(reading));

        String result = riskService.captureAndAssessImmediateReading(patientId);

        assertNull(result);
        verify(patientRepository, times(1)).findById(patientId);
        verify(readingRepository, times(1)).findFirstByPatientOrderByTimestampDesc(patient);
    }

    @Test
    void returnsCriticalWhenBloodPressureIsVeryHigh() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        Reading reading = new Reading();
        reading.setSystolic(190);
        reading.setDiastolic(125);
        reading.setTimestamp(LocalDateTime.now());

        when(readingRepository.findFirstByPatientOrderByTimestampDesc(patient)).thenReturn(Optional.of(reading));

        String result = riskService.captureAndAssessImmediateReading(patientId);

        assertEquals("CRITICAL", result);
        verify(patientRepository, times(1)).findById(patientId);
        verify(readingRepository, times(1)).findFirstByPatientOrderByTimestampDesc(patient);
    }

    @Test
    void throwsExceptionWhenNoReadingsFound() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(readingRepository.findFirstByPatientOrderByTimestampDesc(patient)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> riskService.captureAndAssessImmediateReading(patientId));
        verify(patientRepository, times(1)).findById(patientId);
        verify(readingRepository, times(1)).findFirstByPatientOrderByTimestampDesc(patient);
    }

    private Reading createReading(int systolic, int diastolic, String timestamp) {
        Reading reading = new Reading();
        reading.setSystolic(systolic);
        reading.setDiastolic(diastolic);
        reading.setTimestamp(LocalDateTime.parse(timestamp));
        return reading;
    }
}
