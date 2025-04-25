package com.chikere.bp.bptracker.service;

import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.repository.PatientRepository;
import com.chikere.bp.bptracker.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patientId = UUID.randomUUID();
        patient = new Patient();
        patient.setId(patientId);
        patient.setFullName("John Doe");
    }

    @Test
    void createsPatientSuccessfully() {
        when(patientRepository.save(patient)).thenReturn(patient);

        Patient result = patientService.createPatient(patient);

        assertEquals(patient, result);
        verify(patientRepository, times(1)).save(patient);
    }

    @Test
    void findsPatientByIdSuccessfully() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        Patient result = patientService.findById(patientId);

        assertEquals(patient, result);
        verify(patientRepository, times(1)).findById(patientId);
    }

    @Test
    void returnsNullWhenPatientNotFoundById() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        Patient result = patientService.findById(patientId);

        assertNull(result);
        verify(patientRepository, times(1)).findById(patientId);
    }

    @Test
    void searchesPatientsByNameSuccessfully() {
        List<Patient> patients = List.of(patient);
        when(patientRepository.findByFullNameContainingIgnoreCase("John")).thenReturn(patients);

        List<Patient> result = patientService.search("John");

        assertEquals(patients, result);
        verify(patientRepository, times(1)).findByFullNameContainingIgnoreCase("John");
    }

    @Test
    void getsPatientByIdSuccessfully() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        Patient result = patientService.get(patientId);

        assertEquals(patient, result);
        verify(patientRepository, times(1)).findById(patientId);
    }

    @Test
    void throwsExceptionWhenPatientNotFoundById() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> patientService.get(patientId));
        verify(patientRepository, times(1)).findById(patientId);
    }

    @Test
    void findsAllPatientsSuccessfully() {
        List<Patient> patients = List.of(patient);
        when(patientRepository.findAll()).thenReturn(patients);

        List<Patient> result = patientService.findAll();

        assertEquals(patients, result);
        verify(patientRepository, times(1)).findAll();
    }
}
