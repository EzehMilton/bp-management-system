package com.chikere.bp.bptracker.dto;

import com.chikere.bp.bptracker.model.enums.Gender;
import lombok.Data;

import java.util.UUID;

@Data
public class PatientDTO {
    private UUID id;
    private String fullName;
    private Gender gender;
    private String birthDate;      // As ISO date string
    private String address;
    private String phone;
    private String kinName;
    private String kinTelNumber;
    private String knownConditions;
    private String registeredAt;   // As ISO datetime string
}
