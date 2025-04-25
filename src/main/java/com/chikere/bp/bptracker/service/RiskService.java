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
        NORMAL,
        AT_RISK,
        CRITICAL
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

        if (systolic >= 180 || diastolic >= 120) {
            log.info("Critical BP Levels for: {} , reading taken: {}", patient, latestReading.getTimestamp());
            return RiskLevel.CRITICAL.name();
        }
        if (systolic >= 140 || diastolic >= 90) {
            log.info("At Risk BP Levels for: {} , reading taken: {}", patient, latestReading.getTimestamp());
            return RiskLevel.AT_RISK.name();
        }
        return RiskLevel.NORMAL.name();
    }


    /**
     * Uses AI to assess patient risk based on their last 3 readings.
     */
    public String accessRiskWithAI(UUID patientId) {
        // This will be a standalone call, that will have a button or link associated with it in the FrontEnd.
        // It will be used weekly to assess the patients risk level.
        log.info("Fetching recent blood pressure readings for patient with ID: {}", patientId);
        String readingsSummary = fetchAndValidateRecentBPReadings(patientId);
        if (UNKNOWN.equals(readingsSummary)) {
            log.info("Not enough readings found for patient with ID: {}", patientId);
            return UNKNOWN;
        }
        log.info("Recent blood pressure readings for patient with ID: {}: {}", patientId, readingsSummary);

        // TODO - Remove hardcoded readings summary. It is used if we dont have enough readings.
        // String readingsSummary = getHardCodedReadings();

        String prompt = buildPrompt(readingsSummary);
        log.info("Prompt sent to AI: {}", prompt);

        // Send prompt to LLM via Spring AI
        var bpResponse = chatClient.prompt(prompt).call().content();
        // TODO Add response to the patients notes.
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

    private String buildPrompt(String readingsSummary) {
        // TODO Extract prompt to seperate file/resource.
        return """
            Based on these 3 recent blood pressure readings (systolic/diastolic):
            %s
            
            Please assess the patient's risk level and respond with one of the following options in CAPITAL LETTERS: NORMAL, AT_RISK, or CRITICAL.
            
            First, explain your reasoning based on the readings.
            Then, clearly state the risk level on a new line, as one of the three options (NORMAL, AT_RISK, or CRITICAL).
            
            Normal blood pressure is around 120/80 mmHg.
            Readings consistently higher than 130/80 may indicate AT_RISK.
            Readings significantly above 140/90 or very low may indicate CRITICAL.
            """.formatted(readingsSummary);
    }

    // TODO Remove in Production
    private static String getHardCodedReadings() {
        return """
        Systolic: 120, Diastolic: 80, Time: 2023-10-01T10:00:00
        Systolic: 130, Diastolic: 85, Time: 2023-10-02T10:00:00
        Systolic: 140, Diastolic: 90, Time: 2023-10-03T10:00:00
        Systolic: 150, Diastolic: 95, Time: 2023-10-04T10:00:00
        Systolic: 160, Diastolic: 100, Time: 2023-10-05T10:00:00
        """;
    }
}
