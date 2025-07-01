package org.openmrs.performance.registries;

import io.gatling.javaapi.core.ChainBuilder;
import org.openmrs.performance.http.DoctorHttpService;

import java.util.List;
import java.util.Set;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.foreach;
import static io.gatling.javaapi.core.CoreDsl.pause;
import static org.openmrs.performance.Constants.ALLERGY_REACTION_UUID;
import static org.openmrs.performance.Constants.ARTERIAL_BLOOD_OXYGEN_SATURATION;
import static org.openmrs.performance.Constants.DIABETIC_FOOT_ULCER_CONCEPT;
import static org.openmrs.performance.Constants.DIABETIC_KETOSIS_CONCEPT;
import static org.openmrs.performance.Constants.DIASTOLIC_BLOOD_PRESSURE;
import static org.openmrs.performance.Constants.DRUG_ALLERGEN_UUID;
import static org.openmrs.performance.Constants.ENVIRONMENTAL_ALLERGEN_UUID;
import static org.openmrs.performance.Constants.FOOD_ALLERGEN_UUID;
import static org.openmrs.performance.Constants.HEIGHT_CM;
import static org.openmrs.performance.Constants.MID_UPPER_ARM_CIRCUMFERENCE;
import static org.openmrs.performance.Constants.PERSON_ATTRIBUTE_PHONE_NUMBER;
import static org.openmrs.performance.Constants.PULSE;
import static org.openmrs.performance.Constants.RESPIRATORY_RATE;
import static org.openmrs.performance.Constants.SOAP_NOTE_TEMPLATE;
import static org.openmrs.performance.Constants.SYSTOLIC_BLOOD_PRESSURE;
import static org.openmrs.performance.Constants.TEMPERATURE_C;
import static org.openmrs.performance.Constants.UNKNOWN_OBSERVATION_TYPE;
import static org.openmrs.performance.Constants.WEIGHT_KG;
import static org.openmrs.performance.Constants.BLOODWORK;
import static org.openmrs.performance.Constants.HEMATOLOGY;
import static org.openmrs.performance.Constants.HIV_VIRAL_LOAD;

public class DoctorRegistry extends Registry<DoctorHttpService> {

	public DoctorRegistry() {
		super(new DoctorHttpService());
	}

	public ChainBuilder getVisitsFromNewEndpoint(String patientUuid) {
		return exec(httpService.getVisitWithDiagnosesAndNotes(patientUuid));
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
		return exec(httpService.getDrugOrdersExceptCancelledAndExpired(patientUuid))
		        .exec(httpService.getDrugOrdersExceptDiscontinuedOrders(patientUuid))
		        .exec(httpService.getActiveVisitOfPatient(patientUuid));
	}

	public ChainBuilder openOrdersTab(String patientUuid) {
		return exec(httpService.getOrderTypes()).exec(httpService.getAllActiveOrders(patientUuid));
	}

	public ChainBuilder openLabResultsTab(String patientUuid) {
		return exec(httpService.getObservationTree(patientUuid, HEMATOLOGY))
		        .exec(httpService.getObservationTree(patientUuid, BLOODWORK))
		        .exec(httpService.getObservationTree(patientUuid, HIV_VIRAL_LOAD))
		        .exec(httpService.getLabResults(patientUuid)).doIf(session -> session.contains("conceptIDs"))
		        .then(foreach("#{conceptIDs}", "conceptId").on(exec(httpService.getConcept("#{conceptId}"))));
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
		return exec(httpService.getPatientConditions(patientUuid));
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
		return exec(httpService.getVisitsOfPatient(patientUuid)).exec(httpService.getPatientEncounters(patientUuid))
		        .exec(httpService.getLabResults(patientUuid)).doIf(session -> session.contains("conceptIDs"))
		        .then(foreach("#{conceptIDs}", "conceptId").on(exec(httpService.getConcept("#{conceptId}"))));
	}

	public ChainBuilder openAppointmentsTab(String patientUuid) {
		return exec(httpService.getAppointmentsOfPatient(patientUuid));
	}

	public ChainBuilder addDrugOrder(String patientUuid) {
		return exec(httpService.getActiveVisitOfPatient(patientUuid), httpService.searchForDrug("asprin"),
		    httpService.searchForDrug("Tylenol"), httpService.saveOrder());

	}

	public ChainBuilder discontinueDrugOrder() {
		return exec(httpService.discontinueDrugOrder());
	}

	public ChainBuilder addCondition(String patientUuid, String currentUserUuid) {
		return exec(httpService.getPatientConditions(patientUuid), pause(2), httpService.searchForConditions("Pa"), pause(1),
		    httpService.searchForConditions("Pain"), pause(1), httpService.saveCondition(patientUuid, currentUserUuid));
	}

	public ChainBuilder addVisitNote(String patientUuid, String currentUserUuid) {
		String visitNoteText = "Patient visit note";
		String certainty = "PROVISIONAL";
		String encounterUuid = "#{encounterUuid}";

		return exec(httpService.saveVisitNote(patientUuid, currentUserUuid, visitNoteText),
		    httpService.saveDiagnosis(patientUuid, encounterUuid, DIABETIC_KETOSIS_CONCEPT, certainty, 1),
		    httpService.saveDiagnosis(patientUuid, encounterUuid, DIABETIC_FOOT_ULCER_CONCEPT, certainty, 2));
	}

	public ChainBuilder openEditPatientTab(String patientUuid) {
		return exec(httpService.getAddressTemplate(), httpService.getPatientIdentifierTypes(),
		    httpService.getPrimaryIdentifierTermMapping(), httpService.getRelationshipTypes(),
		    httpService.getModuleInformation(), httpService.getPersonAttributeType(PERSON_ATTRIBUTE_PHONE_NUMBER),
		    httpService.getAutoGenerationOptions(), httpService.getOrderedAddressHierarchyLevels(),
		    httpService.getIdentifierSources(), httpService.getPatientLifeStatus(patientUuid),
		    httpService.getPatientIdPhoto(patientUuid), httpService.getPatientSummaryData(patientUuid),
		    httpService.getPatientAttributes(patientUuid), httpService.getPatientIdentifiers(patientUuid),
		    httpService.getPatientRelationships(patientUuid));
	}

	public ChainBuilder editPatientDetails(String patientUuid) {
		return exec(httpService.editPatientDetails(patientUuid));
	}

	public ChainBuilder addProgramEnrollment(String patientUuid) {
		return exec(httpService.getProgramEnrollments(patientUuid), httpService.getPrograms(), httpService.getLocations(),
		    httpService.getLocationsByTag("Login+Location"), httpService.addProgramEnrollment());
	}

	public ChainBuilder completeProgramEnrollment(String patientUuid) {
		return exec(httpService.completeProgramEnrollment("#{programUuid}"), httpService.getProgramEnrollments(patientUuid));
	}

	public ChainBuilder openClinicalFormWorkspace(String patientUuid) {
		return exec(httpService.getActiveVisitOfPatient(patientUuid), httpService.getSpecificVisitDetails("#{visitUuid}"),
		    httpService.getPatientFormEncounters(patientUuid), httpService.getAllClinicalForms());
	}

	public ChainBuilder openSoapTemplateForm(String patientUuid) {
		return exec(httpService.getActiveVisitOfPatient(patientUuid), httpService.getSpecificVisitDetails("#{visitUuid}"),
				httpService.getSpecificClinicalForm(SOAP_NOTE_TEMPLATE), httpService.getPatientSummaryData(patientUuid),
				httpService.getEncounterRoles(), httpService.getLatestVisitNoteEncounter(patientUuid),
				httpService.getEncounterByUuid("#{clinicalEncounterUuid}")).exec(session -> {

					List<String> clinicalFormUuids = session.get("clinicalFormUuid");
					if (clinicalFormUuids != null) {
						String clinicalConceptRef = String.join(",", clinicalFormUuids);
						session.remove("clinicalFormUuid");
						session.set("clinicalConceptRef", clinicalConceptRef);
					}
					return session;
				}).doIf(session -> session.contains("clinicalConceptRef"))
				.then(exec(httpService.getConcepts("#{clinicalConceptRef}")));
	}

	public ChainBuilder saveSoapTemplateForm(String patientUuid) {
		return exec(httpService.saveSoapTemplateClinicalForm(), httpService.getActiveVisitOfPatient(patientUuid),
		    httpService.getSpecificVisitDetails("#{visitUuid}"));
	}

}
