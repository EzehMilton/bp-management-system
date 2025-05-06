package com.chikere.bp.bptracker.service;

import com.chikere.bp.bptracker.exception.EntityNotFoundException;
import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.Reading;
import com.chikere.bp.bptracker.model.enums.RiskLevel;
import com.chikere.bp.bptracker.repository.PatientRepository;
import com.chikere.bp.bptracker.repository.ReadingRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for rule-based risk assessment of blood pressure readings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RuleBasedRiskService {

    private final PatientRepository patientRepository;
    private final ReadingRepository readingRepository;
    private final MeterRegistry meterRegistry;

    /**
     * Assesses the risk level based on the patient's most recent blood pressure reading.
     *
     * @param patientId The ID of the patient
     * @return The risk level as a string
     * @throws EntityNotFoundException if the patient or reading is not found
     */
    public String captureAndAssessImmediateReading(UUID patientId) {
        // Create a timer for this operation
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found " + patientId));

            // Get the latest reading from the database
            Reading latestReading = readingRepository.findFirstByPatientOrderByTimestampDesc(patient)
                    .orElseThrow(() -> new EntityNotFoundException("No readings found for patient " + patientId));

            int systolic = latestReading.getSystolic();
            int diastolic = latestReading.getDiastolic();

            RiskLevel riskLevel;

            if (systolic >= 180 || diastolic >= 110) {
                log.info("Severe Hypertensive BP Levels for: {} , reading taken: {}", patient, latestReading.getTimestamp());
                riskLevel = RiskLevel.SEVERE_HYPERTENSIVE;
            } else if (systolic >= 160 || diastolic >= 100) {
                log.info("Moderate Hypertensive BP Levels for: {} , reading taken: {}", patient, latestReading.getTimestamp());
                riskLevel = RiskLevel.MODERATE_HYPERTENSIVE;
            } else if (systolic >= 140 || diastolic >= 90) {
                log.info("Mild Hypertensive BP Levels for: {} , reading taken: {}", patient, latestReading.getTimestamp());
                riskLevel = RiskLevel.MILD_HYPERTENSIVE;
            } else if (systolic >= 80 && diastolic >= 80) {
                log.info("Normal BP Levels for: {} , reading taken: {}", patient, latestReading.getTimestamp());
                riskLevel = RiskLevel.NORMAL;
            } else {
                log.info("Low BP Levels for: {} , reading taken: {}", patient, latestReading.getTimestamp());
                riskLevel = RiskLevel.LOW;
            }

            // Record metrics for this risk level
            meterRegistry.counter("bp.risk.level", "level", riskLevel.name()).increment();

            // Record blood pressure values as gauges
            meterRegistry.gauge("bp.reading.systolic", systolic);
            meterRegistry.gauge("bp.reading.diastolic", diastolic);

            String result = riskLevel.name();

            // Stop the timer and record the duration
            sample.stop(meterRegistry.timer("bp.risk.assessment.duration", "method", "rule-based"));

            return result;
        } catch (Exception e) {
            // Record the error
            meterRegistry.counter("bp.risk.assessment.errors", "method", "rule-based").increment();
            // Stop the timer even in case of error
            sample.stop(meterRegistry.timer("bp.risk.assessment.duration", "method", "rule-based", "status", "error"));
            throw e;
        }
    }
}
