package com.chikere.bp.bptracker.dto;

import com.chikere.bp.bptracker.model.enums.Arm;
import com.chikere.bp.bptracker.model.enums.BodyPosition;
import lombok.Data;

import java.util.UUID;

@Data
public class ReadingDto {
    private UUID id;
    private UUID patientId;
    private String timestamp;  // As ISO datetime string
    private int systolic;
    private int diastolic;
    private int heartRate;
    private BodyPosition bodyPosition;
    private Arm arm;
    private String notes;
    private String deviceId;
}
