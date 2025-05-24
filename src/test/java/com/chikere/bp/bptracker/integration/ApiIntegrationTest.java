package com.chikere.bp.bptracker.integration;

import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.Reading;
import com.chikere.bp.bptracker.model.enums.Arm;
import com.chikere.bp.bptracker.model.enums.BodyPosition;
import com.chikere.bp.bptracker.model.enums.Gender;
import com.chikere.bp.bptracker.repository.PatientRepository;
import com.chikere.bp.bptracker.repository.ReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ReadingRepository readingRepository;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        readingRepository.deleteAll();
        patientRepository.deleteAll();

        // Create a test patient
        testPatient = new Patient();
        testPatient.setFullName("API Test Patient");
        testPatient.setGender(Gender.MALE);
        testPatient.setBirthDate(LocalDate.now().minusYears(45));
        testPatient = patientRepository.save(testPatient);
    }

    @Test
    void shouldReturnBadRequestWhenNotEnoughReadingsForRiskAssessment() throws Exception {
        // Test API endpoint for risk analysis without enough readings
        mockMvc.perform(get("/v1/api/risk/{patientId}/analyzeAI", testPatient.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Patient needs at least 3 readings")));
    }

    @Test
    void shouldDownloadReadingsAsCsv() throws Exception {
        // Create a reading for the patient
        Reading reading = new Reading();
        reading.setPatient(testPatient);
        reading.setSystolic(120);
        reading.setDiastolic(80);
        reading.setHeartRate(72);
        reading.setArm(Arm.LEFT);
        reading.setBodyPosition(BodyPosition.SITTING);
        reading.setTimestamp(LocalDateTime.now());
        readingRepository.save(reading);

        // Test CSV download
        mockMvc.perform(get("/readings/download-csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", containsString("filename=\"all_readings.csv\"")))
                .andExpect(content().string(containsString("Patient Name,Systolic,Diastolic")));
    }

    @Test
    void shouldHandleImmediateRiskAssessment() throws Exception {
        // Test immediate risk assessment
        mockMvc.perform(post("/patients/{patientId}/risk/immediate", testPatient.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients/" + testPatient.getId() + "/risk"));
    }
}
