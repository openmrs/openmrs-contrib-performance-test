package org.openmrs.performance.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.Constants.ADMIN_SUPER_USER_UUID;
import static org.openmrs.performance.Constants.ASPRIN_CONCEPT_UUID;
import static org.openmrs.performance.Constants.ASPRIN_DRUG_UUID;
import static org.openmrs.performance.Constants.CARE_SETTING_UUID;
import static org.openmrs.performance.Constants.DAYS;
import static org.openmrs.performance.Constants.DEFAULT_DOSING_TYPE;
import static org.openmrs.performance.Constants.INPATEINT_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.Constants.ONCE_DAILY;
import static org.openmrs.performance.Constants.ORAL;
import static org.openmrs.performance.Constants.ORDER;
import static org.openmrs.performance.Constants.ORDER_DISCONTINUED_CODE;
import static org.openmrs.performance.Constants.OUTPATIENT_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.Constants.TABLET;
import static org.openmrs.performance.utils.CommonUtils.getAdjustedDateTimeAsString;
import static org.openmrs.performance.utils.CommonUtils.getCurrentDateTimeAsString;

public class PharmacistHttpService extends HttpService {

	public HttpRequestActionBuilder getOrderTypes() {
		return http("Get Order Types").get("/openmrs/ws/rest/v1/ordertype");
	}

	public HttpRequestActionBuilder getAllActiveOrders(String patientUuid) {
		return http("Get Active Orders").get(
		    "/openmrs/ws/rest/v1/order?patient=" + patientUuid + "&careSetting=" + CARE_SETTING_UUID + "&status=ACTIVE");
	}

	public HttpRequestActionBuilder searchForDrug(String searchQuery) {
		String customRepresentation = "custom:(uuid,display,name,strength,dosageForm:(display,uuid),concept:(display,uuid))";
		return http("Search for Drug").get("/openmrs/ws/rest/v1/drug?name=" + searchQuery + "&v=" + customRepresentation);
	}

	public HttpRequestActionBuilder saveOrder() {

		return http("Save Drug Order").post("/openmrs/ws/rest/v1/encounter").body(StringBody(session -> {
			Map<String, Object> order = new HashMap<>();
			order.put("action", "NEW");
			order.put("asNeeded", false);
			order.put("asNeededCondition", null);
			order.put("careSetting", CARE_SETTING_UUID);
			order.put("concept", ASPRIN_CONCEPT_UUID);
			order.put("dose", 1);
			order.put("doseUnits", TABLET);
			order.put("dosingInstructions", "");
			order.put("dosingType", DEFAULT_DOSING_TYPE);
			order.put("drug", ASPRIN_DRUG_UUID);
			order.put("duration", null);
			order.put("durationUnits", DAYS);
			order.put("encounter", null);
			order.put("frequency", ONCE_DAILY);
			order.put("numRefills", 0);
			order.put("orderReasonNonCoded", "reason");
			order.put("orderer", session.getString("currentUserUuid"));
			order.put("patient", session.getString("patient_uuid"));
			order.put("quantity", 1);
			order.put("quantityUnits", TABLET);
			order.put("route", ORAL);
			order.put("type", "drugorder");

			Map<String, Object> encounter = new HashMap<>();

			encounter.put("encounterType", ORDER);
			encounter.put("location", OUTPATIENT_CLINIC_LOCATION_UUID);
			encounter.put("patient", session.getString("patient_uuid"));
			encounter.put("visit", session.getString("visitUuid"));
			encounter.put("obs", new Object[0]);
			encounter.put("orders", new Object[] { order });
			encounter.put("encounterDatetime", getCurrentDateTimeAsString());
			try {
				return new ObjectMapper().writeValueAsString(encounter);
			}
			catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		})).check(jsonPath("$.uuid").saveAs("orderUuid"), jsonPath("$.orders[0].uuid").saveAs("medicationRequestUuid"));
	}

	public HttpRequestActionBuilder discontinueDrugOrder() {
		return http("Discontinue the drug order").post("/openmrs/ws/rest/v1/order").body(StringBody(session -> {
			Map<String, Object> order = new HashMap<>();

			order.put("action", "DISCONTINUE");
			order.put("type", "drugorder");
			order.put("previousOrder", null);
			order.put("orderer", session.getString("currentUserUuid"));
			order.put("patient", session.getString("patient_uuid"));
			order.put("careSetting", CARE_SETTING_UUID);
			order.put("drug", ASPRIN_DRUG_UUID);
			order.put("concept", ASPRIN_CONCEPT_UUID);
			order.put("orderReasonNonCoded", "reason");
			order.put("encounter", session.getString("orderUuid"));

			try {
				return new ObjectMapper().writeValueAsString(order);
			}
			catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}));
	}

	public HttpRequestActionBuilder getMedicationRequestEncounters() {
		String startDate = getAdjustedDateTimeAsString(-90);
		String encoded = URLEncoder.encode(startDate, StandardCharsets.UTF_8);

		return http("Get medication request encounters")
		        .get("/openmrs/ws/fhir2/R4/Encounter?_query=encountersWithMedicationRequests&date=ge" + encoded
		                + "&_getpagesoffset=0&_count=10&status=active&_summary=data")
		        .check(jsonPath("$.entry[*].resource.subject.reference").findAll().saveAs("medicalPatientEncounterUuids"));
	}

	public HttpRequestActionBuilder getPatientAge(String patientUuid) {
		return http("Get patient age").get("/openmrs/ws/rest/v1/person/" + patientUuid + "?v=custom:(age)");
	}

	public HttpRequestActionBuilder getPatientAllergyIntolerance(String patientUuid) {
		return http("Get Patient Allergy Intolerance")
		        .get("/openmrs/ws/fhir2/R4/AllergyIntolerance?patient=" + patientUuid + "&_summary=data");
	}

	public HttpRequestActionBuilder getSpecificMedicationEncounter(String encounterUuid) {
		return http("Get Specific Medication Requests").get("/openmrs/ws/fhir2/R4/MedicationRequest?encounter="
		        + encounterUuid + "&_revinclude=MedicationDispense:prescription" + "&_include=MedicationRequest:encounter"
		        + "&_summary=data");
	}

	public HttpRequestActionBuilder getEncounterWithVisitAndDiagnoses(String encounterUuid) {
		String customRepresentation = "custom:(uuid,display,visit:(uuid,encounters:(uuid,diagnoses:"
		        + "(uuid,display,certainty,diagnosis:(coded:(uuid,display)))))";

		return http("Get Encounter With Visit and Diagnoses")
		        .get("/openmrs/ws/rest/v1/encounter/" + encounterUuid + "?v=" + customRepresentation);
	}

	public HttpRequestActionBuilder getOrderEntryConfig() {
		return http("Get Order Entry Config").get("/openmrs/ws/rest/v1/orderentryconfig");
	}

	public HttpRequestActionBuilder getValueSetByUuid(String valueSetUuid) {
		return http("Get ValueSet by UUID").get("/openmrs/ws/fhir2/R4/ValueSet/" + valueSetUuid + "?_summary=data");
	}

	public HttpRequestActionBuilder getMedicationRequestByUuid(String medicationRequestUuid) {
		return http("Get Medication Request by UUID")
		        .get("/openmrs/ws/fhir2/R4/MedicationRequest/" + medicationRequestUuid + "?_summary=data")
		        .check(jsonPath("$.subject").saveAs("medicationSubject"),
		            jsonPath("$.medicationReference").saveAs("medicationReference"),
		            jsonPath("$.dosageInstruction").saveAs("dosageInstruction"));
	}

	public HttpRequestActionBuilder getMedicationByUuid(String medicationUuid) {
		return http("Get Medication by UUID").get("/openmrs/ws/fhir2/R4/Medication/" + medicationUuid + "?_summary=data");
	}

	public HttpRequestActionBuilder searchMedicationByCode(String code) {
		return http("Search Medication by Code").get("/openmrs/ws/fhir2/R4/Medication?code=" + code + "&_summary=data");
	}

	public HttpRequestActionBuilder closeMedication() {
		return http("Close medication").post("/openmrs/ws/fhir2/R4/MedicationDispense?_summary=data")
		        .body(StringBody(session -> {
			        ObjectMapper mapper = new ObjectMapper();
			        Map<String, Object> payload = new HashMap<>();

			        payload.put("resourceType", "MedicationDispense");
			        payload.put("status", "declined");

			        List<Map<String, Object>> authorizingPrescription = new ArrayList<>();
			        Map<String, Object> prescription = new HashMap<>();
			        prescription.put("reference", "MedicationRequest/" + session.getString("medicationRequestUuid"));
			        prescription.put("type", "MedicationRequest");
			        authorizingPrescription.add(prescription);
			        payload.put("authorizingPrescription", authorizingPrescription);

			        try {
				        String medRefJson = session.getString("medicationReference");
				        Map<String, Object> medicationReference = mapper.readValue(medRefJson, new TypeReference<>() {});
				        payload.put("medicationReference", medicationReference);
			        }
			        catch (Exception e) {
				        throw new RuntimeException("Failed to parse medicationReference JSON from session", e);
			        }

			        try {
				        String subjectJson = session.getString("medicationSubject");
				        Map<String, Object> subject = mapper.readValue(subjectJson, new TypeReference<>() {});
				        payload.put("subject", subject);
			        }
			        catch (Exception e) {
				        throw new RuntimeException("Failed to parse medicationSubject JSON from session", e);
			        }

			        List<Map<String, Object>> performerList = new ArrayList<>();
			        Map<String, Object> performer = new HashMap<>();
			        Map<String, Object> actor = new HashMap<>();
			        actor.put("reference", "Practitioner/" + ADMIN_SUPER_USER_UUID);
			        performer.put("actor", actor);
			        performerList.add(performer);
			        payload.put("performer", performerList);

			        Map<String, Object> location = new HashMap<>();
			        location.put("reference", "Location/" + INPATEINT_CLINIC_LOCATION_UUID);
			        payload.put("location", location);

			        payload.put("whenHandedOver", getCurrentDateTimeAsString());

			        Map<String, Object> statusReasonCodeableConcept = new HashMap<>();
			        List<Map<String, Object>> statusCodingList = new ArrayList<>();
			        Map<String, Object> statusCoding = new HashMap<>();
			        statusCoding.put("code", ORDER_DISCONTINUED_CODE);
			        statusCodingList.add(statusCoding);
			        statusReasonCodeableConcept.put("coding", statusCodingList);
			        payload.put("statusReasonCodeableConcept", statusReasonCodeableConcept);

			        payload.put("whenPrepared", getCurrentDateTimeAsString());

			        try {
				        return mapper.writeValueAsString(payload);
			        }
			        catch (JsonProcessingException e) {
				        throw new RuntimeException(e);
			        }
		        }));
	}

	public HttpRequestActionBuilder dispenseMedicine() {
		return http("Dispense medication form submission").post("/openmrs/ws/fhir2/R4/MedicationDispense?_summary=data")
		        .body(StringBody(session -> {
			        ObjectMapper mapper = new ObjectMapper();
			        Map<String, Object> payload = new HashMap<>();

			        payload.put("resourceType", "MedicationDispense");
			        payload.put("status", "completed");

			        List<Map<String, Object>> authorizingPrescription = new ArrayList<>();
			        Map<String, Object> prescription = new HashMap<>();
			        prescription.put("reference", "MedicationRequest/" + session.getString("medicationRequestUuid"));
			        prescription.put("type", "MedicationRequest");
			        authorizingPrescription.add(prescription);
			        payload.put("authorizingPrescription", authorizingPrescription);

			        try {
				        String medRefJson = session.getString("medicationReference");
				        Map<String, Object> medicationReference = mapper.readValue(medRefJson, new TypeReference<>() {});
				        payload.put("medicationReference", medicationReference);
			        }
			        catch (Exception e) {
				        throw new RuntimeException("Failed to parse medicationReference JSON from session", e);
			        }

			        try {
				        String subjectJson = session.getString("medicationSubject");
				        Map<String, Object> subject = mapper.readValue(subjectJson, new TypeReference<>() {});
				        payload.put("subject", subject);
			        }
			        catch (Exception e) {
				        throw new RuntimeException("Failed to parse subject JSON from session", e);
			        }

			        try {
				        String dosageJson = session.getString("dosageInstruction");
				        List<Map<String, Object>> dosageInstruction = mapper.readValue(dosageJson, new TypeReference<>() {});
				        payload.put("dosageInstruction", dosageInstruction);
			        }
			        catch (Exception e) {
				        throw new RuntimeException("Failed to parse dosageInstruction JSON from session", e);
			        }

			        List<Map<String, Object>> performerList = new ArrayList<>();
			        Map<String, Object> performer = new HashMap<>();
			        Map<String, Object> actor = new HashMap<>();
			        actor.put("reference", "Practitioner/" + ADMIN_SUPER_USER_UUID);
			        performer.put("actor", actor);
			        performerList.add(performer);
			        payload.put("performer", performerList);

			        Map<String, Object> location = new HashMap<>();
			        location.put("reference", "Location/" + INPATEINT_CLINIC_LOCATION_UUID);
			        payload.put("location", location);

			        payload.put("whenHandedOver", getCurrentDateTimeAsString());

			        Map<String, Object> quantity = new HashMap<>();
			        quantity.put("value", 10);
			        quantity.put("code", TABLET);
			        quantity.put("unit", "Tablet");
			        payload.put("quantity", quantity);

			        Map<String, Object> substitution = new HashMap<>();
			        substitution.put("wasSubstituted", false);

			        List<Map<String, Object>> reasonList = new ArrayList<>();
			        Map<String, Object> reasonItem = new HashMap<>();
			        List<Map<String, Object>> reasonCoding = new ArrayList<>();
			        Map<String, Object> reasonCodingItem = new HashMap<>();
			        reasonCodingItem.put("code", null);
			        reasonCoding.add(reasonCodingItem);
			        reasonItem.put("coding", reasonCoding);
			        reasonList.add(reasonItem);
			        substitution.put("reason", reasonList);

			        Map<String, Object> type = new HashMap<>();
			        List<Map<String, Object>> typeCoding = new ArrayList<>();
			        Map<String, Object> typeCodingItem = new HashMap<>();
			        typeCodingItem.put("code", null);
			        typeCoding.add(typeCodingItem);
			        type.put("coding", typeCoding);
			        substitution.put("type", type);

			        payload.put("substitution", substitution);

			        payload.put("whenPrepared", getCurrentDateTimeAsString());
			        try {
				        return mapper.writeValueAsString(payload);
			        }
			        catch (JsonProcessingException e) {
				        throw new RuntimeException(e);
			        }
		        }));
	}
}
