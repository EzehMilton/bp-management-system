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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class RepositoryIntegrationTest {

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
        testPatient.setFullName("Repository Test Patient");
        testPatient.setGender(Gender.MALE);
        testPatient.setBirthDate(LocalDate.now().minusYears(45));
        testPatient = patientRepository.save(testPatient);
    }

    @Test
    void shouldSaveAndRetrievePatient() {
        // Given a patient is saved in the setup

        // When retrieving the patient
        Optional<Patient> retrievedPatient = patientRepository.findById(testPatient.getId());

        // Then the patient should be found
        assertTrue(retrievedPatient.isPresent());
        assertEquals("Repository Test Patient", retrievedPatient.get().getFullName());
        assertEquals(Gender.MALE, retrievedPatient.get().getGender());
    }

    @Test
    void shouldSaveAndRetrieveReading() {
        // Given a reading for the test patient
        Reading reading = new Reading();
        reading.setPatient(testPatient);
        reading.setSystolic(120);
        reading.setDiastolic(80);
        reading.setHeartRate(72);
        reading.setArm(Arm.LEFT);
        reading.setBodyPosition(BodyPosition.SITTING);
        reading.setTimestamp(LocalDateTime.now());
        reading = readingRepository.save(reading);

        // When retrieving the reading
        Optional<Reading> retrievedReading = readingRepository.findById(reading.getId());

        // Then the reading should be found
        assertTrue(retrievedReading.isPresent());
        assertEquals(120, retrievedReading.get().getSystolic());
        assertEquals(80, retrievedReading.get().getDiastolic());
        assertEquals(72, retrievedReading.get().getHeartRate());
    }

    @Test
    void shouldFindTop3ReadingsByPatient() {
        // Given 5 readings for the test patient with different timestamps
        for (int i = 0; i < 5; i++) {
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

        // When retrieving the top 3 readings
        List<Reading> top3Readings = readingRepository.findTop3ByPatientOrderByTimestampDesc(testPatient);

        // Then only 3 readings should be returned, ordered by timestamp desc
        assertEquals(3, top3Readings.size());
        assertTrue(top3Readings.get(0).getTimestamp().isAfter(top3Readings.get(1).getTimestamp()));
        assertTrue(top3Readings.get(1).getTimestamp().isAfter(top3Readings.get(2).getTimestamp()));
    }

    @Test
    void shouldFindLatestReadingByPatient() {
        // Given 3 readings for the test patient with different timestamps
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 3; i++) {
            Reading reading = new Reading();
            reading.setPatient(testPatient);
            reading.setSystolic(120 + i * 10);
            reading.setDiastolic(80 + i * 5);
            reading.setHeartRate(72);
            reading.setArm(Arm.LEFT);
            reading.setBodyPosition(BodyPosition.SITTING);
            reading.setTimestamp(now.minusDays(i));
            readingRepository.save(reading);
        }

        // When retrieving the latest reading
        Optional<Reading> latestReading = readingRepository.findFirstByPatientOrderByTimestampDesc(testPatient);

        // Then the latest reading should be found
        assertTrue(latestReading.isPresent());
        assertEquals(140, latestReading.get().getSystolic()); // The latest reading has systolic 140
    }

    @Test
    void shouldCountReadingsByPatient() {
        // Given 3 readings for the test patient
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

        // When counting readings for the patient
        int count = readingRepository.countByPatient(testPatient);

        // Then the count should be 3
        assertEquals(3, count);
    }

    @Test
    void shouldFindAllReadingsByPatientOrderedByTimestamp() {
        // Given 3 readings for the test patient with different timestamps
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

        // When retrieving all readings for the patient
        List<Reading> allReadings = readingRepository.findAllByPatientOrderByTimestampDesc(testPatient);

        // Then all 3 readings should be returned, ordered by timestamp desc
        assertEquals(3, allReadings.size());
        assertTrue(allReadings.get(0).getTimestamp().isAfter(allReadings.get(1).getTimestamp()));
        assertTrue(allReadings.get(1).getTimestamp().isAfter(allReadings.get(2).getTimestamp()));
    }
}
