package com.chikere.bp.bptracker.mapper;

import com.chikere.bp.bptracker.dto.NewPatientDTO;
import com.chikere.bp.bptracker.dto.PatientDTO;
import com.chikere.bp.bptracker.model.Patient;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    //** Patient → PatientDTO **/
    @Mapping(source = "birthDate",  target = "birthDate",  dateFormat = "yyyy-MM-dd")
    @Mapping(source = "registeredAt", target = "registeredAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    PatientDTO toDto(Patient patient);

    /** Inverse of above: PatientDTO → Patient **/
    @InheritInverseConfiguration
    Patient toEntity(PatientDTO dto);

    /** NewPatientDTO → Patient (no id/registeredAt yet) **/
    @Mapping(source = "birthDate", target = "birthDate", dateFormat = "yyyy-MM-dd")
    Patient toEntity(NewPatientDTO newPatientDto);
}
