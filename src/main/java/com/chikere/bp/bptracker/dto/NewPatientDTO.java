package com.chikere.bp.bptracker.dto;

import com.chikere.bp.bptracker.model.enums.Gender;
import lombok.Data;

@Data
public class NewPatientDTO {
    private String fullName;
    private Gender gender;
    private String birthDate;
    private String address;
    private String phone;
    private String kinName;
    private String kinTelNumber;
    private String knownConditions;
}
