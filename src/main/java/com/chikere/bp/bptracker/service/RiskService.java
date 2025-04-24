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
        COULD_BE_AT_RISK,
        AT_RISK,
        UNKNOWN
    }
    
    private final PatientRepository patientRepository;
    private final ReadingRepository readingRepository;
    private final ChatClient chatClient;

    private static final String UNKNOWN = "UNKNOWN";
    private static final int MIN_READINGS_REQUIRED = 3;


    /**
     * Uses AI to assess patient risk based on their last 5 readings.
     */
    public String accessRiskWithAI(UUID patientId) {
        // Get Last 3 readings from patient. Readings less than 3 are not enough to diagnose high blood pressure
        String readingsSummary = fetchAndFormatReadings(patientId);
        if (UNKNOWN.equals(readingsSummary)) {
            return UNKNOWN;
        }

        // TODO - Remove hardcoded readings summary. It is used if we dont have enough readings.
        //String readingsSummary = getHardCodedTop3ReadingsSummary();

        String prompt = buildPrompt(readingsSummary);
        log.info("Prompt sent to AI: {}", prompt);

        // Send prompt to LLM via Spring AI
        var bpResponse = chatClient.prompt(prompt).call().content();
        // TODO Add response to the patients notes.
        log.info("AI response: {}", bpResponse);
        // extract risk level from AI response and return it
        return extractRiskLevelFromAIResponse(bpResponse);
    }

    private String fetchAndFormatReadings(UUID patientId) {
        System.out.println("Fetching patient with ID: " + patientId);
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
        return """
            Based on these 3 recent blood pressure readings:
            %s

            Please assess the patient's risk level (normal, could be at risk, at risk) and respond with either NORMAL or COULD_BE_AT_RISK or AT_RISK.
            """.formatted(readingsSummary);
    }

    // TODO Remove in Production
    private static String getHardCodedTop3ReadingsSummary() {
        String readingsSummary = List.of(
                String.format("Systolic: %d, Diastolic: %d, Time: %s", 120, 80, "2023-10-01T10:00:00"),
                String.format("Systolic: %d, Diastolic: %d, Time: %s", 130, 85, "2023-10-02T10:00:00"),
                String.format("Systolic: %d, Diastolic: %d, Time: %s", 140, 90, "2023-10-03T10:00:00"),
                String.format("Systolic: %d, Diastolic: %d, Time: %s", 150, 95, "2023-10-04T10:00:00"),
                String.format("Systolic: %d, Diastolic: %d, Time: %s", 160, 100, "2023-10-05T10:00:00")
        ).stream().collect(Collectors.joining("\n"));
        return readingsSummary;
    }
}
