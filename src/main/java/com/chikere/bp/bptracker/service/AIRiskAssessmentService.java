package com.chikere.bp.bptracker.service;

import com.chikere.bp.bptracker.exception.EntityNotFoundException;
import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.Reading;
import com.chikere.bp.bptracker.model.enums.RiskLevel;
import com.chikere.bp.bptracker.repository.PatientRepository;
import com.chikere.bp.bptracker.repository.ReadingRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for AI-based risk assessment of blood pressure readings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIRiskAssessmentService {

    private static final String CIRCUIT_BREAKER_NAME = "AIBreaker";
    private static final String UNKNOWN = "UNKNOWN";
    private static final int MIN_READINGS_REQUIRED = 3;

    private final PatientRepository patientRepository;
    private final ReadingRepository readingRepository;
    private final ChatClient chatClient;
    private final Counter aiServiceSuccessCounter;
    private final Counter aiServiceFailureCounter;

    /**
     * Uses AI to assess patient risk based on their last 3 readings.
     *
     * @param patientId The ID of the patient
     * @return The risk level as a string
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "fallbackAssessRiskWithAI")
    public String assessRiskWithAI(UUID patientId) {
        log.info("Assessing risk with AI for patient with ID: {}", patientId);

        // Fetch and validate readings
        String readingsSummary = fetchAndValidateRecentBPReadings(patientId);
        if (UNKNOWN.equals(readingsSummary)) {
            log.info("Not enough readings found for patient with ID: {}", patientId);
            return UNKNOWN;
        }

        // Build prompt and send to AI
        String prompt = buildPrompt(readingsSummary);
        log.debug("Prompt sent to AI: {}", prompt);

        String aiResponse = chatClient.prompt(prompt).call().content();

        // Increment success counter
        aiServiceSuccessCounter.increment();

        // Update patient notes with AI response
        updatePatientNotes(patientId, aiResponse);

        log.debug("AI response: {}", aiResponse);

        // Extract and return risk level
        return extractRiskLevelFromAIResponse(aiResponse);
    }

    /**
     * Fallback method for the circuit breaker.
     * 
     * @param patientId The ID of the patient
     * @param throwable The exception that triggered the fallback
     * @return The default risk level (UNKNOWN)
     */
    public String fallbackAssessRiskWithAI(UUID patientId, Throwable throwable) {
        // Increment failure counter
        aiServiceFailureCounter.increment();

        log.error("Circuit breaker triggered for patient ID: {}. Error: {}", patientId, throwable.getMessage(), throwable);
        return UNKNOWN;
    }

    /**
     * Fetches and validates recent blood pressure readings for a patient.
     * 
     * @param patientId The ID of the patient
     * @return A formatted string of readings or UNKNOWN if not enough readings
     */
    private String fetchAndValidateRecentBPReadings(UUID patientId) {
        log.info("Fetching patient with ID: {}", patientId);
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found " + patientId));

        List<Reading> recentReadings = readingRepository.findTop3ByPatientOrderByTimestampDesc(patient);

        if (recentReadings == null || recentReadings.isEmpty() || recentReadings.size() < MIN_READINGS_REQUIRED) {
            return UNKNOWN;
        }
        return formatReadings(recentReadings);
    }

    /**
     * Formats a list of readings into a string.
     * 
     * @param readings The list of readings to format
     * @return A formatted string of readings
     */
    private String formatReadings(List<Reading> readings) {
        return readings.stream()
                .map(r -> String.format("Systolic: %d, Diastolic: %d, Time: %s",
                        r.getSystolic(), r.getDiastolic(), r.getTimestamp()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Extracts the risk level from an AI response.
     * 
     * @param aiResponse The AI response
     * @return The extracted risk level or UNKNOWN if not found
     */
    private String extractRiskLevelFromAIResponse(String aiResponse) {
        for (RiskLevel level : RiskLevel.values()) {
            if (aiResponse.contains(level.name())) {
                return level.name();
            }
        }
        return UNKNOWN;
    }

    /**
     * Updates the patient's notes with the AI analysis response.
     * 
     * @param patientId The ID of the patient
     * @param aiResponse The AI response
     */
    private void updatePatientNotes(UUID patientId, String aiResponse) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found " + patientId));

        // Format the note with timestamp
        String timestamp = LocalDateTime.now().toString();
        String formattedNote = String.format("[%s] AI Risk Analysis: %s", timestamp, aiResponse);

        // Append to existing notes or create new notes
        if (patient.getNotes() != null && !patient.getNotes().isEmpty()) {
            patient.setNotes(patient.getNotes() + "\n\n" + formattedNote);
        } else {
            patient.setNotes(formattedNote);
        }

        // Save the updated patient
        patientRepository.save(patient);
        log.info("Updated notes for patient with ID: {}", patientId);
    }

    /**
     * Builds a prompt for the AI based on readings summary.
     * 
     * @param readingsSummary The summary of readings
     * @return The built prompt
     */
    private String buildPrompt(String readingsSummary) {
        return """
            Based on these 3 recent blood pressure readings (systolic/diastolic):
            %s

            Please assess the patient's risk level and respond with one of the following options in CAPITAL LETTERS: 
            LOW, NORMAL, MILD_HYPERTENSIVE, MODERATE_HYPERTENSIVE, or SEVERE_HYPERTENSIVE.

            First, explain your reasoning based on the readings.
            Then, clearly state the risk level on a new line, as one of the five options.

            Blood pressure classification:
            - 80/80 and below: LOW
            - 80-140/80-90: NORMAL
            - 140-160/90-100: MILD_HYPERTENSIVE
            - 160-180/100-110: MODERATE_HYPERTENSIVE
            - 180/110 and above: SEVERE_HYPERTENSIVE
            """.formatted(readingsSummary);
    }
}
