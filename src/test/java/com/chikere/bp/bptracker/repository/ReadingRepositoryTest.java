package com.chikere.bp.bptracker.repository;

import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.Reading;
import com.chikere.bp.bptracker.model.enums.Arm;
import com.chikere.bp.bptracker.model.enums.BodyPosition;
import com.chikere.bp.bptracker.model.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ReadingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReadingRepository readingRepository;

    private Patient patient;
    private Reading reading1;
    private Reading reading2;
    private Reading reading3;
    private Reading reading4;

    @BeforeEach
    void setUp() {
        // Create test patient
        patient = new Patient();
        patient.setFullName("John Doe");
        patient.setGender(Gender.MALE);
        patient.setBirthDate(LocalDate.of(1980, 1, 1));
        entityManager.persist(patient);
        
        // Create test readings with different timestamps
        reading1 = new Reading();
        reading1.setPatient(patient);
        reading1.setSystolic(120);
        reading1.setDiastolic(80);
        reading1.setHeartRate(72);
        reading1.setBodyPosition(BodyPosition.SITTING);
        reading1.setArm(Arm.LEFT);
        reading1.setTimestamp(LocalDateTime.now().minusDays(3));
        
        reading2 = new Reading();
        reading2.setPatient(patient);
        reading2.setSystolic(130);
        reading2.setDiastolic(85);
        reading2.setHeartRate(75);
        reading2.setBodyPosition(BodyPosition.STANDING);
        reading2.setArm(Arm.RIGHT);
        reading2.setTimestamp(LocalDateTime.now().minusDays(2));
        
        reading3 = new Reading();
        reading3.setPatient(patient);
        reading3.setSystolic(125);
        reading3.setDiastolic(82);
        reading3.setHeartRate(70);
        reading3.setBodyPosition(BodyPosition.SITTING);
        reading3.setArm(Arm.LEFT);
        reading3.setTimestamp(LocalDateTime.now().minusDays(1));
        
        reading4 = new Reading();
        reading4.setPatient(patient);
        reading4.setSystolic(135);
        reading4.setDiastolic(88);
        reading4.setHeartRate(78);
        reading4.setBodyPosition(BodyPosition.LYING);
        reading4.setArm(Arm.RIGHT);
        reading4.setTimestamp(LocalDateTime.now());
        
        // Save readings to the test database
        entityManager.persist(reading1);
        entityManager.persist(reading2);
        entityManager.persist(reading3);
        entityManager.persist(reading4);
        entityManager.flush();
    }

    @Test
    void findTop3ByPatientOrderByTimestampDescShouldReturnLatestThreeReadings() {
        List<Reading> readings = readingRepository.findTop3ByPatientOrderByTimestampDesc(patient);
        
        assertEquals(3, readings.size());
        assertEquals(reading4.getId(), readings.get(0).getId()); // Most recent
        assertEquals(reading3.getId(), readings.get(1).getId());
        assertEquals(reading2.getId(), readings.get(2).getId());
        // reading1 should not be included as it's the oldest
    }

    @Test
    void findFirstByPatientOrderByTimestampDescShouldReturnLatestReading() {
        Optional<Reading> latestReading = readingRepository.findFirstByPatientOrderByTimestampDesc(patient);
        
        assertTrue(latestReading.isPresent());
        assertEquals(reading4.getId(), latestReading.get().getId());
    }

    @Test
    void countByPatientShouldReturnCorrectCount() {
        int count = readingRepository.countByPatient(patient);
        
        assertEquals(4, count);
    }

    @Test
    void findAllByPatientOrderByTimestampDescShouldReturnAllReadingsInOrder() {
        List<Reading> readings = readingRepository.findAllByPatientOrderByTimestampDesc(patient);
        
        assertEquals(4, readings.size());
        assertEquals(reading4.getId(), readings.get(0).getId()); // Most recent
        assertEquals(reading3.getId(), readings.get(1).getId());
        assertEquals(reading2.getId(), readings.get(2).getId());
        assertEquals(reading1.getId(), readings.get(3).getId()); // Oldest
    }
}