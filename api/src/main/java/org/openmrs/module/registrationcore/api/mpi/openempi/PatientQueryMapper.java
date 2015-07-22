package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.module.registrationcore.api.impl.IdentifierGenerator;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;

import java.util.*;

public class PatientQueryMapper {

    private IdentifierGenerator identifierGenerator;

    public void setIdentifierGenerator(IdentifierGenerator identifierGenerator) {
        this.identifierGenerator = identifierGenerator;
    }

    public OpenEmpiPatientQuery convert(Patient patient) {
        OpenEmpiPatientQuery patientQuery = new OpenEmpiPatientQuery();

        patientQuery.setFamilyName(patient.getFamilyName());
        patientQuery.setGivenName(patient.getGivenName());
//        patientQuery.setMiddleName(patient.getMiddleName());
//        patientQuery.setDateOfBirth(patient.getBirthdate());

        return patientQuery;
    }

    public Patient convert(OpenEmpiPatientQuery patientQuery) {
        MpiPatient patient = new MpiPatient();
        patient = (MpiPatient) convertPatient(patient, patientQuery);
        patient.setMpiPatient(true);
        return patient;
    }

    public Patient importPatient(OpenEmpiPatientQuery patientQuery) {
        return convertPatient(new Patient(), patientQuery);
    }

    private Patient convertPatient(Patient patient, OpenEmpiPatientQuery patientQuery) {
        patient.setDateCreated(new Date());

        patient.setGender(patientQuery.getGender().getGenderCode());

        setNames(patientQuery, patient);

        setBirthdate(patientQuery, patient);

        setAddresses(patientQuery, patient);

        setIdentifiers(patientQuery, patient);
        return patient;
    }

    private void setNames(OpenEmpiPatientQuery patientQuery, Patient patient) {
        PersonName names = new PersonName();
        names.setFamilyName(patientQuery.getFamilyName());
        names.setGivenName(patientQuery.getGivenName());
        patient.setNames(new TreeSet<PersonName>(Collections.singleton(names)));
    }

    private void setBirthdate(OpenEmpiPatientQuery patientQuery, Patient patient) {
        if (patientQuery.getDateOfBirth() == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(patientQuery.getDateOfBirth());
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date clearDate = calendar.getTime();
        patient.setBirthdate(clearDate);
    }

    private void setAddresses(OpenEmpiPatientQuery patientQuery, Patient patient) {
        Set<PersonAddress> addresses = new TreeSet<PersonAddress>();
        PersonAddress address = new PersonAddress();
        address.setAddress1(patientQuery.getAddress1());
        addresses.add(address);
        patient.setAddresses(addresses);
    }

    private void setIdentifiers(OpenEmpiPatientQuery patientQuery, Patient patient) {
        setOpenMrsIdentifier(patient);
        setImportedIdentifiers(patientQuery, patient);
    }

    private void setOpenMrsIdentifier(Patient patient) {
        Integer openMrsIdentifierId = identifierGenerator.getOpenMrsIdentifier();
        addIdentifier(patient, openMrsIdentifierId, null, true);
    }

    private void setImportedIdentifiers(OpenEmpiPatientQuery patientQuery, Patient patient) {
        for (PersonIdentifiers identifier : patientQuery.getPersonIdentifiers()) {
            String idName = identifier.getIdentifierDomain().getIdentifierDomainName();
            String idValue = identifier.getIdentifier();
            Integer identifierId = identifierGenerator.getIdentifierIdByName(idName);
            addIdentifier(patient, identifierId, idValue, false);
        }
    }

    private void addIdentifier(Patient patient, Integer identifierId, String idValue, boolean preferred) {
        PatientIdentifier identifier = identifierGenerator.generateIdentifier(identifierId, idValue, null);
        if (preferred)
            identifier.setPreferred(true);
        patient.addIdentifier(identifier);
    }
}
