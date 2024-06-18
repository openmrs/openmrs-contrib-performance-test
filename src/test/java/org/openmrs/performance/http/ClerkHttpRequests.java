package org.openmrs.performance.http;

import io.gatling.http.action.HttpActionBuilder;
import io.gatling.http.request.builder.HttpRequestBuilder;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

import java.util.Set;
import java.util.StringJoiner;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class ClerkHttpRequests {
	public static final String registrationRequestTemplate = """
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
	public static HttpRequestActionBuilder generateOMRSIdentifier() {
		return http("Generate OMRS Identifier")
				.post("/openmrs/ws/rest/v1/idgen/identifiersource/8549f706-7e85-4c1d-9424-217d50a2988b/identifier")
				.body(StringBody("{}"))
				.check(jsonPath("$.identifier").saveAs("identifier"))
				.check(bodyString().saveAs("responseBody"));
	}
	
	public static HttpRequestActionBuilder sendPatientRegistrationRequest() {
		return http("Send Patient Registration Request")
				.post("/openmrs/ws/rest/v1/patient/")
				.body(StringBody(registrationRequestTemplate))
				.check(bodyString().saveAs("registrationResponseBody"))
				.check(jsonPath("$.uuid").saveAs("patientUuid"));
	}
	
	public static HttpRequestActionBuilder getPatientSummaryData(String patientUuid) {
		return http("Get Patient Summary Data")
				.get("/openmrs/ws/fhir2/R4/Patient/"+patientUuid+"?_summary=data");
	}
	
	public static HttpRequestActionBuilder getPatientObservations(String patientUuid, Set<String> observationTypes) {
		// Join the observationTypes array into a single string with "%2C" as the delimiter
		StringJoiner joiner = new StringJoiner("%2C");
		for (String code : observationTypes) {
			joiner.add(code);
		}
		String codesParam = joiner.toString();
		
		// Construct the URL with the dynamically joined observationTypes
		String url = String.format("/openmrs/ws/fhir2/R4/Observation?subject:Patient=%s&code=%s&_summary=data&_sort=-date&_count=100", patientUuid, codesParam);
		
		return http("Get Patient Observations").get(url);
	}
	

	
	public static HttpRequestActionBuilder getPatientConditions(String patientUuid) {
		return http("Get Patient Conditions")
				.get("/openmrs/ws/fhir2/R4/Condition?patient="+patientUuid+"&_count=100&_summary=data");
	}
	
	public static HttpRequestActionBuilder getActiveOrders(String patientUuid) {
		return http("Get Active Orders")
				.get("/openmrs/ws/rest/v1/order?patient="+patientUuid+"&careSetting=6f0c9a92-6f24-11e3-af88-005056821db0&status=ACTIVE&orderType=131168f4-15f5-102d-96e4-000c29c2a5d7&v=custom:(uuid,dosingType,orderNumber,accessionNumber,patient:ref,action,careSetting:ref,previousOrder:ref,dateActivated,scheduledDate,dateStopped,autoExpireDate,orderType:ref,encounter:ref,orderer:(uuid,display,person:(display)),orderReason,orderReasonNonCoded,orderType,urgency,instructions,commentToFulfiller,drug:(uuid,display,strength,dosageForm:(display,uuid),concept),dose,doseUnits:ref,frequency:ref,asNeeded,asNeededCondition,quantity,quantityUnits:ref,numRefills,dosingInstructions,duration,durationUnits:ref,route:ref,brandName,dispenseAsWritten)");
	}
	
//	public static HttpRequestActionBuilder getPatientIdentifierTypes() {
//		return http("Get Patient Identifier Types")
//				.get("/openmrs/ws/rest/v1/patientidentifiertype?v=custom:(display,uuid,name,format,required,uniquenessBehavior)");
//	}
//
//	public static HttpRequestActionBuilder getPrimaryIdentifierTermMapping() {
//		return http("Get Primary Identifier Term Mapping")
//				.get("/openmrs/ws/rest/v1/metadatamapping/termmapping?v=full&code=emr.primaryIdentifierType");
//	}
//
//	public static HttpRequestActionBuilder getModuleInformation() {
//		return http("Get Module Information")
//				.get("/openmrs/ws/rest/v1/module?v=custom:(uuid,version)");
//	}
//
//	public static HttpRequestActionBuilder getRelationshipTypes() {
//		return http("Get Relationship Types")
//				.get("/openmrs/ws/rest/v1/relationshiptype?v=default");
//	}
	
	public static HttpRequestActionBuilder getPersonAttributeType(String personAttributeTypeUuid) {
		return http("Get Person Attribute Type")
				.get("/openmrs/ws/rest/v1/personattributetype/"+personAttributeTypeUuid);
	}
	
//	public static HttpRequestActionBuilder getAutoGenerationOptions() {
//		return http("Get Auto Generation Options")
//				.get("/openmrs/ws/rest/v1/idgen/autogenerationoption?v=full");
//	}
	
	public static HttpRequestActionBuilder getOrderedAddressHierarchyLevels() {
		return http("Get Ordered Address Hierarchy Levels")
				.get("/openmrs/module/addresshierarchy/ajax/getOrderedAddressHierarchyLevels.form");
	}
}
