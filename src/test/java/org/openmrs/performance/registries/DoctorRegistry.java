package org.openmrs.performance.registries;

import io.gatling.javaapi.core.ChainBuilder;
import org.openmrs.performance.http.DoctorHttpService;

import java.util.List;
import java.util.Set;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static org.openmrs.performance.Constants.ALLERGY_REACTION_UUID;
import static org.openmrs.performance.Constants.ARTERIAL_BLOOD_OXYGEN_SATURATION;
import static org.openmrs.performance.Constants.DIABETIC_FOOT_ULCER_CONCEPT;
import static org.openmrs.performance.Constants.DIABETIC_KETOSIS_CONCEPT;
import static org.openmrs.performance.Constants.DIASTOLIC_BLOOD_PRESSURE;
import static org.openmrs.performance.Constants.DRUG_ALLERGEN_UUID;
import static org.openmrs.performance.Constants.ENVIRONMENTAL_ALLERGEN_UUID;
import static org.openmrs.performance.Constants.FACULTY_VISIT_TYPE_UUID;
import static org.openmrs.performance.Constants.FOOD_ALLERGEN_UUID;
import static org.openmrs.performance.Constants.HEIGHT_CM;
import static org.openmrs.performance.Constants.MID_UPPER_ARM_CIRCUMFERENCE;
import static org.openmrs.performance.Constants.OUTPATIENT_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.Constants.PULSE;
import static org.openmrs.performance.Constants.RESPIRATORY_RATE;
import static org.openmrs.performance.Constants.SYSTOLIC_BLOOD_PRESSURE;
import static org.openmrs.performance.Constants.TEMPERATURE_C;
import static org.openmrs.performance.Constants.UNKNOWN_OBSERVATION_TYPE;
import static org.openmrs.performance.Constants.WEIGHT_KG;
import static org.openmrs.performance.utils.CommonUtils.extractConceptIds;

public class DoctorRegistry extends Registry<DoctorHttpService> {

	public DoctorRegistry() {
		super(new DoctorHttpService());
	}

	public ChainBuilder startVisit(String patientUuid) {

		return exec(httpService.getVisitTypes()).exec(httpService.getCurrentVisit(patientUuid))
		        .exec(httpService.getVisitsOfPatient(patientUuid)).exec(httpService.getProgramEnrollments(patientUuid))
		        .exec(httpService.getVisitQueueEntry(patientUuid)).exec(httpService.getAppointments(patientUuid)).pause(5)
		        .exec(httpService.submitVisitForm(patientUuid, FACULTY_VISIT_TYPE_UUID, OUTPATIENT_CLINIC_LOCATION_UUID))
		        .exec(httpService.getCurrentVisit(patientUuid)).exec(httpService.getVisitsOfPatient(patientUuid));
	}

	public ChainBuilder endVisit(String patientUuid) {
		return exec(httpService.submitEndVisit("#{visitUuid}", OUTPATIENT_CLINIC_LOCATION_UUID, FACULTY_VISIT_TYPE_UUID))
		        .exec(httpService.getCurrentVisit(patientUuid)).exec(httpService.getVisitsOfPatient(patientUuid));
	}

	public ChainBuilder openVitalsAndBiometricsTab(String patientUuid) {

		Set<String> vitals = Set.of(SYSTOLIC_BLOOD_PRESSURE, DIASTOLIC_BLOOD_PRESSURE, PULSE, TEMPERATURE_C,
		    ARTERIAL_BLOOD_OXYGEN_SATURATION, RESPIRATORY_RATE, UNKNOWN_OBSERVATION_TYPE);

		Set<String> biometrics = Set.of(HEIGHT_CM, WEIGHT_KG, MID_UPPER_ARM_CIRCUMFERENCE);
		return exec(httpService.getPatientObservations(patientUuid, vitals))
		        .exec(httpService.getPatientObservations(patientUuid, biometrics));
	}

	public ChainBuilder recordVitals(String patientUuid) {
		return exec(httpService.saveVitalsData(patientUuid));
	}

	public ChainBuilder openMedicationsTab(String patientUuid) {
		return exec(httpService.getDrugOrders(patientUuid));
	}

	public ChainBuilder openOrdersTab(String patientUuid) {
		return exec(httpService.getOrderTypes()).exec(httpService.getAllActiveOrders(patientUuid));
	}

	public ChainBuilder openLabResultsTab(String patientUuid) {
		return exec(httpService.getLabResults(patientUuid)).exec(session -> {
			// Extract concept IDs from the lab results response
			String response = session.getString("labResultsResponse");
			List<String> conceptIds = extractConceptIds(response);
			// Save concept IDs in the session
			return session.set("labResultConceptIds", conceptIds);
		}).foreach("#{labResultConceptIds}", "conceptId").on(exec(httpService.getConcept("#{conceptId}")));
	}

	public ChainBuilder openAllergiesTab(String patientUuid) {
		return exec(httpService.getAllergies(patientUuid));
	}

	public ChainBuilder openAllergiesForm() {
		return exec(httpService.getAllergens("Drug", DRUG_ALLERGEN_UUID),
		    httpService.getAllergens("Environment", ENVIRONMENTAL_ALLERGEN_UUID),
		    httpService.getAllergens("Food", FOOD_ALLERGEN_UUID),
		    httpService.getAllergens("Allergic Reactions", ALLERGY_REACTION_UUID));
	}

	public ChainBuilder recordAllergy(String patientUuid) {
		return exec(httpService.saveAllergy(patientUuid));
	}

	public ChainBuilder openConditionsTab(String patientUuid) {
		return exec(httpService.getConditions(patientUuid));
	}

	public ChainBuilder openImmunizationsTab(String patientUuid) {
		return exec(httpService.getImmunizations(patientUuid));
	}

	public ChainBuilder openAttachmentsTab(String patientUuid) {
		return exec(httpService.getAttachments(patientUuid)).exec(httpService.getAllowedFileExtensions());
	}

	public ChainBuilder openProgramsTab(String patientUuid) {
		return exec(httpService.getPrograms()).exec(httpService.getProgramEnrollments(patientUuid));
	}

	public ChainBuilder addAttachment(String patientUuid) {
		return exec(httpService.uploadAttachment(patientUuid)).exec(httpService.getAttachments(patientUuid));
	}

	public ChainBuilder openVisitsTab(String patientUuid) {
		return exec(httpService.getVisitsOfPatient(patientUuid));
	}

	public ChainBuilder openAppointmentsTab(String patientUuid) {
		return exec(httpService.getAppointments(patientUuid));
	}

	public ChainBuilder addDrugOrder(String patientUuid, String visitUuid, String currentUserUuid) {
		String asprin_162_5mg = "a722710f-403b-451f-804b-09f8624b0838";
		String asprinConcept = "71617AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		return exec(httpService.getActiveVisitOfPatient(patientUuid), httpService.searchForDrug("asprin"),
		    httpService.searchForDrug("Tylenol"),
		    httpService.saveOrder(patientUuid, visitUuid, currentUserUuid, asprin_162_5mg, asprinConcept));

	}

	public ChainBuilder addVisitNote(String patientUuid, String currentUserUuid) {
		String visitNoteText = "Patient visit note";
		String certainty = "PROVISIONAL";
		String encounterUuid = "#{encounterUuid}";

		return exec(httpService.saveVisitNote(patientUuid, currentUserUuid, visitNoteText),
		    httpService.saveDiagnosis(patientUuid, encounterUuid, DIABETIC_KETOSIS_CONCEPT, certainty, 1),
		    httpService.saveDiagnosis(patientUuid, encounterUuid, DIABETIC_FOOT_ULCER_CONCEPT, certainty, 2));
	}
}
