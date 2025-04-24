package com.chikere.bp.bptracker.config;

import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.enums.Gender;
import com.chikere.bp.bptracker.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final PatientRepository patientRepository;

    @Override
    public void run(String... args) throws Exception {
        // clear existing (optional)
        patientRepository.deleteAll();

        // seed 5 patients
        patientRepository.save(create("Angelina Ezeh",   Gender.FEMALE,  LocalDate.of(2009, 10, 15),
                                "0712345678","Village A","0712300000","None"));
        patientRepository.save(create("Micheal Ezeh",   Gender.MALE,    LocalDate.of(1997, 8,  4),
                                "0722345678","Village B","0722300000","None"));
        patientRepository.save(create("Nnuola Ezeh",Gender.FEMALE,  LocalDate.of(1966, 10, 9),
                                "0732345678","Village C","0732300000","None"));
        patientRepository.save(create("Chikere Ezeh",   Gender.MALE,    LocalDate.of(1967,1,15),
                                "0742345678","Village D","0742300000","None"));
        patientRepository.save(create("Anthony Ezeh",     Gender.MALE,  LocalDate.of(2002, 10, 20),
                                "0752345678","Village E","0752300000","None"));
    }

    private Patient create(String fullName, Gender gender, LocalDate dob,
                           String phone, String address, String kinPhone, String conditions) {
        Patient p = new Patient();
        p.setFullName(fullName);
        p.setGender(gender);
        p.setBirthDate(dob);
        p.setPhone(phone);
        p.setAddress(address);
        p.setKinTelNumber(kinPhone);
        p.setKnownConditions(conditions);
        return p;
    }
}