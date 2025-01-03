package org.openmrs.performance.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.bodyString;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.Constants.CARE_SETTING_UUID;
import static org.openmrs.performance.Constants.CLINICIAN_ENCOUNTER_ROLE;
import static org.openmrs.performance.Constants.DAYS;
import static org.openmrs.performance.Constants.DEFAULT_DOSING_TYPE;
import static org.openmrs.performance.Constants.DRUG_ORDER;
import static org.openmrs.performance.Constants.ONCE_DAILY;
import static org.openmrs.performance.Constants.ORAL;
import static org.openmrs.performance.Constants.ORDER;
import static org.openmrs.performance.Constants.OUTPATIENT_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.Constants.TABLET;
import static org.openmrs.performance.Constants.VISIT_NOTE_CONCEPT_UUID;
import static org.openmrs.performance.Constants.VISIT_NOTE_ENCOUNTER_TYPE_UUID;
import static org.openmrs.performance.Constants.VISIT_NOTE_FORM_UUID;

public class DoctorHttpService extends HttpService {
	
	public HttpRequestActionBuilder getVisitTypes() {
		return http("Get Visit Types")
				.get("/openmrs/ws/rest/v1/visittype");
	}
	
	public HttpRequestActionBuilder getVisitsOfPatient(String patientUuid) {
		String customRepresentation = "custom:(uuid,encounters:(uuid,diagnoses:(uuid,display,rank,diagnosis),"
				+ "form:(uuid,display),encounterDatetime,orders:full,"
				+ "obs:(uuid,concept:(uuid,display,conceptClass:(uuid,display)),display,"
				+ "groupMembers:(uuid,concept:(uuid,display),value:(uuid,display),display),"
				+ "value,obsDatetime),encounterType:(uuid,display,viewPrivilege,editPrivilege),"
				+ "encounterProviders:(uuid,display,encounterRole:(uuid,display),"
				+ "provider:(uuid,person:(uuid,display)))),visitType:(uuid,name,display),"
				+ "startDatetime,stopDatetime,patient,"
				+ "attributes:(attributeType:ref,display,uuid,value))";
		
		return http("Get Visits of Patient")
				.get("/openmrs/ws/rest/v1/visit?patient=" + patientUuid + "&v=" + customRepresentation);
	}
	
	public HttpRequestActionBuilder getActiveVisitOfPatient(String patientUuid) {
		String customRepresentation = "custom:(uuid,display,voided,indication,startDatetime,stopDatetime,"
				+ "encounters:(uuid,display,encounterDatetime,"
				+ "form:(uuid,name),location:ref,"
				+ "encounterType:ref,"
				+ "encounterProviders:(uuid,display,"
				+ "provider:(uuid,display))),"
				+ "patient:(uuid,display),"
				+ "visitType:(uuid,name,display),"
				+ "attributes:(uuid,display,attributeType:(name,datatypeClassname,uuid),value),"
				+ "location:(uuid,name,display))";
		
		return http("Get Active Visits of Patient")
				.get("/openmrs/ws/rest/v1/visit?patient=" + patientUuid + "&v=" + customRepresentation + "&includeInactive=false");
	}
	
	public HttpRequestActionBuilder getProgramEnrollments(String patientUuid) {
		String customRepresentation = "custom:(uuid,display,program,dateEnrolled,dateCompleted," +
									  "location:(uuid,display))";
		
		return http("Get Program Enrollments of Patient")
				.get("/openmrs/ws/rest/v1/programenrollment?patient=" + patientUuid + "&v=" + customRepresentation);
	}
	
	public HttpRequestActionBuilder getAppointments(String patientUuid) {
		ZonedDateTime now = ZonedDateTime.now();
		String startDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
		String requestBody = String.format("{\"patientUuid\":\"%s\",\"startDate\":\"%s\"}", patientUuid, startDate);
		
		return http("Get Appointments of Patient")
				.post("/openmrs/ws/rest/v1/appointments/search")
				.body(StringBody(requestBody));
	}
	
	public HttpRequestActionBuilder submitVisitForm(String patientUuid, String visitTypeUuid, String locationUuid) {
		Gson gson = new Gson();
		ZonedDateTime now = ZonedDateTime.now();
		String startDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
		
		Map<String, String> requestBodyMap = new HashMap<>();
		requestBodyMap.put("patient", patientUuid);
		requestBodyMap.put("startDatetime", startDateTime);
		requestBodyMap.put("visitType", visitTypeUuid);
		requestBodyMap.put("location", locationUuid);
		
		return http("Submit Visit Form")
				.post("/openmrs/ws/rest/v1/visit")
				.body(StringBody(gson.toJson(requestBodyMap)))
				.check(jsonPath("$.uuid").saveAs("visitUuid"));
	}
	
	public HttpRequestActionBuilder submitEndVisit(String visitUuid, String locationUuid, String visitTypeUuid) {
		Gson gson = new Gson();
		ZonedDateTime now = ZonedDateTime.now();
		String formattedStopDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
		
		Map<String, String> requestBodyMap = new HashMap<>();
		requestBodyMap.put("location", locationUuid);
		requestBodyMap.put("visitType", visitTypeUuid);
		requestBodyMap.put("stopDatetime", formattedStopDateTime);
		
		return http("End Visit")
				.post("/openmrs/ws/rest/v1/visit/" + visitUuid)
				.body(StringBody(gson.toJson(requestBodyMap)));
	}
	
	public HttpRequestActionBuilder getOrderTypes() {
		return http("Get Order Types")
				.get("/openmrs/ws/rest/v1/ordertype");
	}
	
	public HttpRequestActionBuilder getAllActiveOrders(String patientUuid) {
		return http("Get Active Orders")
				.get("/openmrs/ws/rest/v1/order?patient=" + patientUuid + "&careSetting=" + CARE_SETTING_UUID + "&status=ACTIVE");
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
		return http("Get Orders")
				.get("/openmrs/ws/rest/v1/order" +
						"?patient=" + patientUuid +
						"&careSetting=" + CARE_SETTING_UUID +
						"&status=any&orderType=" + DRUG_ORDER +
						"&v=" + customRepresentation);
	}
	
	public HttpRequestActionBuilder getAllergies(String patientUuid) {
		return http("Get Allergies of Patient")
				.get("/openmrs/ws/fhir2/R4/AllergyIntolerance?patient=" + patientUuid + "&_summary=data");
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
	
	public HttpRequestActionBuilder getLabResults(String patientUuid) {
		return http("Get Lab Results of Patient")
				.get("/openmrs/ws/fhir2/R4/Observation?category=laboratory&patient=" + patientUuid + "&_count=100&_summary=data")
				.check(bodyString().saveAs("labResultsResponse"));
	}
	
	public HttpRequestActionBuilder getConcept(String conceptUuid) {
		return http("Get Concept")
				.get("/openmrs/ws/rest/v1/concept/" + conceptUuid + "?v=full");
	}
	
	public HttpRequestActionBuilder getImmunizations(String patientUuid) {
		return http("Get Immunizations of Patient")
				.get("/openmrs/ws/fhir2/R4/Immunization?patient=" + patientUuid + "&_summary=data");
	}
	
	public HttpRequestActionBuilder searchForDrug(String searchQuery) {
		String customRepresentation = """
				custom:(uuid,display,name,strength,
					dosageForm:(display,uuid),
					concept:(display,uuid))
				""";
		return http("Search for Drug")
				.get("/openmrs/ws/rest/v1/drug?name=" + searchQuery + "&v=" + customRepresentation);
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
		
		Gson gson = new Gson();
		String body = gson.toJson(encounter);
		
		return http("Save Drug Order")
				.post("/openmrs/ws/rest/v1/encounter")
				.body(StringBody(body));
	}

	public HttpRequestActionBuilder saveVisitNote(String patientUuid, String currentUser, String value) {
		Map<String, Object> visitNote = new HashMap<>();
		visitNote.put("form", VISIT_NOTE_FORM_UUID);
		visitNote.put("patient", patientUuid);
		visitNote.put("location", OUTPATIENT_CLINIC_LOCATION_UUID);
		visitNote.put("encounterType", VISIT_NOTE_ENCOUNTER_TYPE_UUID);

		Map<String, Object> encounterProvider = new HashMap<>();
		encounterProvider.put("encounterRole", CLINICIAN_ENCOUNTER_ROLE);
		encounterProvider.put("provider", currentUser);

		visitNote.put("encounterProviders", List.of(encounterProvider));

		Map<String, Object> concept = new HashMap<>();
		concept.put("uuid", VISIT_NOTE_CONCEPT_UUID);


		Map<String, Object> obs = new HashMap<>();
		obs.put("concept", concept);
		obs.put("value", value);
		visitNote.put("obs", List.of(obs));

		Gson gson = new Gson();
		String body = gson.toJson(visitNote);

		exec(session -> {
			System.out.println(body);
			return session;
		});

		return http("Save Visit Note").post("/openmrs/ws/rest/v1/encounter").body(StringBody(body))
				.check(jsonPath("$.uuid").saveAs("encounter_uuid"));
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

		Gson gson = new GsonBuilder().serializeNulls().create();
		String body = gson.toJson(patientDiagnosis);

		exec(seassion -> {
			System.out.println(body);
			return seassion;
		});

		return http("Save Patient Diagnosis")
				.post("/openmrs/ws/rest/v1/patientdiagnoses")
				.body(StringBody(body));
	}

}
