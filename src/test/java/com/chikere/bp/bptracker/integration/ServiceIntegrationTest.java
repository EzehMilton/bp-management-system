package com.chikere.bp.bptracker.integration;

import com.chikere.bp.bptracker.dto.NewReadingDto;
import com.chikere.bp.bptracker.exception.EntityNotFoundException;
import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.Reading;
import com.chikere.bp.bptracker.model.enums.Arm;
import com.chikere.bp.bptracker.model.enums.BodyPosition;
import com.chikere.bp.bptracker.model.enums.Gender;
import com.chikere.bp.bptracker.repository.PatientRepository;
import com.chikere.bp.bptracker.repository.ReadingRepository;
import com.chikere.bp.bptracker.service.PatientService;
import com.chikere.bp.bptracker.service.ReadingService;
import com.chikere.bp.bptracker.service.RiskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ServiceIntegrationTest {

    @Autowired
    private PatientService patientService;

    @Autowired
    private ReadingService readingService;

    @Autowired
    private RiskService riskService;

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
        testPatient.setFullName("Service Test Patient");
        testPatient.setGender(Gender.MALE);
        testPatient.setBirthDate(LocalDate.now().minusYears(45));
        testPatient = patientRepository.save(testPatient);
    }

    @Test
    void shouldCreateAndRetrievePatient() {
        // Given a new patient
        Patient newPatient = new Patient();
        newPatient.setFullName("New Service Test Patient");
        newPatient.setGender(Gender.FEMALE);
        newPatient.setBirthDate(LocalDate.now().minusYears(30));

        // When creating the patient
        Patient createdPatient = patientService.createPatient(newPatient);

        // Then the patient should be created and retrievable
        assertNotNull(createdPatient.getId());

        Patient retrievedPatient = patientService.get(createdPatient.getId());
        assertEquals("New Service Test Patient", retrievedPatient.getFullName());
        assertEquals(Gender.FEMALE, retrievedPatient.getGender());
    }

    @Test
    void shouldUpdatePatient() {
        // Given an existing patient

        // When updating the patient
        testPatient.setFullName("Updated Service Test Patient");
        patientService.updatePatient(testPatient.getId(), testPatient);

        // Then the patient should be updated
        Patient updatedPatient = patientService.get(testPatient.getId());
        assertEquals("Updated Service Test Patient", updatedPatient.getFullName());
    }

    @Test
    void shouldDeletePatient() {
        // Given an existing patient

        // When deleting the patient
        patientService.deletePatient(testPatient.getId());

        // Then the patient should be deleted
        assertThrows(EntityNotFoundException.class, () -> patientService.get(testPatient.getId()));
    }

    @Test
    void shouldCreateAndRetrieveReading() {
        // Given a new reading DTO
        NewReadingDto newReadingDto = new NewReadingDto();
        newReadingDto.setPatientId(testPatient.getId());
        newReadingDto.setSystolic(120);
        newReadingDto.setDiastolic(80);
        newReadingDto.setHeartRate(72);
        newReadingDto.setArm(Arm.LEFT);
        newReadingDto.setBodyPosition(BodyPosition.SITTING);

        // When creating the reading
        var createdReading = readingService.create(newReadingDto);

        // Then the reading should be created and retrievable
        assertNotNull(createdReading.getId());

        var retrievedReading = readingService.getById(UUID.fromString(createdReading.getId().toString()));
        assertEquals(120, retrievedReading.getSystolic());
        assertEquals(80, retrievedReading.getDiastolic());
        assertEquals(72, retrievedReading.getHeartRate());
    }

    @Test
    void shouldGetRecentReadingsForPatient() {
        // Given 5 readings for the test patient
        for (int i = 0; i < 5; i++) {
            NewReadingDto newReadingDto = new NewReadingDto();
            newReadingDto.setPatientId(testPatient.getId());
            newReadingDto.setSystolic(120 + i * 10);
            newReadingDto.setDiastolic(80 + i * 5);
            newReadingDto.setHeartRate(72);
            newReadingDto.setArm(Arm.LEFT);
            newReadingDto.setBodyPosition(BodyPosition.SITTING);
            readingService.create(newReadingDto);
        }

        // When getting recent readings
        var recentReadings = readingService.getRecentReadingsForPatient(testPatient.getId());

        // Then should get the most recent readings (up to 3)
        assertFalse(recentReadings.isEmpty());
        assertTrue(recentReadings.size() <= 3);
    }

    @Test
    void shouldCheckIfPatientHasEnoughReadingsForRisk() {
        // Given no readings initially

        // Then should not have enough readings for risk assessment
        assertFalse(readingService.hasAtLeastThreeReadings(testPatient.getId()));

        // When adding 3 readings
        for (int i = 0; i < 3; i++) {
            NewReadingDto newReadingDto = new NewReadingDto();
            newReadingDto.setPatientId(testPatient.getId());
            newReadingDto.setSystolic(120);
            newReadingDto.setDiastolic(80);
            newReadingDto.setHeartRate(72);
            newReadingDto.setArm(Arm.LEFT);
            newReadingDto.setBodyPosition(BodyPosition.SITTING);
            readingService.create(newReadingDto);
        }

        // Then should have enough readings for risk assessment
        assertTrue(readingService.hasAtLeastThreeReadings(testPatient.getId()));
    }

    @Test
    void shouldGenerateCsvForReadings() {
        // Given a reading for the test patient
        NewReadingDto newReadingDto = new NewReadingDto();
        newReadingDto.setPatientId(testPatient.getId());
        newReadingDto.setSystolic(120);
        newReadingDto.setDiastolic(80);
        newReadingDto.setHeartRate(72);
        newReadingDto.setArm(Arm.LEFT);
        newReadingDto.setBodyPosition(BodyPosition.SITTING);
        readingService.create(newReadingDto);

        // When generating CSV
        String csv = readingService.getAllReadingsAsCsv();

        // Then CSV should contain reading data
        assertNotNull(csv);
        assertTrue(csv.contains("Patient Name"));
        assertTrue(csv.contains("Systolic"));
        assertTrue(csv.contains("Diastolic"));
        assertTrue(csv.contains("120") && csv.contains("80"));
    }

    @Test
    void shouldPerformRiskAssessment() {
        // Given 3 readings for the test patient
        for (int i = 0; i < 3; i++) {
            Reading reading = new Reading();
            reading.setPatient(testPatient);
            reading.setSystolic(120);
            reading.setDiastolic(80);
            reading.setHeartRate(72);
            reading.setArm(Arm.LEFT);
            reading.setBodyPosition(BodyPosition.SITTING);
            reading.setTimestamp(LocalDateTime.now().minusDays(i));
            readingRepository.save(reading);
        }

        // When performing risk assessment
        String riskLevel = riskService.accessRiskWithAI(testPatient.getId());

        // Then should get a risk level
        assertNotNull(riskLevel);
        // The actual risk level might vary, but it should be a non-empty string
        assertFalse(riskLevel.isEmpty());
    }

    @Test
    void shouldSearchPatients() {
        // Given multiple patients with different names
        Patient patient1 = new Patient();
        patient1.setFullName("John Smith");
        patient1.setGender(Gender.MALE);
        patient1.setBirthDate(LocalDate.now().minusYears(40));
        patientRepository.save(patient1);

        Patient patient2 = new Patient();
        patient2.setFullName("Jane Doe");
        patient2.setGender(Gender.FEMALE);
        patient2.setBirthDate(LocalDate.now().minusYears(35));
        patientRepository.save(patient2);

        // When searching for patients
        List<Patient> smithPatients = patientService.search("Smith");
        List<Patient> jPatients = patientService.search("J");

        // Then should find matching patients
        assertEquals(1, smithPatients.size());
        assertEquals("John Smith", smithPatients.get(0).getFullName());

        assertTrue(jPatients.size() >= 2);
        assertTrue(jPatients.stream().anyMatch(p -> p.getFullName().equals("John Smith")));
        assertTrue(jPatients.stream().anyMatch(p -> p.getFullName().equals("Jane Doe")));
    }
}
