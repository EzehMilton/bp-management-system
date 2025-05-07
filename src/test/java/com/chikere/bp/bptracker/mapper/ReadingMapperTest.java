package com.chikere.bp.bptracker.mapper;

import com.chikere.bp.bptracker.dto.NewReadingDto;
import com.chikere.bp.bptracker.dto.ReadingDto;
import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.Reading;
import com.chikere.bp.bptracker.model.enums.Arm;
import com.chikere.bp.bptracker.model.enums.BodyPosition;
import com.chikere.bp.bptracker.model.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ReadingMapperTest {

    private final ReadingMapper readingMapper = Mappers.getMapper(ReadingMapper.class);
    
    private Patient patient;
    private UUID patientId;
    private UUID readingId;
    private LocalDateTime timestamp;
    
    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        readingId = UUID.randomUUID();
        timestamp = LocalDateTime.now();
        
        patient = new Patient();
        patient.setId(patientId);
        patient.setFullName("John Doe");
        patient.setGender(Gender.MALE);
        patient.setBirthDate(LocalDate.of(1980, 1, 1));
    }

    @Test
    void shouldMapReadingToReadingDto() {
        // Given
        Reading reading = new Reading();
        reading.setId(readingId);
        reading.setPatient(patient);
        reading.setSystolic(120);
        reading.setDiastolic(80);
        reading.setHeartRate(72);
        reading.setBodyPosition(BodyPosition.SITTING);
        reading.setArm(Arm.LEFT);
        reading.setTimestamp(timestamp);
        reading.setNotes("Test reading");
        reading.setDeviceId("device123");
        
        // When
        ReadingDto readingDto = readingMapper.toDto(reading);
        
        // Then
        assertEquals(readingId, readingDto.getId());
        assertEquals(patientId, readingDto.getPatientId());
        assertEquals(120, readingDto.getSystolic());
        assertEquals(80, readingDto.getDiastolic());
        assertEquals(72, readingDto.getHeartRate());
        assertEquals(BodyPosition.SITTING, readingDto.getBodyPosition());
        assertEquals(Arm.LEFT, readingDto.getArm());
        assertEquals(timestamp.toString(), readingDto.getTimestamp());
        assertEquals("Test reading", readingDto.getNotes());
        assertEquals("device123", readingDto.getDeviceId());
    }

    @Test
    void shouldMapReadingDtoToReading() {
        // Given
        ReadingDto readingDto = new ReadingDto();
        readingDto.setId(readingId);
        readingDto.setPatientId(patientId);
        readingDto.setSystolic(120);
        readingDto.setDiastolic(80);
        readingDto.setHeartRate(72);
        readingDto.setBodyPosition(BodyPosition.SITTING);
        readingDto.setArm(Arm.LEFT);
        readingDto.setTimestamp(timestamp.toString());
        readingDto.setNotes("Test reading");
        readingDto.setDeviceId("device123");
        
        // When
        Reading reading = readingMapper.toEntity(readingDto);
        
        // Then
        assertEquals(readingId, reading.getId());
        assertNull(reading.getPatient()); // Patient is ignored in the mapping
        assertEquals(120, reading.getSystolic());
        assertEquals(80, reading.getDiastolic());
        assertEquals(72, reading.getHeartRate());
        assertEquals(BodyPosition.SITTING, reading.getBodyPosition());
        assertEquals(Arm.LEFT, reading.getArm());
        assertNull(reading.getTimestamp()); // Timestamp is ignored in the mapping
        assertEquals("Test reading", reading.getNotes());
        assertEquals("device123", reading.getDeviceId());
    }

    @Test
    void shouldMapNewReadingDtoToReading() {
        // Given
        NewReadingDto newReadingDto = new NewReadingDto();
        newReadingDto.setPatientId(patientId);
        newReadingDto.setSystolic(120);
        newReadingDto.setDiastolic(80);
        newReadingDto.setHeartRate(72);
        newReadingDto.setBodyPosition(BodyPosition.SITTING);
        newReadingDto.setArm(Arm.LEFT);
        newReadingDto.setNotes("Test reading");
        newReadingDto.setDeviceId("device123");
        
        // When
        Reading reading = readingMapper.toEntity(newReadingDto);
        
        // Then
        assertNull(reading.getId()); // ID should be null for new readings
        assertNull(reading.getPatient()); // Patient is ignored in the mapping
        assertEquals(120, reading.getSystolic());
        assertEquals(80, reading.getDiastolic());
        assertEquals(72, reading.getHeartRate());
        assertEquals(BodyPosition.SITTING, reading.getBodyPosition());
        assertEquals(Arm.LEFT, reading.getArm());
        assertNull(reading.getTimestamp()); // Timestamp is ignored in the mapping
        assertEquals("Test reading", reading.getNotes());
        assertEquals("device123", reading.getDeviceId());
    }

    @Test
    void shouldSetPatientInReading() {
        // Given
        Reading reading = new Reading();
        
        // When
        Reading result = readingMapper.withPatient(reading, patient);
        
        // Then
        assertSame(patient, result.getPatient());
        assertSame(reading, result); // Should return the same reading instance
    }

    @Test
    void shouldSetPatientIdInReading() {
        // Given
        Reading reading = new Reading();
        
        // When
        Reading result = readingMapper.withPatientId(reading, patientId);
        
        // Then
        assertNotNull(result.getPatient());
        assertEquals(patientId, result.getPatient().getId());
        assertSame(reading, result); // Should return the same reading instance
    }
}