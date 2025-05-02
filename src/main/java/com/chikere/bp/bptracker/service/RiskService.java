package com.chikere.bp.bptracker.service;

import com.chikere.bp.bptracker.exception.EntityNotFoundException;
import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.Reading;
import com.chikere.bp.bptracker.repository.PatientRepository;
import com.chikere.bp.bptracker.repository.ReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskService {

    public enum RiskLevel {
        LOW,
        NORMAL,
        MILD_HYPERTENSIVE,
        MODERATE_HYPERTENSIVE,
        SEVERE_HYPERTENSIVE
    }

    private final PatientRepository patientRepository;
    private final ReadingRepository readingRepository;
    private final ChatClient chatClient;

    private static final String UNKNOWN = "UNKNOWN";
    private static final int MIN_READINGS_REQUIRED = 3;

    public String captureAndAssessImmediateReading(UUID patientId) {
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

        return riskLevel.name();
    }


    /**
     * Uses AI to assess patient risk based on their last 3 readings.
     */
    public String accessRiskWithAI(UUID patientId) {
        log.info("Fetching recent blood pressure readings for patient with ID: {}", patientId);
        String readingsSummary = fetchAndValidateRecentBPReadings(patientId);
        if (UNKNOWN.equals(readingsSummary)) {
            log.info("Not enough readings found for patient with ID: {}", patientId);
            return UNKNOWN;
        }
        log.info("Recent blood pressure readings for patient with ID: {}: {}", patientId, readingsSummary);

        String prompt = buildPrompt(readingsSummary);
        log.info("Prompt sent to AI: {}", prompt);

        // Send prompt to LLM via Spring AI
        var bpResponse = chatClient.prompt(prompt).call().content();

        // Add response to the patient's notes by updating the patient entity
        updatePatientNotes(patientId, bpResponse);

        log.debug("AI response: {}", bpResponse);
        // extract risk level from AI response and return it
        return extractRiskLevelFromAIResponse(bpResponse);
    }

    private String fetchAndValidateRecentBPReadings(UUID patientId) {
        log.info("Fetching patient with ID: {}", patientId);
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found " + patientId));
        log.info("Patient found: {}", patient);

        List<Reading> recentReadings = readingRepository.findTop3ByPatientOrderByTimestampDesc(patient);

        if (recentReadings == null || recentReadings.isEmpty() || recentReadings.size() < MIN_READINGS_REQUIRED) {
            return UNKNOWN;
        }
        return formatReadings(recentReadings);
    }

    private String formatReadings(List<Reading> readings) {
        return readings.stream()
                .map(r -> String.format("Systolic: %d, Diastolic: %d, Time: %s",
                        r.getSystolic(), r.getDiastolic(), r.getTimestamp()))
                .collect(Collectors.joining("\n"));
    }

    private String extractRiskLevelFromAIResponse(String aiResponse) {
        for (RiskLevel level : RiskLevel.values()) {
            if (aiResponse.contains(level.name())) {
                return level.name();
            }
        }
        return "UNKNOWN";
    }

    /**
     * Updates the patient's notes with the AI analysis response
     */
    private void updatePatientNotes(UUID patientId, String aiResponse) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found " + patientId));

        // Format the note with timestamp
        String timestamp = java.time.LocalDateTime.now().toString();
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
