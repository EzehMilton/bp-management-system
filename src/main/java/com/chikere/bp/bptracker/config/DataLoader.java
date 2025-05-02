package com.chikere.bp.bptracker.config;

import com.chikere.bp.bptracker.model.Patient;
import com.chikere.bp.bptracker.model.enums.Gender;
import com.chikere.bp.bptracker.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Initializes the application with sample data upon startup.
 * <p>
 * This component implements {@link CommandLineRunner} to execute code after
 * the application context is loaded but before the application starts accepting
 * requests. It's used here to populate the database with initial patient data
 * for development and testing purposes.
 * </p>
 */

@Component
@Profile({"dev", "test"})
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final PatientRepository patientRepository;
    /**
     * Executes the data loading process when the application starts.
     * <p>
     * This method clears any existing patient records and populates the database
     * with five sample patient profiles. Each patient is created with a complete
     * set of demographic information.
     * </p>
     *
     * @param args Command line arguments passed to the application
     * @throws Exception If an error occurs during data loading
     */

    @Override
    public void run(String... args) throws Exception {

        // seed 5 patients
        if (patientRepository.count() == 0) {
            System.out.println("Seeding database with sample patient data...");
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
    }

    /**
     * Helper method to create a new Patient entity with the provided attributes.
     * <p>
     * This method simplifies the creation of Patient objects with consistent
     * attribute assignment, improving code readability in the run method.
     * </p>
     *
     * @param fullName       The patient's full name
     * @param gender         The patient's gender
     * @param dob            The patient's date of birth
     * @param phone          The patient's phone number
     * @param address        The patient's address
     * @param kinPhone       Emergency contact phone number
     * @param conditions     Known medical conditions
     * @return               A new Patient entity with all attributes set
     */

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