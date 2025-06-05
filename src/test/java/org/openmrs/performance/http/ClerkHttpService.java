package org.openmrs.performance.http;

import io.gatling.javaapi.http.HttpRequestActionBuilder;
import org.openmrs.performance.utils.CommonUtils;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.bodyString;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.Constants.OUTPATIENT_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.Constants.PATIENT_IDENTIFICATION_PHOTO;
import static org.openmrs.performance.Constants.GENERAL_MEDICINE_SERVICE_UUID;

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

	public HttpRequestActionBuilder getPersonAttributeType(String personAttributeTypeUuid) {
		return http("Get Person Attribute Type").get("/openmrs/ws/rest/v1/personattributetype/" + personAttributeTypeUuid);
	}

	public HttpRequestActionBuilder getOrderedAddressHierarchyLevels() {
		return http("Get Ordered Address Hierarchy Levels")
		        .get("/openmrs/module/addresshierarchy/ajax/getOrderedAddressHierarchyLevels.form");
	}

	public HttpRequestActionBuilder getDefaultAppointmentService() {
		return http("Get all default appointment services").get("/openmrs/ws/rest/v1/appointmentService/all/default");
	}

	public HttpRequestActionBuilder getAppointmentsOfTheDay() {
		String recordedDate = CommonUtils.getCurrentDateTimeAsString();
		return http("Get all appointments for a specific day")
		        .get("/openmrs/ws/rest/v1/appointment/all?forDate=" + recordedDate);
	}

	public HttpRequestActionBuilder getAppointmentsSummary() {
		String startDate = CommonUtils.getCurrentDateTimeAsString();
		String endDate = CommonUtils.getAdjustedDateTimeAsString(5);
		return http("Get all appointment summaries")
		        .get("/openmrs/ws/rest/v1/appointment/appointmentSummary?startDate=" + startDate + "&endDate=" + endDate);
	}

	public HttpRequestActionBuilder getAppointmentByStatus(String status) {
		String startDate = CommonUtils.getCurrentDateTimeAsString();
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
		return http("Get all visits of the given location with date")
		        .get("/openmrs/ws/rest/v1/visit?includeInactive=true&includeParentLocations=true&v=" + customRepresentation
		                + "&fromStartDate=2025-06-02&location=" + locationUuid);
	}

	public HttpRequestActionBuilder getPatients(String searchQuery) {
		String customRepresentation = "custom:(patientId,uuid,identifiers,display,patientIdentifier:(uuid,identifier),"
		        + "person:(gender,age,birthdate,birthdateEstimated,personName,addresses,display,dead,deathDate),attributes:"
		        + "(value,attributeType:(uuid,display)))";
		return http("Get Patients").get("/openmrs/ws/rest/v1/patient?q=" + searchQuery + "&v=" + customRepresentation
		        + "&includeDead=false&limit=50&totalCount=true").check(bodyString().saveAs("patientSearchResults"));
	}

	public HttpRequestActionBuilder getPatientIdPhoto(String patientUuid) {
		return http("Get patient's identification photo").get(
		    "/openmrs/ws/rest/v1/obs?patient=" + patientUuid + "&concept=" + PATIENT_IDENTIFICATION_PHOTO + "&v=full");
	}

	public HttpRequestActionBuilder getAppointmentLocations() {
		return http("Get Appointment Locations").get("/openmrs/ws/rest/v1/location?tag=Appointment+Location");
	}

	public HttpRequestActionBuilder getAllAppointmentServices() {
		return http("Get All Appointment Services(full)").get("/openmrs/ws/rest/v1/appointmentService/all/full");
	}

	public HttpRequestActionBuilder getPatientQueueEntry(String patientUuid) {
		String customRepresentation = "custom:(uuid,display,queue,status,patient:(uuid,display,person,identifiers:(uuid,display,identifier,identifierType)),"
		        + "visit:(uuid,display,startDatetime,encounters:(uuid,display,diagnoses,encounterDatetime,encounterType,obs,encounterProviders,voided),"
		        + "attributes:(uuid,display,value,attributeType)),priority,priorityComment,sortWeight,startedAt,endedAt,locationWaitingFor,queueComingFrom,"
		        + "providerWaitingFor,previousQueueEntry)";

		return http("Get Queue Entry").get("/openmrs/ws/rest/v1/queue-entry?v=" + customRepresentation
		        + "&totalCount=true&patient=" + patientUuid + "&isEnded=false");
	}

	public HttpRequestActionBuilder getAllProviders() {
		return http("Get All Providers").get("/openmrs/ws/rest/v1/provider");
	}

	public HttpRequestActionBuilder checkAppointmentConflicts(String patientUuid, String startDateTime, String endDateTime) {
		String requestBody = String.format("""
		        {
		            "patientUuid": "%s",
		            "serviceUuid": "%s",
		            "startDateTime": "%s",
		            "endDateTime": "%s",
		            "providers": [],
		            "locationUuid": "%s",
		            "appointmentKind": "Scheduled"
		        }
		        """, patientUuid, GENERAL_MEDICINE_SERVICE_UUID, startDateTime, endDateTime,
		    OUTPATIENT_CLINIC_LOCATION_UUID);
		return http("Check Appointment Conflicts").post("/openmrs/ws/rest/v1/appointments/conflicts")
		        .body(StringBody(requestBody));
	}

	public HttpRequestActionBuilder createAppointment(String patientUuid, String startDateTime, String endDateTime) {
		String requestBody = String.format("""
		        {
		            "appointmentKind": "Scheduled",
		            "status": "",
		            "serviceUuid": "%s",
		            "startDateTime": "%s",
		            "endDateTime": "%s",
		            "locationUuid": "%s",
		            "providers": [
		                {
		                    "uuid": "705f5791-07a7-44b8-932f-a81f3526fc98"
		                }
		            ],
		            "patientUuid": "%s",
		            "comments": "Hi",
		            "dateAppointmentScheduled": "2025-03-24T23:01:46+05:30"
		        }
		        """, GENERAL_MEDICINE_SERVICE_UUID, startDateTime, endDateTime, OUTPATIENT_CLINIC_LOCATION_UUID,
		    patientUuid);
		return http("Create Appointment").post("/openmrs/ws/rest/v1/appointment").body(StringBody(requestBody));
	}
}
