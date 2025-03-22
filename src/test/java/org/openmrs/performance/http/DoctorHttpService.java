package org.openmrs.performance.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.bodyString;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.RawFileBodyPart;
import static io.gatling.javaapi.http.HttpDsl.StringBodyPart;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.Constants.ALLERGY_REACTION_UUID;
import static org.openmrs.performance.Constants.ARTERIAL_BLOOD_OXYGEN_SATURATION;
import static org.openmrs.performance.Constants.CARE_SETTING_UUID;
import static org.openmrs.performance.Constants.CLINICIAN_ENCOUNTER_ROLE;
import static org.openmrs.performance.Constants.CODED_ALLERGEN_UUID;
import static org.openmrs.performance.Constants.DAYS;
import static org.openmrs.performance.Constants.DEFAULT_DOSING_TYPE;
import static org.openmrs.performance.Constants.DIASTOLIC_BLOOD_PRESSURE;
import static org.openmrs.performance.Constants.DRUG_ORDER;
import static org.openmrs.performance.Constants.HEIGHT_CM;
import static org.openmrs.performance.Constants.MID_UPPER_ARM_CIRCUMFERENCE;
import static org.openmrs.performance.Constants.ONCE_DAILY;
import static org.openmrs.performance.Constants.ORAL;
import static org.openmrs.performance.Constants.ORDER;
import static org.openmrs.performance.Constants.OUTPATIENT_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.Constants.PULSE;
import static org.openmrs.performance.Constants.RESPIRATORY_RATE;
import static org.openmrs.performance.Constants.SEVERITY_UUID;
import static org.openmrs.performance.Constants.SYSTOLIC_BLOOD_PRESSURE;
import static org.openmrs.performance.Constants.TABLET;
import static org.openmrs.performance.Constants.TEMPERATURE_C;
import static org.openmrs.performance.Constants.VISIT_NOTE_CONCEPT_UUID;
import static org.openmrs.performance.Constants.VISIT_NOTE_ENCOUNTER_TYPE_UUID;
import static org.openmrs.performance.Constants.VISIT_NOTE_FORM_UUID;
import static org.openmrs.performance.Constants.VITALS_ENCOUNTER_TYPE_UUID;
import static org.openmrs.performance.Constants.VITALS_FORM_UUID;
import static org.openmrs.performance.Constants.VITALS_LOCATION_UUID;
import static org.openmrs.performance.Constants.WEIGHT_KG;

public class DoctorHttpService extends HttpService {

	public HttpRequestActionBuilder getVisitTypes() {
		return http("Get Visit Types").get("/openmrs/ws/rest/v1/visittype");
	}

	public HttpRequestActionBuilder getVisitsOfPatient(String patientUuid) {
		String customRepresentation = "custom:(uuid,display,voided,indication,startDatetime,stopDatetime,"
		        + "encounters:(uuid,display,encounterDatetime,form:(uuid,name),location:ref,encounterType:ref,encounterProviders:(uuid,display,provider:(uuid,display))),"
		        + "patient:(uuid,display)," + "visitType:(uuid,name,display),"
		        + "attributes:(uuid,display,attributeType:(name,datatypeClassname,uuid),value),"
		        + "location:(uuid,name,display))";

		return http("Get Visits of Patient")
		        .get("/openmrs/ws/rest/v1/visit?patient=" + patientUuid + "&v=" + customRepresentation);
	}

	public HttpRequestActionBuilder getActiveVisitOfPatient(String patientUuid) {
		String customRepresentation = "custom:(uuid,display,voided,indication,startDatetime,stopDatetime,"
		        + "encounters:(uuid,display,encounterDatetime," + "form:(uuid,name),location:ref," + "encounterType:ref,"
		        + "encounterProviders:(uuid,display," + "provider:(uuid,display)))," + "patient:(uuid,display),"
		        + "visitType:(uuid,name,display),"
		        + "attributes:(uuid,display,attributeType:(name,datatypeClassname,uuid),value),"
		        + "location:(uuid,name,display))";

		return http("Get Active Visits of Patient").get(
		    "/openmrs/ws/rest/v1/visit?patient=" + patientUuid + "&v=" + customRepresentation + "&includeInactive=false");
	}

	public HttpRequestActionBuilder getProgramEnrollments(String patientUuid) {
		String customRepresentation = "custom:(uuid,display,program,dateEnrolled,dateCompleted,"
		        + "location:(uuid,display))";

		return http("Get Program Enrollments of Patient")
		        .get("/openmrs/ws/rest/v1/programenrollment?patient=" + patientUuid + "&v=" + customRepresentation);
	}

	public HttpRequestActionBuilder getAppointments(String patientUuid) {
		ZonedDateTime now = ZonedDateTime.now();
		String startDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
		String requestBody = String.format("{\"patientUuid\":\"%s\",\"startDate\":\"%s\"}", patientUuid, startDate);

		return http("Get Appointments of Patient").post("/openmrs/ws/rest/v1/appointments/search")
		        .body(StringBody(requestBody));
	}

	public HttpRequestActionBuilder submitVisitForm(String patientUuid, String visitTypeUuid, String locationUuid) {
		ZonedDateTime now = ZonedDateTime.now();
		String startDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

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

	public HttpRequestActionBuilder submitEndVisit(String visitUuid, String locationUuid, String visitTypeUuid) {
		ZonedDateTime now = ZonedDateTime.now();
		String formattedStopDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

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

	public HttpRequestActionBuilder getOrderTypes() {
		return http("Get Order Types").get("/openmrs/ws/rest/v1/ordertype");
	}

	public HttpRequestActionBuilder getAllActiveOrders(String patientUuid) {
		return http("Get Active Orders").get(
		    "/openmrs/ws/rest/v1/order?patient=" + patientUuid + "&careSetting=" + CARE_SETTING_UUID + "&status=ACTIVE");
	}

	public HttpRequestActionBuilder getDrugOrders(String patientUuid) {
		String customRepresentation = """
		        custom:(uuid,dosingType,orderNumber,accessionNumber,
		        	patient:ref,action,careSetting:ref,previousOrder:ref,dateActivated,scheduledDate,dateStopped,autoExpireDate,
		        	orderType:ref,encounter:ref,
		        	orderer:(uuid,display,person:(display)),
		        	orderReason,orderReasonNonCoded,orderType,urgency,instructions,
		        	commentToFulfiller,
		        	drug:(uuid,display,strength,
		        		dosageForm:(display,uuid),concept),
		        		dose,doseUnits:ref,
		        	frequency:ref,asNeeded,asNeededCondition,quantity,quantityUnits:ref,numRefills,dosingInstructions,
		        	duration,durationUnits:ref,route:ref,brandName,dispenseAsWritten)
		        """;
		return http("Get Orders").get("/openmrs/ws/rest/v1/order" + "?patient=" + patientUuid + "&careSetting="
		        + CARE_SETTING_UUID + "&status=any&orderType=" + DRUG_ORDER + "&v=" + customRepresentation);
	}

	public HttpRequestActionBuilder getAllergies(String patientUuid) {
		return http("Get Allergies of Patient")
		        .get("/openmrs/ws/fhir2/R4/AllergyIntolerance?patient=" + patientUuid + "&_summary=data");
	}

	public HttpRequestActionBuilder getAllergens(String allergenType, String allergenUuid) {
		return http("Get " + allergenType + " Allergens").get("/openmrs/ws/rest/v1/concept/" + allergenUuid + "?v=full");
	}

	public HttpRequestActionBuilder saveAllergy(String patientUuid) {
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

	public HttpRequestActionBuilder getConditions(String patientUuid) {
		return http("Get Conditions of Patient")
		        .get("/openmrs/ws/fhir2/R4/Condition?patient=" + patientUuid + "&_count=100&_summary=data");
	}

	public HttpRequestActionBuilder getAttachments(String patientUuid) {
		return http("Get Attachments of Patient")
		        .get("/openmrs/ws/rest/v1/attachment?patient=" + patientUuid + "&includeEncounterless=true");
	}

	public HttpRequestActionBuilder getAllowedFileExtensions() {
		return http("Get Allowed File Extensions")
		        .get("/openmrs/ws/rest/v1/systemsetting?&v=custom:(value)&q=attachments.allowedFileExtensions");
	}

	public HttpRequestActionBuilder uploadAttachment(String patientUuid) {
		return http("Upload Attachment Request").post("/openmrs/ws/rest/v1/attachment")
		        .bodyPart(StringBodyPart("fileCaption", "Test Image")).bodyPart(StringBodyPart("patient", patientUuid))
		        .bodyPart(RawFileBodyPart("file", "Sample_1MB_image.jpg").contentType("image/jpg")
		                .fileName("Sample_1MB_image.jpg"))
		        .asMultipartForm();
	}

	public HttpRequestActionBuilder getLabResults(String patientUuid) {
		return http("Get Lab Results of Patient").get(
		    "/openmrs/ws/fhir2/R4/Observation?category=laboratory&patient=" + patientUuid + "&_count=100&_summary=data")
		        .check(bodyString().saveAs("labResultsResponse"));
	}

	public HttpRequestActionBuilder getConcept(String conceptUuid) {
		return http("Get Concept").get("/openmrs/ws/rest/v1/concept/" + conceptUuid + "?v=full");
	}

	public HttpRequestActionBuilder getImmunizations(String patientUuid) {
		return http("Get Immunizations of Patient")
		        .get("/openmrs/ws/fhir2/R4/Immunization?patient=" + patientUuid + "&_summary=data");
	}

	public HttpRequestActionBuilder getPrograms() {
		return http("Get Programs")
		        .get("/openmrs/ws/rest/v1/program?v=custom:(uuid,display,allWorkflows,concept:(uuid,display))");
	}

	public HttpRequestActionBuilder searchForDrug(String searchQuery) {
		String customRepresentation = """
		        custom:(uuid,display,name,strength,
		        	dosageForm:(display,uuid),
		        	concept:(display,uuid))
		        """;
		return http("Search for Drug").get("/openmrs/ws/rest/v1/drug?name=" + searchQuery + "&v=" + customRepresentation);
	}

	public HttpRequestActionBuilder saveOrder(String patientUuid, String visitUuid, String currentUserUuid, String drugUuid,
	        String drugConceptUuid) {
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

		encounter.put("encounterDatetime",
		    ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")));
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

	public HttpRequestActionBuilder saveVisitNote(String patientUuid, String currentUser, String value) {
		ZonedDateTime now = ZonedDateTime.now();
		String encounterDatetime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

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

	public HttpRequestActionBuilder saveDiagnosis(String patientUuid, String encounterUuid, String diagnosisUuid,
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

	public HttpRequestActionBuilder saveVitalsData(String patientUuid) {
		ZonedDateTime now = ZonedDateTime.now();
		String encounterDatetime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

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
