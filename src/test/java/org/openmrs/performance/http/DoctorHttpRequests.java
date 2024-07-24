package org.openmrs.performance.http;

import com.google.gson.Gson;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.Constants.CARE_SETTING_UUID;
import static org.openmrs.performance.Constants.DRUG_ORDER_TYPE_UUID;

public class DoctorHttpRequests {
	
	public static HttpRequestActionBuilder getVisitTypes() {
		return http("Get Visit Types")
				.get("/openmrs/ws/rest/v1/visittype");
	}
	
	
	public static HttpRequestActionBuilder getVisits(String patientUuid) {
		return http("Get Patient Visits")
				.get("/openmrs/ws/rest/v1/visit?patient="+patientUuid+"&v=custom:(uuid,encounters:(uuid,diagnoses:(uuid,display,rank,diagnosis),form:(uuid,display),encounterDatetime,orders:full,obs:(uuid,concept:(uuid,display,conceptClass:(uuid,display)),display,groupMembers:(uuid,concept:(uuid,display),value:(uuid,display),display),value,obsDatetime),encounterType:(uuid,display,viewPrivilege,editPrivilege),encounterProviders:(uuid,display,encounterRole:(uuid,display),provider:(uuid,person:(uuid,display)))),visitType:(uuid,name,display),startDatetime,stopDatetime,patient,attributes:(attributeType:ref,display,uuid,value)");
	}

	
	public static HttpRequestActionBuilder getProgramEnrollments(String patientUuid) {
		return http("Get Patient Program Enrollments")
				.get("/openmrs/ws/rest/v1/programenrollment?patient="+patientUuid+"&v=custom:(uuid,display,program,dateEnrolled,dateCompleted,location:(uuid,display))");
	}
	
	public static HttpRequestActionBuilder getAppointments(String patientUuid) {
		ZonedDateTime now = ZonedDateTime.now();
		String startDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
		String requestBody = String.format("{\"patientUuid\":\"%s\",\"startDate\":\"%s\"}", patientUuid, startDate);
		
		return http("Get Patient Appointments")
				.post("/openmrs/ws/rest/v1/appointments/search")
				.body(StringBody(requestBody));
	}
	
	public static HttpRequestActionBuilder submitVisitForm(String patientUuid, String visitTypeUuid, String locationUuid) {
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
	
	public static HttpRequestActionBuilder submitEndVisit(String visitUuid, String locationUuid, String visitTypeUuid) {
		Gson gson = new Gson();
		ZonedDateTime now = ZonedDateTime.now();
		String formattedStopDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
		
		Map<String, String> requestBodyMap = new HashMap<>();
		requestBodyMap.put("location", locationUuid);
		requestBodyMap.put("visitType", visitTypeUuid);
		requestBodyMap.put("stopDatetime", formattedStopDateTime);
		
		return http("End Visit")
				.post("/openmrs/ws/rest/v1/visit/"+visitUuid)
				.body(StringBody(gson.toJson(requestBodyMap)));
	}
	
	public static HttpRequestActionBuilder getOrderTypes() {
		return http("Get Order Types")
				.get("/openmrs/ws/rest/v1/ordertype");
	}
	
	public static HttpRequestActionBuilder getAllActiveOrders(String patientUuid) {
		return http("Get Active Orders")
				.get("/openmrs/ws/rest/v1/order?patient="+patientUuid+"&careSetting="+CARE_SETTING_UUID+"&status=ACTIVE");
	}
	
	public static HttpRequestActionBuilder getDrugOrders(String patientUuid) {
		return http("Get Orders")
				.get("/openmrs/ws/rest/v1/order?patient="+patientUuid+"&careSetting="+CARE_SETTING_UUID+"&status=any&orderType="+DRUG_ORDER_TYPE_UUID+"&v=custom:(uuid,dosingType,orderNumber,accessionNumber,patient:ref,action,careSetting:ref,previousOrder:ref,dateActivated,scheduledDate,dateStopped,autoExpireDate,orderType:ref,encounter:ref,orderer:(uuid,display,person:(display)),orderReason,orderReasonNonCoded,orderType,urgency,instructions,commentToFulfiller,drug:(uuid,display,strength,dosageForm:(display,uuid),concept),dose,doseUnits:ref,frequency:ref,asNeeded,asNeededCondition,quantity,quantityUnits:ref,numRefills,dosingInstructions,duration,durationUnits:ref,route:ref,brandName,dispenseAsWritten)");
	}
	
	public static HttpRequestActionBuilder getAllergies(String patientUuid) {
		return http("Get Allergies")
				.get("/openmrs/ws/fhir2/R4/AllergyIntolerance?patient="+patientUuid+"&_summary=data");
	}
	
	public static HttpRequestActionBuilder getConditions(String patientUuid) {
		return http("Get Conditions")
				.get("/openmrs/ws/fhir2/R4/Condition?patient="+patientUuid+"&_count=100&_summary=data");
	}
	
	public static HttpRequestActionBuilder getAttachments(String patientUuid) {
		return http("Get Attachments")
				.get("/openmrs/ws/rest/v1/attachment?patient="+patientUuid+"&includeEncounterless=true");
	}
	
	public static HttpRequestActionBuilder getAllowedFileExtensions() {
		return http("Get Allowed File Extensions")
				.get("/openmrs/ws/rest/v1/systemsetting?&v=custom:(value)&q=attachments.allowedFileExtensions");
	}
	
	public static HttpRequestActionBuilder getLabResults(String patientUuid) {
		return http("Get Lab Results")
				.get("/openmrs/ws/fhir2/R4/Observation?category=laboratory&patient="+patientUuid+"&_count=100&_summary=data")
				.check(bodyString().saveAs("labResultsResponse"));
	}
	
	public static HttpRequestActionBuilder getConcept(String conceptUuid) {
		return http("Get Concept")
				.get("/openmrs/ws/rest/v1/concept/"+conceptUuid+"?v=full");
	}
}
