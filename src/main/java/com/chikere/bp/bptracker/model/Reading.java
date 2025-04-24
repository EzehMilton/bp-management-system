package com.chikere.bp.bptracker.model;

import com.chikere.bp.bptracker.model.enums.Arm;
import com.chikere.bp.bptracker.model.enums.BodyPosition;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class Reading {
    @Id @GeneratedValue
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Patient patient;
    private LocalDateTime timestamp;
    private int systolic;
    private int diastolic;
    private int heartRate;

    @Enumerated(EnumType.STRING)
    private BodyPosition bodyPosition;
    @Enumerated(EnumType.STRING)
    private Arm arm;

    private String notes;
    private String deviceId;

    @PrePersist
    private void onCreate() {
        timestamp = LocalDateTime.now();
    }


}
