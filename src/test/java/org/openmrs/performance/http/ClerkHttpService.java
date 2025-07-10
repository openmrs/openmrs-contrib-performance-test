package org.openmrs.performance.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.http.HttpRequestActionBuilder;
import org.openmrs.performance.utils.CommonUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.Constants.ADMIN_SUPER_USER_UUID;
import static org.openmrs.performance.Constants.NOT_URGENT_PRIORITY;
import static org.openmrs.performance.Constants.OUTPATIENT_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.Constants.GENERAL_MEDICINE_SERVICE_UUID;
import static org.openmrs.performance.Constants.OUTPATIENT_CONSULTATION;
import static org.openmrs.performance.Constants.OUTPATIENT_TRIAGE;
import static org.openmrs.performance.Constants.TRIAGE_SERVICE;
import static org.openmrs.performance.Constants.URGENT_PRIORITY;
import static org.openmrs.performance.Constants.PATIENT_IDENTIFIER_UUID;
import static org.openmrs.performance.Constants.USER_GENERATED_PATIENT_LIST;
import static org.openmrs.performance.Constants.WAITING_STATUS;
import static org.openmrs.performance.utils.CommonUtils.getAdjustedDateTimeAsString;
import static org.openmrs.performance.utils.CommonUtils.getCurrentDateTimeAsString;
import static org.openmrs.performance.utils.CommonUtils.getCurrentTimeZone;

public class ClerkHttpService extends HttpService {

	public HttpRequestActionBuilder generateOMRSIdentifier() {
		return http("Generate OMRS Identifier")
		        .post("/openmrs/ws/rest/v1/idgen/identifiersource/8549f706-7e85-4c1d-9424-217d50a2988b/identifier")
		        .body(StringBody("{}")).check(jsonPath("$.identifier").saveAs("identifier"));
	}

	public HttpRequestActionBuilder sendPatientRegistrationRequest() {
		String registrationRequestTemplate = """
		        		{
		        		   "identifiers":[
		        		      {
		        		         "identifier":"#{identifier}",
		        		         "identifierType":"05a29f94-c0ed-11e2-94be-8c13b969e334",
		        		         "location":"44c3efb0-2583-4c80-a79e-1f756a03c0a1",
		        		         "preferred":true
		        		      }
		        		   ],
		        		   "person":{
		        		      "gender":"M",
		        		      "age":47,
		        		      "birthdate":"1970-01-01T00:00:00.000+0100",
		        		      "birthdateEstimated":false,
		        		      "dead":false,
		        		      "deathDate":null,
		        		      "causeOfDeath":null,
		        		      "names":[
		        		         {
		        		            "givenName":"Jayasanka",
		        		            "familyName":"Smith"
		        		         }
		        		      ],
		        		      "addresses": [
		        		        {
		        		        "address1": "30, Vivekananda Layout, Munnekolal,Marathahalli",
		        		        "cityVillage": "Bengaluru",
		        		        "country": "India",
		        		        "postalCode": "560037"
		        		        }
		        		      ]
		        		    }
		        		}

		        """;

		return http("Send Patient Registration Request").post("/openmrs/ws/rest/v1/patient/")
		        .body(StringBody(registrationRequestTemplate)).check(jsonPath("$.uuid").saveAs("patientUuid"));
	}

	public HttpRequestActionBuilder getDefaultAppointmentService() {
		return http("Get all default appointment services").get("/openmrs/ws/rest/v1/appointmentService/all/default");
	}

	public HttpRequestActionBuilder getAppointmentsOfTheDay() {
		String recordedDate = getCurrentDateTimeAsString();

		return http("Get all appointments for a specific day")
		        .get("/openmrs/ws/rest/v1/appointment/all?forDate=" + recordedDate);
	}

	public HttpRequestActionBuilder getAppointmentsSummary() {
		String startDate = getCurrentDateTimeAsString();
		String endDate = CommonUtils.getAdjustedDateTimeAsString(5);

		return http("Get all appointment summaries")
		        .get("/openmrs/ws/rest/v1/appointment/appointmentSummary?startDate=" + startDate + "&endDate=" + endDate);
	}

	public HttpRequestActionBuilder getAppointmentByStatus(String status) {
		String startDate = getCurrentDateTimeAsString();
		String endDate = CommonUtils.getAdjustedDateTimeAsString(5);
		String requestBody = String.format("""
		        {
		            "startDate": "%s",
		        	"endDate": "%s",
		        	"status": "%s"
		        }
		        """, startDate, endDate, status);

		return http("Get Appointment By Status").post("/openmrs/ws/rest/v1/appointments/search")
		        .body(StringBody(requestBody));
	}

	public HttpRequestActionBuilder getAllVisitsOfTheLocationWithDate(String locationUuid) {
		String customRepresentation = "custom:(uuid,patient:(uuid,identifiers:(identifier,uuid),person:(age,display,gender,"
		        + "uuid)),visitType:(uuid,name,display),location:(uuid,name,display),startDatetime,stopDatetime)";

		String startDate = URLEncoder.encode(getCurrentDateTimeAsString(), StandardCharsets.UTF_8);

		return http("Get all visits of the given location with date")
		        .get("/openmrs/ws/rest/v1/visit?includeInactive=true&includeParentLocations=true&v=" + customRepresentation
		                + "&fromStartDate=" + startDate + "&location=" + locationUuid);
	}

	public HttpRequestActionBuilder getAllAppointmentServices() {
		return http("Get All Appointment Services(full)").get("/openmrs/ws/rest/v1/appointmentService/all/full");
	}

	public HttpRequestActionBuilder getAllProviders() {
		return http("Get All Providers").get("/openmrs/ws/rest/v1/provider");
	}

	public HttpRequestActionBuilder getPatientQueueEntry(String patientUuid) {
		String customRepresentation = "custom:(uuid,display,queue,status,patient:(uuid,display,person,identifiers:(uuid,display,identifier,identifierType)),"
		        + "visit:(uuid,display,startDatetime,encounters:(uuid,display,diagnoses,encounterDatetime,encounterType,obs,encounterProviders,voided),"
		        + "attributes:(uuid,display,value,attributeType)),priority,priorityComment,sortWeight,startedAt,endedAt,locationWaitingFor,queueComingFrom,"
		        + "providerWaitingFor,previousQueueEntry)";

		return http("Get Queue Entry").get("/openmrs/ws/rest/v1/queue-entry?v=" + customRepresentation
		        + "&totalCount=true&patient=" + patientUuid + "&isEnded=false");

	}

	public HttpRequestActionBuilder checkAppointmentConflicts() {

		return http("Check Appointment Conflicts").post("/openmrs/ws/rest/v1/appointments/conflicts")
		        .body(StringBody(session -> {

			        String startDatetime = getCurrentDateTimeAsString();
			        String endDatetime = getAdjustedDateTimeAsString(0, 1);
			        session.set("startDateTime", startDatetime);
			        session.set("endDateTime", endDatetime);

			        Map<String, Object> payload = new HashMap<>();
			        payload.put("patientUuid", session.get("patient_uuid"));
			        payload.put("serviceUuid", GENERAL_MEDICINE_SERVICE_UUID);
			        payload.put("startDateTime", getCurrentDateTimeAsString());
			        payload.put("endDateTime", getAdjustedDateTimeAsString(0, 1));
			        payload.put("providers", new ArrayList<>());
			        payload.put("locationUuid", OUTPATIENT_CLINIC_LOCATION_UUID);
			        payload.put("appointmentKind", "Scheduled");
			        try {
				        return new ObjectMapper().writeValueAsString(payload);
			        }
			        catch (JsonProcessingException e) {
				        throw new RuntimeException(e);
			        }
		        }));
	}

	public HttpRequestActionBuilder createAppointment() {

		return http("Create Appointment").post("/openmrs/ws/rest/v1/appointment").body(StringBody(session -> {
			Map<String, Object> payload = new HashMap<>();

			payload.put("appointmentKind", "Scheduled");
			payload.put("status", "");
			payload.put("serviceUuid", GENERAL_MEDICINE_SERVICE_UUID);
			payload.put("startDateTime", session.get("startDateTime"));
			payload.put("endDateTime", session.get("endDateTime"));
			payload.put("locationUuid", OUTPATIENT_CLINIC_LOCATION_UUID);

			Map<String, String> provider = new HashMap<>();
			provider.put("uuid", ADMIN_SUPER_USER_UUID);

			List<Map<String, String>> providers = Collections.singletonList(provider);
			payload.put("providers", providers);
			payload.put("patientUuid", session.get("patient_uuid"));
			payload.put("comments", "Test comments");
			payload.put("dateAppointmentScheduled", getCurrentDateTimeAsString());
			try {
				return new ObjectMapper().writeValueAsString(payload);
			}
			catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		})).check(jsonPath("$.uuid").saveAs("appointmentUuid"));
	}

	public HttpRequestActionBuilder submitAppointmentStatusChange(String appointmentUuid, String status) {
		return http("Submit Appointment StatusChange")
		        .post("/openmrs/ws/rest/v1/appointments/" + appointmentUuid + "/status-change").body(StringBody(session -> {

			        Map<String, Object> statusChangeMessage = new HashMap<>();
			        statusChangeMessage.put("toStatus", status);
			        statusChangeMessage.put("onDate", getCurrentDateTimeAsString());
			        statusChangeMessage.put("timeZone", getCurrentTimeZone());
			        try {
				        return new ObjectMapper().writeValueAsString(statusChangeMessage);
			        }
			        catch (JsonProcessingException e) {
				        throw new RuntimeException(e);
			        }
		        }));
	}

	public HttpRequestActionBuilder getAllPatientsList() {
		return getAllPatientsList("");
	}

	public HttpRequestActionBuilder getAllPatientsList(String cohortType) {
		String customRepresentation = "custom:(uuid,name,description,display,size,attributes,cohortType)";
		String url = "/openmrs/ws/rest/v1/cohortm/cohort?v=" + customRepresentation + "&totalCount=true";

		if (cohortType != null && !cohortType.isEmpty()) {
			String encodedCohortType = URLEncoder.encode(cohortType, StandardCharsets.UTF_8);
			url += "&cohortType=" + encodedCohortType;
		}

		return http("Get all patient lists").get(url);
	}

	public HttpRequestActionBuilder createPatientList() {
		return http("Create a patient list").post("/openmrs/ws/rest/v1/cohortm/cohort/").body(StringBody(session -> {
			String random = new Random().ints(7, 'a', 'z' + 1)
			        .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();

			Map<String, Object> payload = new HashMap<>();

			payload.put("name", random);
			payload.put("description", "Test");
			payload.put("cohortType", USER_GENERATED_PATIENT_LIST);
			payload.put("location", OUTPATIENT_CLINIC_LOCATION_UUID);
			payload.put("startDate", getCurrentDateTimeAsString());
			payload.put("groupCohort", false);
			payload.put("definitionHandlerClassname",
			    "org.openmrs.module.cohort.definition.handler.DefaultCohortDefinitionHandler");

			try {
				return new ObjectMapper().writeValueAsString(payload);
			}
			catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		})).check(jsonPath("$.uuid").saveAs("patientListUuid"));
	}

	public HttpRequestActionBuilder getPatientList(String patientListUuid) {
		String customRepresentation = "custom:(uuid,name,description,display,size,attributes,startDate,endDate,cohortType)";

		return http("Get patient list details")
		        .get("/openmrs/ws/rest/v1/cohortm/cohort/" + patientListUuid + "?v=" + customRepresentation);
	}

	public HttpRequestActionBuilder getMembersOfPatientList(String patientListUuid) {
		return http("Fetch members of patient list").get(
		    "/openmrs/ws/rest/v1/cohortm/cohortmember?cohort=" + patientListUuid + "&startIndex=0&limit=10&v=full&q=");
	}

	public HttpRequestActionBuilder getQueueEntryNumber(String visitUuid) {
		return http("Get queue entry number")
		        .get("/openmrs/ws/rest/v1/queue-entry-number?location=" + OUTPATIENT_CLINIC_LOCATION_UUID + "&queue="
		                + OUTPATIENT_CONSULTATION + "&visit=" + visitUuid + "&visitAttributeType=");
	}

	public HttpRequestActionBuilder submitVisitQueueEntry() {
		return http("Submit visit queue entry").post("/openmrs/ws/rest/v1/visit-queue-entry").body(StringBody(session -> {
			Map<String, Object> payload = new HashMap<>();

			Map<String, Object> visit = new HashMap<>();
			visit.put("uuid", session.getString("visitUuid"));
			payload.put("visit", visit);

			Map<String, Object> queueEntry = new HashMap<>();

			Map<String, Object> status = new HashMap<>();
			status.put("uuid", WAITING_STATUS);
			queueEntry.put("status", status);

			Map<String, Object> priority = new HashMap<>();
			priority.put("uuid", NOT_URGENT_PRIORITY);
			queueEntry.put("priority", priority);

			Map<String, Object> queue = new HashMap<>();
			queue.put("uuid", OUTPATIENT_CONSULTATION);
			queueEntry.put("queue", queue);

			Map<String, Object> patient = new HashMap<>();
			patient.put("uuid", session.getString("patient_uuid"));
			queueEntry.put("patient", patient);

			queueEntry.put("startedAt", getCurrentDateTimeAsString());
			queueEntry.put("sortWeight", 0);

			payload.put("queueEntry", queueEntry);

			try {
				return new ObjectMapper().writeValueAsString(payload);
			}
			catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		})).check(jsonPath("$.uuid").saveAs("serviceQueueEntryUuid"));
	}

	public HttpRequestActionBuilder submitTransitionRequest() {
		return http("Submit transition request").post("/openmrs/ws/rest/v1/queue-entry/transition")
		        .body(StringBody(session -> {
			        Map<String, Object> payload = new HashMap<>();

			        payload.put("queueEntryToTransition", session.getString("serviceQueueEntryUuid"));
			        payload.put("newQueue", OUTPATIENT_TRIAGE);
			        payload.put("newStatus", WAITING_STATUS);
			        payload.put("newPriority", URGENT_PRIORITY);
			        payload.put("newPriorityComment", "Emergency");

			        try {
				        return new ObjectMapper().writeValueAsString(payload);
			        }
			        catch (JsonProcessingException e) {
				        throw new RuntimeException(e);
			        }
		        }));
	}

	public HttpRequestActionBuilder getServiceConceptSet() {
		return http("Get service concept set")
		        .get("/openmrs/ws/rest/v1/systemsetting/queue.serviceConceptSetName?v=custom:(value)")
		        .check(jsonPath("$.value").saveAs("serviceConceptSetUuid"));
	}

	public HttpRequestActionBuilder submitNewServiceQueue() {
		return http("Submit new Service queue").post("/openmrs/ws/rest/v1/queue").body(StringBody(session -> {
			Map<String, Object> payload = new HashMap<>();

			payload.put("name", "Test Triage");
			payload.put("description", "");

			Map<String, Object> service = new HashMap<>();
			service.put("uuid", TRIAGE_SERVICE);
			payload.put("service", service);

			Map<String, Object> location = new HashMap<>();
			location.put("uuid", OUTPATIENT_CLINIC_LOCATION_UUID);
			payload.put("location", location);

			try {
				return new ObjectMapper().writeValueAsString(payload);
			}
			catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		})).check(jsonPath("$.uuid").saveAs("newServiceQueueUuid"));
	}

	public HttpRequestActionBuilder getPatientAttributes(String personUuid) {
		String customRepresentation = "custom:(uuid,display,attributeType:(uuid,display,format),value)";
		return http("Get Person Attributes")
		        .get("/openmrs/ws/rest/v1/person/" + personUuid + "/attribute?v=" + customRepresentation);
	}

	public HttpRequestActionBuilder getPatientIdentifiers(String patientUuid) {
		String customRepresentation = "custom:(uuid,identifier,identifierType:(uuid,required,name),preferred)";
		return http("Get Patient Identifiers")
		        .get("/openmrs/ws/rest/v1/patient/" + patientUuid + "/identifier?v=" + customRepresentation);
	}

	public HttpRequestActionBuilder getPatientRelationships(String personUuid) {
		String customRepresentation = "custom:(display,uuid,personA:(age,display,birthdate,uuid),personB:(age,display,birthdate,uuid),relationshipType:(uuid,display,description,aIsToB,bIsToA))";
		return http("Get Relationships of Person")
		        .get("/openmrs/ws/rest/v1/relationship?v=" + customRepresentation + "&person=" + personUuid);
	}

	public HttpRequestActionBuilder editPatientDetails(String patientUuid) {
		return http("Edit patient details").post("/openmrs/ws/rest/v1/patient/" + patientUuid).body(StringBody(session -> {
			Map<String, Object> payload = new HashMap<>();

			payload.put("uuid", session.get("patient_uuid"));

			Map<String, Object> person = new HashMap<>();
			person.put("uuid", session.get("patient_uuid"));

			List<Map<String, Object>> names = new ArrayList<>();
			Map<String, Object> nameItem = new HashMap<>();
			nameItem.put("uuid", session.getString("patientNameId"));
			nameItem.put("preferred", true);
			nameItem.put("givenName", "Mark");
			nameItem.put("familyName", "Williams");
			names.add(nameItem);
			person.put("names", names);

			person.put("gender", "M");
			person.put("birthdate", "1962-4-5");
			person.put("birthdateEstimated", false);
			person.put("attributes", new ArrayList<>());

			List<Map<String, Object>> addresses = new ArrayList<>();
			Map<String, Object> address = new HashMap<>();
			address.put("address1", "Address16582");
			address.put("cityVillage", "City6582");
			address.put("stateProvince", "State6582");
			address.put("postalCode", "898989");
			address.put("country", "Country6582");
			addresses.add(address);
			person.put("addresses", addresses);

			person.put("dead", false);

			payload.put("person", person);

			List<Map<String, Object>> identifiers = new ArrayList<>();
			Map<String, Object> identifier = new HashMap<>();
			identifier.put("uuid", session.getString("patientIdentifierId"));
			identifier.put("identifier", session.getString("patientIdentifierValue"));
			identifier.put("identifierType", PATIENT_IDENTIFIER_UUID);
			identifier.put("location", OUTPATIENT_CLINIC_LOCATION_UUID);
			identifier.put("preferred", true);
			identifiers.add(identifier);

			payload.put("identifiers", identifiers);

			try {
				return new ObjectMapper().writeValueAsString(payload);
			}
			catch (JsonProcessingException e) {
				throw new RuntimeException("Error converting identifiers to JSON", e);
			}
		}));
	}
}
