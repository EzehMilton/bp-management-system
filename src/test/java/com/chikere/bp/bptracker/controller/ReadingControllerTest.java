package com.chikere.bp.bptracker.controller;

import com.chikere.bp.bptracker.dto.NewReadingDto;
import com.chikere.bp.bptracker.dto.ReadingDto;
import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.enums.Arm;
import com.chikere.bp.bptracker.model.enums.BodyPosition;
import com.chikere.bp.bptracker.model.enums.Gender;
import com.chikere.bp.bptracker.service.PatientService;
import com.chikere.bp.bptracker.service.ReadingService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReadingControllerTest {

    @Mock
    private PatientService patientService;

    @Mock
    private ReadingService readingService;

    @InjectMocks
    private ReadingController readingController;

    private MockMvc mockMvc;

    private UUID patientId;
    private UUID readingId;
    private Patient patient;
    private ReadingDto readingDto;
    private List<ReadingDto> readings;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(readingController).build();

        patientId = UUID.randomUUID();
        readingId = UUID.randomUUID();

        patient = new Patient();
        patient.setId(patientId);
        patient.setFullName("John Doe");
        patient.setGender(Gender.MALE);

        readingDto = new ReadingDto();
        readingDto.setId(readingId);
        readingDto.setPatientId(patientId);
        readingDto.setSystolic(120);
        readingDto.setDiastolic(80);
        readingDto.setHeartRate(72);

        readings = List.of(readingDto);
    }

    @Test
    void listReadingsShouldReturnListView() throws Exception {
        when(readingService.findAll()).thenReturn(readings);

        mockMvc.perform(get("/readings"))
                .andExpect(status().isOk())
                .andExpect(view().name("readings/list"))
                .andExpect(model().attributeExists("readings"));
    }

    @Test
    void viewReadingShouldReturnViewWithReading() throws Exception {
        when(readingService.getById(readingId)).thenReturn(readingDto);

        mockMvc.perform(get("/readings/{id}", readingId))
                .andExpect(status().isOk())
                .andExpect(view().name("readings/view"))
                .andExpect(model().attributeExists("reading"));
    }

    @Test
    void newReadingFormShouldReturnFormView() throws Exception {
        List<Patient> patients = Collections.singletonList(patient);
        when(patientService.findAll()).thenReturn(patients);

        mockMvc.perform(get("/readings/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("readings/new"))
                .andExpect(model().attributeExists("reading"))
                .andExpect(model().attributeExists("patients"))
                .andExpect(model().attributeExists("bodyPositions"))
                .andExpect(model().attributeExists("arms"));
    }

    @Test
    void newReadingForPatientShouldReturnFormView() throws Exception {
        when(patientService.get(patientId)).thenReturn(patient);

        mockMvc.perform(get("/patients/{patientId}/readings/new", patientId))
                .andExpect(status().isOk())
                .andExpect(view().name("readings/new"))
                .andExpect(model().attributeExists("reading"))
                .andExpect(model().attributeExists("patient"))
                .andExpect(model().attributeExists("bodyPositions"))
                .andExpect(model().attributeExists("arms"));
    }

    @Test
    void createReadingShouldRedirectToReadingView() throws Exception {
        NewReadingDto newReadingDto = new NewReadingDto();
        newReadingDto.setPatientId(patientId);
        newReadingDto.setSystolic(120);
        newReadingDto.setDiastolic(80);
        newReadingDto.setHeartRate(72);
        newReadingDto.setArm(Arm.LEFT);
        newReadingDto.setBodyPosition(BodyPosition.SITTING);

        when(readingService.create(any(NewReadingDto.class))).thenReturn(readingDto);

        mockMvc.perform(post("/readings/new")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("patientId", patientId.toString())
                .param("systolic", "120")
                .param("diastolic", "80")
                .param("heartRate", "72")
                .param("arm", "LEFT")
                .param("bodyPosition", "SITTING"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/readings/" + readingId));
    }

    @Test
    void downloadAllReadingsAsCsvShouldReturnCsvFile() throws Exception {
        String csvContent = "Patient Name,Systolic,Diastolic,Heart Rate,Arm,Body Position,Timestamp\n" +
                "John Doe,120,80,72,LEFT,SITTING,2023-01-01 12:00:00";
        when(readingService.getAllReadingsAsCsv()).thenReturn(csvContent);

        mockMvc.perform(get("/readings/download-csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"all_readings.csv\""))
                .andExpect(content().string(csvContent));
    }
}