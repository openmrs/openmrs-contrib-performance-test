package org.openmrs.performance.http;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import static org.openmrs.performance.Constants.OUTPATIENT_CLINIC_LOCATION_UUID;

public class CommonHttpRequests {
	
	public static HttpRequestActionBuilder loginRequest() {
		return http("Login")
				.get("/openmrs/ws/rest/v1/session")
				.header("Authorization", "Basic YWRtaW46QWRtaW4xMjM=")
				.check(jsonPath("$.authenticated").is("true"));
	}
	
	public static HttpRequestActionBuilder getLocations() {
		return http("Get locations")
				.get("/openmrs/ws/fhir2/R4/Location?_summary=data&_count=50&_tag=Login+Location");
	}
	
	public static HttpRequestActionBuilder selectLocation() {
		return http("Select Location")
				.get("/openmrs/ws/rest/v1/session")
				.body(StringBody("{\"sessionLocation\":\"" + OUTPATIENT_CLINIC_LOCATION_UUID + "\"}"));
	}
	
	public static HttpRequestActionBuilder getAddressTemplate() {
		return http("Get Address Template")
				.get("/openmrs/ws/rest/v1/addresstemplate");
	}
	
	public static HttpRequestActionBuilder getRelationshipTypes() {
		return http("Get Relationship Types")
				.get("/openmrs/ws/rest/v1/relationshiptype?v=default");
	}
	
	public static HttpRequestActionBuilder getAppointmentsForSpecificDate(String date) {
		return http("Get Appointments for Specific Date")
				.get("/openmrs/ws/rest/v1/appointment/all?forDate=" + date);
	}
	
	public static HttpRequestActionBuilder getModuleInformation() {
		return http("Get Module Information")
				.get("/openmrs/ws/rest/v1/module?v=custom:(uuid,version)");
	}
	
	public static HttpRequestActionBuilder getPatientIdentifierTypes() {
		return http("Get Patient Identifier Types")
				.get("/openmrs/ws/rest/v1/patientidentifiertype?v=custom:(display,uuid,name,format,required,uniquenessBehavior)");
	}
	
	public static HttpRequestActionBuilder getPrimaryIdentifierTermMapping() {
		return http("Get Primary Identifier Term Mapping")
				.get("/openmrs/ws/rest/v1/metadatamapping/termmapping?v=full&code=emr.primaryIdentifierType");
	}
	
	public static HttpRequestActionBuilder getVisits(String locationUuid) {
		return http("Get Visits")
				.get("/openmrs/ws/rest/v1/visit?v=custom:(uuid,patient:(uuid,identifiers:(identifier,uuid,identifierType:(name,uuid)),person:(age,display,gender,uuid,attributes:(value,attributeType:(uuid,display)))),visitType:(uuid,name,display),location:(uuid,name,display),startDatetime,stopDatetime)&includeInactive=false&totalCount=true&location=" + locationUuid);
	}
	
	public static HttpRequestActionBuilder getIdentifierSources(String identifierSourceUuid) {
		return http("Get Identifier Source")
				.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=" + identifierSourceUuid);
	}
	
	public static HttpRequestActionBuilder getAutoGenerationOptions() {
		return http("Get Auto Generation Options")
				.get("/openmrs/ws/rest/v1/idgen/autogenerationoption?v=full");
	}
	
	public static HttpRequestActionBuilder getVisitQueueEntry(String patientUuid) {
		return http("Get Visit Queue Entry")
				.get("/openmrs/ws/rest/v1/visit-queue-entry??v=full&patient="+patientUuid);
	}
	
	public static HttpRequestActionBuilder getCurrentVisit(String patientUuid) {
		return http("Get Patient's current visit")
				.get("/openmrs/ws/rest/v1/visit?patient="+patientUuid+"&v=custom:(uuid,encounters:(uuid,encounterDatetime,form:(uuid,name),location:ref,encounterType:ref,encounterProviders:(uuid,display,provider:(uuid,display,person:(display))),patient:(uuid,uuid),visitType:(uuid,name,display),attributes:(uuid,display,attributeType:(name,datatypeClassname,uuid),value),location:(uuid,name,display),startDatetime,stopDatetime)&includeInactive=false");
	}
	
}
