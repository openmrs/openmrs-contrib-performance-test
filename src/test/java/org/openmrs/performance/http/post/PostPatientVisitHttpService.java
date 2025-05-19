package org.openmrs.performance.http.post;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.gatling.javaapi.http.HttpRequestActionBuilder;
import org.openmrs.performance.utils.CommonUtils;

import java.util.*;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.*;
import static org.openmrs.performance.Constants.*;

public class PostPatientVisitHttpService {

	public static HttpRequestActionBuilder submitVisitForm(String patientUuid, String visitTypeUuid, String locationUuid) {
		String startDateTime = CommonUtils.getCurrentDateTimeAsString();

		Map<String, String> requestBodyMap = new HashMap<>();
		requestBodyMap.put("patient", patientUuid);
		requestBodyMap.put("startDatetime", startDateTime);
		requestBodyMap.put("visitType", visitTypeUuid);
		requestBodyMap.put("location", locationUuid);

		try {
			return http("Submit Visit Form").post("/openmrs/ws/rest/v1/visit")
			        .body(StringBody(new ObjectMapper().writeValueAsString(requestBodyMap)))
			        .check(jsonPath("$.uuid").saveAs("visitUuid"));
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static HttpRequestActionBuilder submitEndVisit(String visitUuid, String locationUuid, String visitTypeUuid) {
		String formattedStopDateTime = CommonUtils.getCurrentDateTimeAsString();

		Map<String, String> requestBodyMap = new HashMap<>();
		requestBodyMap.put("location", locationUuid);
		requestBodyMap.put("visitType", visitTypeUuid);
		requestBodyMap.put("stopDatetime", formattedStopDateTime);

		try {
			return http("End Visit").post("/openmrs/ws/rest/v1/visit/" + visitUuid)
			        .body(StringBody(new ObjectMapper().writeValueAsString(requestBodyMap)));
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static HttpRequestActionBuilder saveAllergy(String patientUuid) {
		Map<String, Object> payload = new HashMap<>();

		Map<String, String> codedAllergen = new HashMap<>();
		codedAllergen.put("uuid", CODED_ALLERGEN_UUID);

		Map<String, Object> allergen = new HashMap<>();
		allergen.put("allergenType", "DRUG");
		allergen.put("codedAllergen", codedAllergen);

		Map<String, String> severity = new HashMap<>();
		severity.put("uuid", SEVERITY_UUID);

		Map<String, String> reactionUuid = new HashMap<>();
		reactionUuid.put("uuid", ALLERGY_REACTION_UUID);

		Map<String, Object> reaction = new HashMap<>();
		reaction.put("reaction", reactionUuid);
		List<Map<String, Object>> reactions = Collections.singletonList(reaction);

		payload.put("allergen", allergen);
		payload.put("severity", severity);
		payload.put("comment", "test");
		payload.put("reactions", reactions);

		try {
			return http("Save an Allergy").post("/openmrs/ws/rest/v1/patient/" + patientUuid + "/allergy")
			        .body(StringBody(new ObjectMapper().writeValueAsString(payload)));
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static HttpRequestActionBuilder uploadAttachment(String patientUuid) {
		return http("Upload Attachment Request").post("/openmrs/ws/rest/v1/attachment")
		        .bodyPart(StringBodyPart("fileCaption", "Test Image")).bodyPart(StringBodyPart("patient", patientUuid))
		        .bodyPart(RawFileBodyPart("file", "Sample_1MB_image.jpg").contentType("image/jpg")
		                .fileName("Sample_1MB_image.jpg"))
		        .asMultipartForm();
	}

	public static HttpRequestActionBuilder saveCondition(String patientUuid, String currentUserUuid) {
		String recordedDate = CommonUtils.getCurrentDateTimeAsString();
		String onSetDate = CommonUtils.getAdjustedDateTimeAsString(-2);
		Map<String, Object> condition = new HashMap<>();
		condition.put("clinicalStatus", Map.of("coding",
		    List.of(Map.of("system", "http://terminology.hl7.org/CodeSystem/condition-clinical", "code", "active"))));
		condition.put("code", Map.of("coding", List.of(Map.of("code", BACK_PAIN, "display", "Back Pain"))));
		condition.put("abatementDateTime", null);
		condition.put("onsetDateTime", onSetDate);
		condition.put("recorder", Map.of("reference", "Practitioner/" + currentUserUuid));
		condition.put("recordedDate", recordedDate);
		condition.put("resourceType", "Condition");
		condition.put("subject", Map.of("reference", "Patient/" + patientUuid));
		try {
			return http("Save Conditions").post("/openmrs/ws/fhir2/R4/Condition?_summary=data")
			        .body(StringBody(new ObjectMapper().writeValueAsString(condition)));
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static HttpRequestActionBuilder saveOrder(String patientUuid, String visitUuid, String currentUserUuid,
	        String drugUuid, String drugConceptUuid) {
		Map<String, Object> order = new HashMap<>();
		order.put("action", "NEW");
		order.put("asNeeded", false);
		order.put("asNeededCondition", null);
		order.put("careSetting", CARE_SETTING_UUID);
		order.put("concept", drugConceptUuid);
		order.put("dose", 1);
		order.put("doseUnits", TABLET);
		order.put("dosingInstructions", "");
		order.put("dosingType", DEFAULT_DOSING_TYPE);
		order.put("drug", drugUuid);
		order.put("duration", null);
		order.put("durationUnits", DAYS);
		order.put("encounter", visitUuid);
		order.put("frequency", ONCE_DAILY);
		order.put("numRefills", 0);
		order.put("orderReasonNonCoded", "reason");
		order.put("orderer", currentUserUuid);
		order.put("patient", patientUuid);
		order.put("quantity", 1);
		order.put("quantityUnits", TABLET);
		order.put("route", ORAL);
		order.put("type", "drugorder");

		Map<String, Object> encounter = new HashMap<>();

		encounter.put("encounterDatetime", CommonUtils.getCurrentDateTimeAsString());
		encounter.put("encounterType", ORDER);
		encounter.put("location", OUTPATIENT_CLINIC_LOCATION_UUID);
		encounter.put("patient", patientUuid);
		encounter.put("visit", visitUuid);
		encounter.put("obs", new Object[0]);
		encounter.put("orders", new Object[] { order });

		try {
			return http("Save Drug Order").post("/openmrs/ws/rest/v1/encounter")
			        .body(StringBody(new ObjectMapper().writeValueAsString(encounter)));
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static HttpRequestActionBuilder saveVisitNote(String patientUuid, String currentUser, String value) {
		String encounterDatetime = CommonUtils.getCurrentDateTimeAsString();

		Map<String, Object> visitNote = new HashMap<>();
		visitNote.put("form", VISIT_NOTE_FORM_UUID);
		visitNote.put("patient", patientUuid);
		visitNote.put("location", OUTPATIENT_CLINIC_LOCATION_UUID);
		visitNote.put("encounterType", VISIT_NOTE_ENCOUNTER_TYPE_UUID);
		visitNote.put("encounterDatetime", encounterDatetime);

		Map<String, Object> encounterProvider = new HashMap<>();
		encounterProvider.put("encounterRole", CLINICIAN_ENCOUNTER_ROLE);
		encounterProvider.put("provider", currentUser);

		Map<String, Object> obs = new HashMap<>();
		obs.put("concept", Map.of("uuid", VISIT_NOTE_CONCEPT_UUID));
		obs.put("value", value);

		visitNote.put("encounterProviders", List.of(encounterProvider));
		visitNote.put("obs", List.of(obs));

		try {
			String body = new ObjectMapper().writeValueAsString(visitNote); // Convert Map to JSON

			return http("Save Visit Note").post("/openmrs/ws/rest/v1/encounter").body(StringBody(body))
			        .check(jsonPath("$.uuid").saveAs("encounterUuid")); // Store encounter UUID
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException("Error converting visitNote to JSON", e);
		}
	}

	public static HttpRequestActionBuilder saveDiagnosis(String patientUuid, String encounterUuid, String diagnosisUuid,
	        String certainty, int rank) {
		Map<String, Object> patientDiagnosis = new HashMap<>();
		patientDiagnosis.put("patient", patientUuid);
		patientDiagnosis.put("encounter", encounterUuid);
		patientDiagnosis.put("certainty", certainty);
		patientDiagnosis.put("rank", rank);
		patientDiagnosis.put("condition", null);

		Map<String, Object> diagnosis = new HashMap<>();
		diagnosis.put("coded", diagnosisUuid);
		patientDiagnosis.put("diagnosis", diagnosis);

		try {
			String body = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.ALWAYS)
			        .writeValueAsString(patientDiagnosis);

			return http("Save Patient Diagnosis").post("/openmrs/ws/rest/v1/patientdiagnoses").body(StringBody(body));
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException("Error converting patientDiagnosis to JSON", e);
		}
	}

	public static HttpRequestActionBuilder saveVitalsData(String patientUuid) {
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
		observations.add(Map.of("concept", RESPIRATORY_RATE, "value", 100));
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
