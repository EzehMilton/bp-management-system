package com.chikere.bp.bptracker.model;

import com.chikere.bp.bptracker.model.enums.Gender;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class Patient {
    @Id @GeneratedValue
    private UUID id;
    private String fullName;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private LocalDate birthDate;
    private String address;
    private String phone;
    private String kinName;
    private String kinTelNumber;
    private String knownConditions;
    @Column(columnDefinition = "TEXT")
    private String notes;
    private LocalDateTime registeredAt;

    @PrePersist
    public void prePersist(){
        this.registeredAt = LocalDateTime.now();
    }

}
