package org.openmrs.performance.registries;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;
import org.openmrs.performance.http.NurseHttpService;
import org.openmrs.performance.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.foreach;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.Constants.ADMISSION_ENCOUNTER_TYPE_UUID;
import static org.openmrs.performance.Constants.ALLERGY_REACTION_UUID;
import static org.openmrs.performance.Constants.ARTERIAL_BLOOD_OXYGEN_SATURATION;
import static org.openmrs.performance.Constants.CLINICAL_NOTES;
import static org.openmrs.performance.Constants.DIASTOLIC_BLOOD_PRESSURE;
import static org.openmrs.performance.Constants.DISCHARGE_ENCOUNTER_TYPE_UUID;
import static org.openmrs.performance.Constants.DRUG_ALLERGEN_UUID;
import static org.openmrs.performance.Constants.ENVIRONMENTAL_ALLERGEN_UUID;
import static org.openmrs.performance.Constants.FACULTY_VISIT_TYPE_UUID;
import static org.openmrs.performance.Constants.FOOD_ALLERGEN_UUID;
import static org.openmrs.performance.Constants.HEIGHT_CM;
import static org.openmrs.performance.Constants.INPATEINT_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.Constants.MID_UPPER_ARM_CIRCUMFERENCE;
import static org.openmrs.performance.Constants.PULSE;
import static org.openmrs.performance.Constants.RESPIRATORY_RATE;
import static org.openmrs.performance.Constants.SYSTOLIC_BLOOD_PRESSURE;
import static org.openmrs.performance.Constants.TEMPERATURE_C;
import static org.openmrs.performance.Constants.TRANSFER_DISPOSITION;
import static org.openmrs.performance.Constants.TRANSFER_LOCATION;
import static org.openmrs.performance.Constants.TRANSFER_OBSERVATION;
import static org.openmrs.performance.Constants.TRANSFER_PATIENT_REQUEST;
import static org.openmrs.performance.Constants.UNKNOWN_OBSERVATION_TYPE;
import static org.openmrs.performance.Constants.VITALS_ENCOUNTER_TYPE_UUID;
import static org.openmrs.performance.Constants.VITALS_FORM_UUID;
import static org.openmrs.performance.Constants.VITALS_LOCATION_UUID;
import static org.openmrs.performance.Constants.WARD1_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.Constants.WEIGHT_KG;

public class NurseRegistry extends Registry<NurseHttpService> {

	public NurseRegistry() {
		super(new NurseHttpService());
	}

	public ChainBuilder openWardPage(String locationUuid) {
		return exec(httpService.getAdmissionLocationInfo(locationUuid), httpService.getAdmittedPatientInfo(locationUuid),
		    httpService.getInpatientRequest(locationUuid)).doIf(session -> session.contains("admittedPatientUuid"))
		        .then(foreach("#{admittedPatientUuid}", "uuid")
		                .on(exec(httpService.getOrdersWithNullFulfillerStatusAndActivatedDate("#{uuid}"))));
	}

	public ChainBuilder admitPatientToWardPage(String patientUuid) {
		return exec(httpService.getVisitTypes(), httpService.getLocationsThatSupportVisits(),
		    httpService.getProgramEnrollments(patientUuid), httpService.getLocationsByTag("Visit+Location"),
		    httpService.getAppointmentsOfPatient(patientUuid),
		    httpService.submitVisitForm(patientUuid, FACULTY_VISIT_TYPE_UUID, INPATEINT_CLINIC_LOCATION_UUID),
		    httpService.getBedsByPatientUuid(patientUuid), httpService.saveWardEncounter("Admission",
		        Collections.emptyList(), ADMISSION_ENCOUNTER_TYPE_UUID, INPATEINT_CLINIC_LOCATION_UUID));
	}

	public ChainBuilder openingPatientDetails(String patientUuid) {
		Set<String> vitals = Set.of(SYSTOLIC_BLOOD_PRESSURE, DIASTOLIC_BLOOD_PRESSURE, PULSE, TEMPERATURE_C,
		    ARTERIAL_BLOOD_OXYGEN_SATURATION, RESPIRATORY_RATE, UNKNOWN_OBSERVATION_TYPE);

		Set<String> refRangesConcept = Set.of(SYSTOLIC_BLOOD_PRESSURE, DIASTOLIC_BLOOD_PRESSURE, PULSE, TEMPERATURE_C,
		    RESPIRATORY_RATE, ARTERIAL_BLOOD_OXYGEN_SATURATION);

		return exec(httpService.getActiveVisitOfPatient(patientUuid), httpService.getIsVisitsEnabled(),
		    httpService.getVitalConceptSetDetails(), httpService.getPatientObservations(patientUuid, vitals),
		    httpService.getVitalsConceptRefRanges(patientUuid, refRangesConcept),
		    httpService.getSpecificVisitDetails("#{visitUuid}"));
	}

	public ChainBuilder transferPatient() {
		Map<String, Object> obs = new HashMap<>();

		obs.put("concept", TRANSFER_OBSERVATION);

		Map<String, Object> transferLocationConcept = new HashMap<>();
		transferLocationConcept.put("concept", TRANSFER_LOCATION);
		transferLocationConcept.put("value", WARD1_CLINIC_LOCATION_UUID);

		Map<String, Object> groupMember = new HashMap<>();
		groupMember.put("concept", TRANSFER_DISPOSITION);
		groupMember.put("value", "CIEL:167731");

		Map<String, Object> transferLocationNote = new HashMap<>();
		transferLocationNote.put("concept", CLINICAL_NOTES);
		transferLocationNote.put("value", "test");

		obs.put("groupMembers", List.of(transferLocationConcept, groupMember, transferLocationNote));

		return exec(httpService.getCustomTransferLocationsConfiguration(), httpService.getTransferableLocations(),
		    httpService.saveWardEncounter("Transfer", List.of(obs), TRANSFER_PATIENT_REQUEST,
		        INPATEINT_CLINIC_LOCATION_UUID));
	}

	public ChainBuilder moveToDifferentLocationSession() {
		return exec(httpService.getLocations(), httpService.getAllLocationsSearchSet(),
		    httpService.changeTheSessionLocation(WARD1_CLINIC_LOCATION_UUID));
	}

	public ChainBuilder admitTheTransferPatient() {
		return exec(httpService.getPrimaryIdentifierTermMapping()).doIf(session -> session.contains("transferPatientUuid"))
		        .then(foreach("#{transferPatientUuid}", "uuid").on(exec(httpService.getPatientAdmissionNote("#{uuid}"))))
		        .exec(httpService.getBedsByPatientUuid("#{patient_uuid}")).exec(httpService.saveWardEncounter("Admission",
		            Collections.emptyList(), ADMISSION_ENCOUNTER_TYPE_UUID, WARD1_CLINIC_LOCATION_UUID));
	}

	public ChainBuilder dischargePatient() {
		return exec(httpService.saveWardEncounter("Discharge", Collections.emptyList(), DISCHARGE_ENCOUNTER_TYPE_UUID,
		    WARD1_CLINIC_LOCATION_UUID)).exec(httpService.submitEndVisit("#{visitUuid}"));
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

	public HttpRequestActionBuilder saveVitalsData(String patientUuid) {
		String encounterDatetime = CommonUtils.getCurrentDateTimeAsString();

		Map<String, Object> encounter = new HashMap<>();
		encounter.put("form", VITALS_FORM_UUID);
		encounter.put("patient", patientUuid);
		encounter.put("location", VITALS_LOCATION_UUID);
		encounter.put("encounterType", VITALS_ENCOUNTER_TYPE_UUID);
		encounter.put("encounterDatetime", encounterDatetime);

		List<Map<String, Object>> observations = new ArrayList<>();
		observations.add(Map.of("concept", SYSTOLIC_BLOOD_PRESSURE, "value", 34));
		observations.add(Map.of("concept", DIASTOLIC_BLOOD_PRESSURE, "value", 44));
		observations.add(Map.of("concept", RESPIRATORY_RATE, "value", 20));
		observations.add(Map.of("concept", ARTERIAL_BLOOD_OXYGEN_SATURATION, "value", 20));
		observations.add(Map.of("concept", PULSE, "value", 120));
		observations.add(Map.of("concept", TEMPERATURE_C, "value", 28));
		observations.add(Map.of("concept", WEIGHT_KG, "value", 60));
		observations.add(Map.of("concept", HEIGHT_CM, "value", 121));
		observations.add(Map.of("concept", MID_UPPER_ARM_CIRCUMFERENCE, "value", 34));

		encounter.put("obs", observations);

		try {
			String body = new ObjectMapper().writeValueAsString(encounter); // Convert Map to JSON
			return http("Save Vitals").post("/openmrs/ws/rest/v1/encounter").body(StringBody(body));
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException("Error converting visitNote to JSON", e);
		}
	}
}
