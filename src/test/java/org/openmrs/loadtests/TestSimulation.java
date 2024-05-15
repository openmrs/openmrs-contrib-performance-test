package org.openmrs.loadtests;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.util.Map;

/**
 * Unit test for simple App.
 */
public class TestSimulation extends Simulation {
	
	HttpProtocolBuilder httpProtocol =
			http.baseUrl("https://o3.openmrs.org")
					.acceptHeader("application/json, text/plain, */*")
					.acceptLanguageHeader("en-US,en;q=0.5")
					.userAgentHeader(
							"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/119.0"
					);
	
	Map<String, String> sessionHeaders= Map.of("Authorization","Bearer YWRtaW46QWRtaW4xMjM=");
	
	ChainBuilder login = exec(
			http("Login")
				.get("/openmrs/ws/rest/v1/session")
				.header("Authorization", "Basic YWRtaW46QWRtaW4xMjM=")
				.check(jsonPath("$.authenticated").is("true")),
			pause(1),
			http("Get locations")
				.get("/openmrs/ws/fhir2/R4/Location?_summary=data&_count=50&_tag=Login+Location")
				.headers(sessionHeaders)
				.check(status().is(200)),
			pause(5),
			http("Select Location")
				.get("/openmrs/rest/v1/session")
					.body(StringBody("{\"sessionLocation\":\"44c3efb0-2583-4c80-a79e-1f756a03c0a1\"}"))
				.headers(sessionHeaders)
				.check(status().is(200))
	);
	
	ChainBuilder openHomePage = exec(
					http("request_0")
							.get("/openmrs/spa/home")
							.headers(sessionHeaders)
							.resources(
									http("request_1")
											.get("/openmrs/spa/importmap.json"),
									http("request_2")
											.get("/openmrs/ws/rest/v1/session")
											,
									http("request_3")
											.get("/openmrs/spa/routes.registry.json")
											,
									http("request_4")
											.get("/openmrs/ws/rest/v1/addresstemplate")
											,
									http("request_5")
											.get("/openmrs/ws/rest/v1/session")
											,
									http("request_6")
											.get("/openmrs/ws/rest/v1/relationshiptype?v=default")
											,
									http("request_7")
											.get("/openmrs/ws/rest/v1/appointment/all?forDate=2024-05-15T00:00:00.000+0530")
											,
									http("request_8")
											.get("/openmrs/ws/rest/v1/module?v=custom:(uuid,version)")
											,
									http("request_9")
											.get("/openmrs/ws/rest/v1/patientidentifiertype?v=custom:(display,uuid,name,format,required,uniquenessBehavior)")
											,
									http("request_10")
											.get("/openmrs/ws/rest/v1/metadatamapping/termmapping?v=full&code=emr.primaryIdentifierType")
											,
									http("request_11")
											.get("/openmrs/ws/rest/v1/visit?v=custom:(uuid,patient:(uuid,identifiers:(identifier,uuid,identifierType:(name,uuid)),person:(age,display,gender,uuid,attributes:(value,attributeType:(uuid,display)))),visitType:(uuid,name,display),location:(uuid,name,display),startDatetime,stopDatetime)&includeInactive=false&totalCount=true&location=ba685651-ed3b-4e63-9b35-78893060758a")
											,
									http("request_12")
											.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=b4143563-16cd-4439-b288-f83d61670fc8")
											,
									http("request_13")
											.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=05a29f94-c0ed-11e2-94be-8c13b969e334")
											,
									http("request_14")
											.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=53fdfd34-f046-4b45-99d8-6d921773e05c")
											,
									http("request_15")
											.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=22348099-3873-459e-a32e-d93b17eda533")
											,
									http("request_16")
											.get("/openmrs/ws/rest/v1/idgen/autogenerationoption?v=full")
											,
									http("request_17")
											.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=8d79403a-c2cc-11de-8d13-0010c6dffd0f")
											,
									http("request_18")
											.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=8d793bee-c2cc-11de-8d13-0010c6dffd0f")
											,
									http("request_19")
											.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=a71403f3-8584-4289-ab41-2b4e5570bd45")
											,
									http("request_20")
											.get("/openmrs/ws/rest/v1/visit?v=custom:(uuid,patient:(uuid,identifiers:(identifier,uuid,identifierType:(name,uuid)),person:(age,display,gender,uuid,attributes:(value,attributeType:(uuid,display)))),visitType:(uuid,name,display),location:(uuid,name,display),startDatetime,stopDatetime)&includeInactive=false&totalCount=true&location=ba685651-ed3b-4e63-9b35-78893060758a")
											,
									http("request_21")
											.get("/openmrs/ws/rest/v1/visit?v=custom:(uuid,patient:(uuid,identifiers:(identifier,uuid,identifierType:(name,uuid)),person:(age,display,gender,uuid,attributes:(value,attributeType:(uuid,display)))),visitType:(uuid,name,display),location:(uuid,name,display),startDatetime,stopDatetime)&includeInactive=false&totalCount=true&location=ba685651-ed3b-4e63-9b35-78893060758a&startIndex=50")
											,
									http("request_22")
											.get("/openmrs/ws/rest/v1/visit?v=custom:(uuid,patient:(uuid,identifiers:(identifier,uuid,identifierType:(name,uuid)),person:(age,display,gender,uuid,attributes:(value,attributeType:(uuid,display)))),visitType:(uuid,name,display),location:(uuid,name,display),startDatetime,stopDatetime)&includeInactive=false&totalCount=true&location=ba685651-ed3b-4e63-9b35-78893060758a")
											,
									http("request_23")
											.get("/openmrs/ws/rest/v1/visit?v=custom:(uuid,patient:(uuid,identifiers:(identifier,uuid,identifierType:(name,uuid)),person:(age,display,gender,uuid,attributes:(value,attributeType:(uuid,display)))),visitType:(uuid,name,display),location:(uuid,name,display),startDatetime,stopDatetime)&includeInactive=false&totalCount=true&location=ba685651-ed3b-4e63-9b35-78893060758a&startIndex=100")));
											
									
	
	ChainBuilder search = exec(http("Home").get("/"),
			pause(1),
			http("Search")
					.get("/computers?f=ChipTest")
	);
	
	ScenarioBuilder jaye = scenario("Jaye")
			.exec(login)
			.exec(openHomePage);
	
	ScenarioBuilder doctor = scenario("Doctor")
			.exec(search);
	
	{
		setUp(jaye.injectOpen(rampUsers(100).during(10))
		).protocols(httpProtocol);
	}
}
