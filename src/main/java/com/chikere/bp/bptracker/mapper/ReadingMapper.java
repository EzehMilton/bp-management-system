package com.chikere.bp.bptracker.mapper;

import com.chikere.bp.bptracker.dto.NewReadingDto;
import com.chikere.bp.bptracker.dto.ReadingDto;
import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.Reading;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ReadingMapper {
    /** Reading → ReadingDto **/
    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "timestamp", target = "timestamp", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    ReadingDto toDto(Reading reading);

    /** Inverse of above: ReadingDto → Reading **/
    @InheritInverseConfiguration
    @Mapping(target = "patient", ignore = true)
    Reading toEntity(ReadingDto dto);

    /** NewReadingDto → Reading (no id/timestamp yet) **/
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "patient", ignore = true)
    Reading toEntity(NewReadingDto newReadingDto);

    /** Helper method to set patient in Reading entity **/
    default Reading withPatient(Reading reading, Patient patient) {
        reading.setPatient(patient);
        return reading;
    }

    /** Helper method to set patient ID in Reading entity **/
    default Reading withPatientId(Reading reading, UUID patientId) {
        Patient patient = new Patient();
        patient.setId(patientId);
        reading.setPatient(patient);
        return reading;
    }
}