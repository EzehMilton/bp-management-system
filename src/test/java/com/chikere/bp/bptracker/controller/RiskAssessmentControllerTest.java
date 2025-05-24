package com.chikere.bp.bptracker.controller;

import com.chikere.bp.bptracker.dto.ReadingDto;
import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.enums.Gender;
import com.chikere.bp.bptracker.service.PatientService;
import com.chikere.bp.bptracker.service.ReadingService;
import com.chikere.bp.bptracker.service.RiskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentControllerTest {

    @Mock
    private PatientService patientService;

    @Mock
    private ReadingService readingService;

    @Mock
    private RiskService riskService;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private RiskAssessmentController riskAssessmentController;

    private MockMvc mockMvc;

    private UUID patientId;
    private Patient patient;
    private ReadingDto readingDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(riskAssessmentController).build();

        patientId = UUID.randomUUID();

        patient = new Patient();
        patient.setId(patientId);
        patient.setFullName("John Doe");
        patient.setGender(Gender.MALE);

        readingDto = new ReadingDto();
        readingDto.setPatientId(patientId);
        readingDto.setSystolic(120);
        readingDto.setDiastolic(80);
        readingDto.setHeartRate(72);
    }

    @Test
    void riskAssessmentShouldReturnAssessmentView() throws Exception {
        when(readingService.hasAtLeastThreeReadings(patientId)).thenReturn(true);
        when(patientService.get(patientId)).thenReturn(patient);
        when(readingService.getLatestReadingForPatient(patientId)).thenReturn(readingDto);

        mockMvc.perform(get("/patients/{patientId}/risk", patientId))
                .andExpect(status().isOk())
                .andExpect(view().name("risk/assessment"))
                .andExpect(model().attributeExists("patient"))
                .andExpect(model().attributeExists("latestReading"));
    }

    @Test
    void riskAssessmentWithAiAnalysisShouldReturnAssessmentView() throws Exception {
        when(readingService.hasAtLeastThreeReadings(patientId)).thenReturn(true);
        when(patientService.get(patientId)).thenReturn(patient);
        when(readingService.getLatestReadingForPatient(patientId)).thenReturn(readingDto);
        when(riskService.accessRiskWithAI(patientId)).thenReturn("NORMAL");

        mockMvc.perform(get("/patients/{patientId}/risk", patientId)
                .param("analyze", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("risk/assessment"))
                .andExpect(model().attributeExists("patient"))
                .andExpect(model().attributeExists("latestReading"))
                .andExpect(model().attributeExists("aiRisk"));
    }

    @Test
    void riskAssessmentWithNotEnoughReadingsShouldRedirectToPatientView() throws Exception {
        when(readingService.hasAtLeastThreeReadings(patientId)).thenReturn(false);

        mockMvc.perform(get("/patients/{patientId}/risk", patientId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients/" + patientId));
    }

    @Test
    void captureAndAssessImmediateReadingShouldRedirectToRiskAssessment() throws Exception {
        when(riskService.captureAndAssessImmediateReading(patientId)).thenReturn("NORMAL");

        mockMvc.perform(post("/patients/{patientId}/risk/immediate", patientId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients/" + patientId + "/risk"));
    }
}