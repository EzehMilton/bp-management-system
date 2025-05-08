package com.chikere.bp.bptracker.integration;

import com.chikere.bp.bptracker.dto.NewPatientDTO;
import com.chikere.bp.bptracker.dto.NewReadingDto;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class WebControllerIntegrationTest {

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
        testPatient.setFullName("Test Patient");
        testPatient.setGender(Gender.MALE);
        testPatient.setBirthDate(LocalDate.now().minusYears(45));
        testPatient = patientRepository.save(testPatient);
    }

    @Test
    void homePageShouldDisplayPatients() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("patients"))
                .andExpect(content().string(containsString("Test Patient")));
    }

    @Test
    void shouldCreateNewPatient() throws Exception {
        mockMvc.perform(post("/patients/new")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("fullName", "New Test Patient")
                .param("gender", "FEMALE")
                .param("age", "35"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        List<Patient> patients = patientRepository.findAll();
        assertTrue(patients.stream().anyMatch(p -> p.getFullName().equals("New Test Patient")));
    }

    @Test
    void shouldViewPatientDetails() throws Exception {
        mockMvc.perform(get("/patients/{id}", testPatient.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/view"))
                .andExpect(model().attributeExists("patient"))
                .andExpect(content().string(containsString("Test Patient")));
    }

    @Test
    void shouldUpdatePatient() throws Exception {
        LocalDate newBirthDate = LocalDate.now().minusYears(50);
        mockMvc.perform(post("/patients/{id}/edit", testPatient.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("fullName", "Updated Test Patient")
                .param("gender", "MALE")
                .param("birthDate", newBirthDate.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients/" + testPatient.getId()));

        Patient updatedPatient = patientRepository.findById(testPatient.getId()).orElseThrow();
        assertEquals("Updated Test Patient", updatedPatient.getFullName());
        // Check that the birth date is approximately 50 years ago (allowing for slight differences in day)
        assertTrue(Period.between(updatedPatient.getBirthDate(), LocalDate.now()).getYears() >= 49);
    }

    @Test
    void shouldDeletePatient() throws Exception {
        mockMvc.perform(post("/patients/{id}/delete", testPatient.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients"));

        assertFalse(patientRepository.existsById(testPatient.getId()));
    }

    @Test
    void shouldCreateNewReading() throws Exception {
        mockMvc.perform(post("/readings/new")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("patientId", testPatient.getId().toString())
                .param("systolic", "120")
                .param("diastolic", "80")
                .param("heartRate", "72")
                .param("arm", "LEFT")
                .param("bodyPosition", "SITTING"))
                .andExpect(status().is3xxRedirection());

        List<Reading> readings = readingRepository.findAllByPatientOrderByTimestampDesc(testPatient);
        assertFalse(readings.isEmpty());
        Reading reading = readings.get(0);
        assertEquals(120, reading.getSystolic());
        assertEquals(80, reading.getDiastolic());
        assertEquals(72, reading.getHeartRate());
    }

    @Test
    void shouldCreateMultipleReadingsAndPerformRiskAssessment() throws Exception {
        // Create 3 readings for the patient
        for (int i = 0; i < 3; i++) {
            Reading reading = new Reading();
            reading.setPatient(testPatient);
            reading.setSystolic(120 + i * 10);
            reading.setDiastolic(80 + i * 5);
            reading.setHeartRate(72);
            reading.setArm(Arm.LEFT);
            reading.setBodyPosition(BodyPosition.SITTING);
            reading.setTimestamp(LocalDateTime.now().minusDays(i));
            readingRepository.save(reading);
        }

        // Test risk assessment page
        mockMvc.perform(get("/patients/{patientId}/risk", testPatient.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("risk/assessment"))
                .andExpect(model().attributeExists("patient"))
                .andExpect(model().attributeExists("latestReading"));
    }

    @Test
    void shouldReturnBadRequestWhenNotEnoughReadingsForRiskAssessment() throws Exception {
        // Test API endpoint for risk analysis
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
    void shouldSearchPatients() throws Exception {
        // Create another patient with a different name
        Patient anotherPatient = new Patient();
        anotherPatient.setFullName("Another Patient");
        anotherPatient.setGender(Gender.FEMALE);
        anotherPatient.setBirthDate(LocalDate.now().minusYears(30));
        patientRepository.save(anotherPatient);

        // Test search functionality
        mockMvc.perform(get("/patients/search").param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/list"))
                .andExpect(model().attribute("searchTerm", "Test"))
                .andExpect(content().string(containsString("Test Patient")))
                .andExpect(content().string(not(containsString("Another Patient"))));
    }
}
