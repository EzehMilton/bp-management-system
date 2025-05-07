package com.chikere.bp.bptracker.repository;

import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PatientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PatientRepository patientRepository;

    @BeforeEach
    void setUp() {
        // Create test patients
        Patient patient1 = new Patient();
        patient1.setFullName("John Doe");
        patient1.setGender(Gender.MALE);
        patient1.setBirthDate(LocalDate.of(1980, 1, 1));

        Patient patient2 = new Patient();
        patient2.setFullName("Jane Smith");
        patient2.setGender(Gender.FEMALE);
        patient2.setBirthDate(LocalDate.of(1985, 5, 15));

        // Save patients to the test database
        entityManager.persist(patient1);
        entityManager.persist(patient2);
        entityManager.flush();
    }

    @Test
    void findByFullNameContainingIgnoreCaseShouldReturnMatchingPatients() {
        // Test with exact match
        List<Patient> results1 = patientRepository.findByFullNameContainingIgnoreCase("John Doe");
        assertEquals(1, results1.size());
        assertEquals("John Doe", results1.getFirst().getFullName());

        // Test with partial match (case insensitive)
        List<Patient> results2 = patientRepository.findByFullNameContainingIgnoreCase("john");
        assertEquals(1, results2.size());
        assertEquals("John Doe", results2.getFirst().getFullName());

        // Test with partial match for both patients
//        List<Patient> results3 = patientRepository.findByFullNameContainingIgnoreCase("o");
//        assertEquals(1, results3.size());

        // Test with another partial match for both patients
        List<Patient> results3b = patientRepository.findByFullNameContainingIgnoreCase("e");
        assertEquals(2, results3b.size());

        // Test with no match
        List<Patient> results4 = patientRepository.findByFullNameContainingIgnoreCase("XYZ");
        assertEquals(0, results4.size());
    }
}
