package com.chikere.bp.bptracker.mapper;

import com.chikere.bp.bptracker.dto.NewPatientDTO;
import com.chikere.bp.bptracker.dto.PatientDTO;
import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.enums.Gender;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PatientMapperTest {

    private final PatientMapper patientMapper = Mappers.getMapper(PatientMapper.class);

    @Test
    void shouldMapPatientToPatientDTO() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDate birthDate = LocalDate.of(1980, 1, 1);
        LocalDateTime registeredAt = LocalDateTime.of(2023, 1, 1, 10, 30);

        Patient patient = new Patient();
        patient.setId(id);
        patient.setFullName("John Doe");
        patient.setGender(Gender.MALE);
        patient.setBirthDate(birthDate);
        patient.setRegisteredAt(registeredAt);
        patient.setAddress("123 Main St");
        patient.setPhone("123-456-7890");
        patient.setKinName("Jane Doe");
        patient.setKinTelNumber("987-654-3210");
        patient.setKnownConditions("None");
        patient.setNotes("Test patient");

        // When
        PatientDTO patientDTO = patientMapper.toDto(patient);

        // Then
        assertEquals(id, patientDTO.getId());
        assertEquals("John Doe", patientDTO.getFullName());
        assertEquals(Gender.MALE, patientDTO.getGender());
        assertEquals(birthDate.toString(), patientDTO.getBirthDate());
        assertEquals(registeredAt.toString(), patientDTO.getRegisteredAt());
        assertEquals("123 Main St", patientDTO.getAddress());
        assertEquals("123-456-7890", patientDTO.getPhone());
        assertEquals("Jane Doe", patientDTO.getKinName());
        assertEquals("987-654-3210", patientDTO.getKinTelNumber());
        assertEquals("None", patientDTO.getKnownConditions());
        assertEquals("Test patient", patientDTO.getNotes());
    }

    @Test
    void shouldMapPatientDTOToPatient() {
        // Given
        UUID id = UUID.randomUUID();
        String birthDate = "1980-01-01";
        String registeredAt = "2023-01-01T10:30:00";

        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setId(id);
        patientDTO.setFullName("John Doe");
        patientDTO.setGender(Gender.MALE);
        patientDTO.setBirthDate(birthDate);
        patientDTO.setRegisteredAt(registeredAt);
        patientDTO.setAddress("123 Main St");
        patientDTO.setPhone("123-456-7890");
        patientDTO.setKinName("Jane Doe");
        patientDTO.setKinTelNumber("987-654-3210");
        patientDTO.setKnownConditions("None");
        patientDTO.setNotes("Test patient");

        // When
        Patient patient = patientMapper.toEntity(patientDTO);

        // Then
        assertEquals(id, patient.getId());
        assertEquals("John Doe", patient.getFullName());
        assertEquals(Gender.MALE, patient.getGender());
        assertEquals(LocalDate.of(1980, 1, 1), patient.getBirthDate());
        assertEquals("123 Main St", patient.getAddress());
        assertEquals("123-456-7890", patient.getPhone());
        assertEquals("Jane Doe", patient.getKinName());
        assertEquals("987-654-3210", patient.getKinTelNumber());
        assertEquals("None", patient.getKnownConditions());
        assertEquals("Test patient", patient.getNotes());
    }

    @Test
    void shouldMapNewPatientDTOToPatient() {
        // Given
        String birthDate = "1980-01-01";

        NewPatientDTO newPatientDTO = new NewPatientDTO();
        newPatientDTO.setFullName("John Doe");
        newPatientDTO.setGender(Gender.MALE);
        newPatientDTO.setBirthDate(birthDate);
        newPatientDTO.setAddress("123 Main St");
        newPatientDTO.setPhone("123-456-7890");
        newPatientDTO.setKinName("Jane Doe");
        newPatientDTO.setKinTelNumber("987-654-3210");
        newPatientDTO.setKnownConditions("None");
        newPatientDTO.setNotes("Test patient");

        // When
        Patient patient = patientMapper.toEntity(newPatientDTO);

        // Then
        assertNull(patient.getId()); // ID should be null for new patients
        assertEquals("John Doe", patient.getFullName());
        assertEquals(Gender.MALE, patient.getGender());
        assertEquals(LocalDate.of(1980, 1, 1), patient.getBirthDate());
        assertEquals("123 Main St", patient.getAddress());
        assertEquals("123-456-7890", patient.getPhone());
        assertEquals("Jane Doe", patient.getKinName());
        assertEquals("987-654-3210", patient.getKinTelNumber());
        assertEquals("None", patient.getKnownConditions());
        assertEquals("Test patient", patient.getNotes());
    }
}
