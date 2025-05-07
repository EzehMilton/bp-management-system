package com.chikere.bp.bptracker.controller;

import com.chikere.bp.bptracker.dto.NewPatientDTO;
import com.chikere.bp.bptracker.dto.PatientDTO;
import com.chikere.bp.bptracker.dto.ReadingDto;
import com.chikere.bp.bptracker.mapper.PatientMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class WebControllerTest {

    @Mock
    private PatientService patientService;

    @Mock
    private ReadingService readingService;

    @Mock
    private RiskService riskService;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private WebController webController;

    private MockMvc mockMvc;

    private UUID patientId;
    private Patient patient;
    private PatientDTO patientDTO;
    private List<ReadingDto> readings;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(webController).build();

        patientId = UUID.randomUUID();

        patient = new Patient();
        patient.setId(patientId);
        patient.setFullName("John Doe");
        patient.setGender(Gender.MALE);

        patientDTO = new PatientDTO();
        patientDTO.setId(patientId);
        patientDTO.setFullName("John Doe");
        patientDTO.setGender(Gender.MALE);


        readings = List.of(new ReadingDto());
    }

    @Test
    void homePageShouldReturnIndexView() throws Exception {
        List<Patient> patientEntities = Collections.singletonList(patient);
        when(patientService.findAll()).thenReturn(patientEntities);
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void listPatientsShouldReturnListView() throws Exception {
        List<Patient> patientEntities = Collections.singletonList(patient);
        when(patientService.findAll()).thenReturn(patientEntities);

        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/list"));
    }

    @Test
    void viewPatientShouldReturnViewWithPatientAndReadings() throws Exception {
        when(patientService.get(patientId)).thenReturn(patient);
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);
        when(readingService.getRecentReadingsForPatient(patientId)).thenReturn(readings);
        when(readingService.hasAtLeastThreeReadings(patientId)).thenReturn(true);
        when(readingService.hasReadings(patientId)).thenReturn(true);

        mockMvc.perform(get("/patients/{id}", patientId))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/view"));
    }

    @Test
    void newPatientFormShouldReturnFormView() throws Exception {
        mockMvc.perform(get("/patients/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/new"))
                .andExpect(model().attributeExists("patient"))
                .andExpect(model().attributeExists("genders"));
    }

    @Test
    void createPatientShouldRedirectToHome() throws Exception {
        NewPatientDTO newPatientDTO = new NewPatientDTO();
        newPatientDTO.setFullName("Jane Doe");
        newPatientDTO.setGender(Gender.FEMALE);

        when(patientMapper.toEntity(any(NewPatientDTO.class))).thenReturn(patient);
        when(patientService.createPatient(any(Patient.class))).thenReturn(patient);

        mockMvc.perform(post("/patients/new")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("fullName", "Jane Doe")
                .param("gender", "FEMALE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void editPatientFormShouldReturnFormView() throws Exception {
        when(patientService.get(patientId)).thenReturn(patient);
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        mockMvc.perform(get("/patients/{id}/edit", patientId))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/edit"))
                .andExpect(model().attributeExists("genders"));
    }

    @Test
    void updatePatientShouldRedirectToPatientView() throws Exception {
        when(patientMapper.toEntity(any(PatientDTO.class))).thenReturn(patient);

        mockMvc.perform(post("/patients/{id}/edit", patientId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("fullName", "John Doe Updated")
                .param("gender", "MALE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients/" + patientId));
    }

    @Test
    void deletePatientShouldRedirectToPatientsList() throws Exception {
        mockMvc.perform(post("/patients/{id}/delete", patientId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients"));

        verify(patientService).deletePatient(patientId);
    }

    @Test
    void searchPatientsShouldReturnListView() throws Exception {
        List<Patient> patientEntities = Collections.singletonList(patient);
        when(patientService.search(anyString())).thenReturn(patientEntities);

        mockMvc.perform(get("/patients/search").param("name", "John"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/list"))
                .andExpect(model().attribute("searchTerm", "John"));
    }

    @Test
    void riskAssessmentShouldReturnAssessmentView() throws Exception {
        when(readingService.hasAtLeastThreeReadings(patientId)).thenReturn(true);
        when(patientService.get(patientId)).thenReturn(patient);
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        mockMvc.perform(get("/patients/{patientId}/risk", patientId))
                .andExpect(status().isOk())
                .andExpect(view().name("risk/assessment"));
    }

    @Test
    void analyzeRiskWithAIShouldReturnRiskLevel() throws Exception {
        when(readingService.hasAtLeastThreeReadings(patientId)).thenReturn(true);
        when(riskService.accessRiskWithAI(patientId)).thenReturn("NORMAL");

        mockMvc.perform(get("/v1/api/risk/{patientId}/analyzeAI", patientId))
                .andExpect(status().isOk())
                .andExpect(content().string("NORMAL"));
    }
}
