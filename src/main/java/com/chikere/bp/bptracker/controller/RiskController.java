package com.chikere.bp.bptracker.controller;

import com.chikere.bp.bptracker.service.RiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/risk")
public class RiskController {
    private final RiskService riskService;

    @GetMapping("/{patientId}")
    public ResponseEntity<String> riskCheckWithAi(@PathVariable UUID patientId) {
        try {
            String riskLevelString = riskService.accessRiskWithAI(patientId);
            RiskService.RiskLevel level = RiskService.RiskLevel.valueOf(riskLevelString);
            return ResponseEntity.ok(level.name());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid risk level: " + e.getMessage());
        }
    }
}