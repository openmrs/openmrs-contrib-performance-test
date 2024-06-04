package org.openmrs.performance.http;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

import java.util.Date;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class CommonHttpRequests {
	public static ChainBuilder login = exec(
			http("Login")
					.get("/openmrs/ws/rest/v1/session")
					.header("Authorization", "Basic YWRtaW46QWRtaW4xMjM=")
					.check(jsonPath("$.authenticated").is("true")),
			pause(1),
			http("Get locations")
					.get("/openmrs/ws/fhir2/R4/Location?_summary=data&_count=50&_tag=Login+Location")
					.check(status().is(200)),
			pause(5),
			http("Select Location")
					.get("/openmrs/ws/rest/v1/session")
					.body(StringBody("{\"sessionLocation\":\"44c3efb0-2583-4c80-a79e-1f756a03c0a1\"}"))
					.check(status().is(200))
	);
	
	public static ChainBuilder openHomePage = exec(
			http("Get Address Template")
					.get("/openmrs/ws/rest/v1/addresstemplate"),
			http("Get Relationship Types")
					.get("/openmrs/ws/rest/v1/relationshiptype?v=default"),
			http("Get Appointments for Specific Date")
					.get("/openmrs/ws/rest/v1/appointment/all?forDate=2024-05-15T00:00:00.000+0530"),
			http("Get Module Information")
					.get("/openmrs/ws/rest/v1/module?v=custom:(uuid,version)"),
			http("Get Patient Identifier Types")
					.get("/openmrs/ws/rest/v1/patientidentifiertype?v=custom:(display,uuid,name,format,required,uniquenessBehavior)"),
			http("Get Primary Identifier Term Mapping")
					.get("/openmrs/ws/rest/v1/metadatamapping/termmapping?v=full&code=emr.primaryIdentifierType"),
			http("Get Visits")
					.get("/openmrs/ws/rest/v1/visit?v=custom:(uuid,patient:(uuid,identifiers:(identifier,uuid,identifierType:(name,uuid)),person:(age,display,gender,uuid,attributes:(value,attributeType:(uuid,display)))),visitType:(uuid,name,display),location:(uuid,name,display),startDatetime,stopDatetime)&includeInactive=false&totalCount=true&location=ba685651-ed3b-4e63-9b35-78893060758a"),
			http("Get Identifier Source - ID Card")
					.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=b4143563-16cd-4439-b288-f83d61670fc8"),
			http("Get Identifier Source - OpenMRS ID")
					.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=05a29f94-c0ed-11e2-94be-8c13b969e334"),
			http("Get Identifier Source - Unknown Type 2")
					.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=53fdfd34-f046-4b45-99d8-6d921773e05c"),
			http("Get Identifier Source - Legacy ID")
					.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=22348099-3873-459e-a32e-d93b17eda533"),
			http("Get Auto Generation Options")
					.get("/openmrs/ws/rest/v1/idgen/autogenerationoption?v=full"),
			http("Get Identifier Source - Unknown Type")
					.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=8d79403a-c2cc-11de-8d13-0010c6dffd0f"),
			http("Get Identifier Source - Unknown Type")
					.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=8d793bee-c2cc-11de-8d13-0010c6dffd0f"),
			http("Get Identifier Source - SSN")
					.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=a71403f3-8584-4289-ab41-2b4e5570bd45"),
			http("Get Visits Again")
					.get("/openmrs/ws/rest/v1/visit?v=custom:(uuid,patient:(uuid,identifiers:(identifier,uuid,identifierType:(name,uuid)),person:(age,display,gender,uuid,attributes:(value,attributeType:(uuid,display)))),visitType:(uuid,name,display),location:(uuid,name,display),startDatetime,stopDatetime)&includeInactive=false&totalCount=true&location=ba685651-ed3b-4e63-9b35-78893060758a"),
			http("Get Visits with Offset 50")
					.get("/openmrs/ws/rest/v1/visit?v=custom:(uuid,patient:(uuid,identifiers:(identifier,uuid,identifierType:(name,uuid)),person:(age,display,gender,uuid,attributes:(value,attributeType:(uuid,display)))),visitType:(uuid,name,display),location:(uuid,name,display),startDatetime,stopDatetime)&includeInactive=false&totalCount=true&location=ba685651-ed3b-4e63-9b35-78893060758a&startIndex=50"),
			http("Get Visits Again with Offset 100")
					.get("/openmrs/ws/rest/v1/visit?v=custom:(uuid,patient:(uuid,identifiers:(identifier,uuid,identifierType:(name,uuid)),person:(age,display,gender,uuid,attributes:(value,attributeType:(uuid,display)))),visitType:(uuid,name,display),location:(uuid,name,display),startDatetime,stopDatetime)&includeInactive=false&totalCount=true&location=ba685651-ed3b-4e63-9b35-78893060758a&startIndex=100")
	);
	
	public static HttpRequestActionBuilder getAddresstemplate = http("Get Address Template")
			.get("/openmrs/ws/rest/v1/addresstemplate");
	
	public static HttpRequestActionBuilder getRelationshipTypes = http("Get Relationship Types")
			.get("/openmrs/ws/rest/v1/relationshiptype?v=default");
	
	public static HttpRequestActionBuilder getAppointmentsForSpecificDate(Date date) {
		return http("Get Appointments for Specific Date")
				.get("/openmrs/ws/rest/v1/appointment/all?forDate=" + date);
	}
	
	public static HttpRequestActionBuilder getModuleInformation = http("Get Module Information")
			.get("/openmrs/ws/rest/v1/module?v=custom:(uuid,version)");
	
	public static HttpRequestActionBuilder getPatientIdentifierTypes = http("Get Patient Identifier Types")
			.get("/openmrs/ws/rest/v1/patientidentifiertype?v=custom:(display,uuid,name,format,required,uniquenessBehavior)");
	
	public static HttpRequestActionBuilder getPrimaryIdentifierTermMapping = http("Get Primary Identifier Term Mapping")
			.get("/openmrs/ws/rest/v1/metadatamapping/termmapping?v=full&code=emr.primaryIdentifierType");
	
	
	
}
