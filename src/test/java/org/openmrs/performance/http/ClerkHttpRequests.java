package org.openmrs.performance.http;

import io.gatling.javaapi.core.ChainBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class ClerkHttpRequests {
	private static final String registrationRequestTemplate = """
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
	
	public static ChainBuilder openRegistrationPage = exec(
			http("Get Address Template")
					.get("/openmrs/ws/rest/v1/addresstemplate"),
			http("Get Patient Identifier Types")
					.get("/openmrs/ws/rest/v1/patientidentifiertype?v=custom:(display,uuid,name,format,required,uniquenessBehavior)"),
			http("Get Primary Identifier Term Mapping")
					.get("/openmrs/ws/rest/v1/metadatamapping/termmapping?v=full&code=emr.primaryIdentifierType"),
			http("Get Relationship Types")
					.get("/openmrs/ws/rest/v1/relationshiptype?v=default"),
			http("Get Module Information")
					.get("/openmrs/ws/rest/v1/module?v=custom:(uuid,version)"),
			http("Get Person Attribute Type")
					.get("/openmrs/ws/rest/v1/personattributetype/14d4f066-15f5-102d-96e4-000c29c2a5d7"),
			http("Get Address Template")
					.get("/openmrs/ws/rest/v1/addresstemplate"),
			http("Get Patient Identifier Types")
					.get("/openmrs/ws/rest/v1/patientidentifiertype?v=custom:(display,uuid,name,format,required,uniquenessBehavior)"),
			http("Get Relationship Types")
					.get("/openmrs/ws/rest/v1/relationshiptype?v=default"),
			http("Get Primary Identifier Term Mapping")
					.get("/openmrs/ws/rest/v1/metadatamapping/termmapping?v=full&code=emr.primaryIdentifierType"),
			http("Get Auto Generation Options")
					.get("/openmrs/ws/rest/v1/idgen/autogenerationoption?v=full"),
			http("Get Identifier Source - OpenMRS ID")
					.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=05a29f94-c0ed-11e2-94be-8c13b969e334"),
			http("Get Identifier Source - ID Card")
					.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=b4143563-16cd-4439-b288-f83d61670fc8"),
			http("Get Identifier Source - Legacy ID")
					.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=22348099-3873-459e-a32e-d93b17eda533"),
			http("Get Identifier Source - Unknown Type")
					.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=8d79403a-c2cc-11de-8d13-0010c6dffd0f"),
			http("Get Identifier Source - Unknown Type")
					.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=8d79403a-c2cc-11de-8d13-0010c6dffd0f"),
			http("Get Identifier Source - SSN")
					.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=a71403f3-8584-4289-ab41-2b4e5570bd45"),
			http("Get Auto Generation Options")
					.get("/openmrs/ws/rest/v1/idgen/autogenerationoption?v=full"),
			http("Get Identifier Source - ID Card")
					.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=b4143563-16cd-4439-b288-f83d61670fc8"),
			http("Get Identifier Source - OpenMRS ID")
					.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=05a29f94-c0ed-11e2-94be-8c13b969e334"),
			http("Get Identifier Source - Legacy ID")
					.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=22348099-3873-459e-a32e-d93b17eda533"),
			http("Get Identifier Source - Unknown Type")
					.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=8d79403a-c2cc-11de-8d13-0010c6dffd0f"),
			http("Get Identifier Source - Unknown Type")
					.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=8d79403a-c2cc-11de-8d13-0010c6dffd0f"),
			http("Get Identifier Source - SSN")
					.get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default&identifierType=a71403f3-8584-4289-ab41-2b4e5570bd45"),
			http("Get Ordered Address Hierarchy Levels")
					.get("/openmrs/module/addresshierarchy/ajax/getOrderedAddressHierarchyLevels.form")
	);
	
	public static ChainBuilder registerPatient = exec(
			http("Generate OMRS Identifier")
					.post("/openmrs/ws/rest/v1/idgen/identifiersource/8549f706-7e85-4c1d-9424-217d50a2988b/identifier")
					.body(StringBody("{}"))
					.check(jsonPath("$.identifier").saveAs("identifier"))
					.check(bodyString().saveAs("responseBody")),
			http("Send Patient Registration Request")
					.post("/openmrs/ws/rest/v1/patient/")
					.body(StringBody(registrationRequestTemplate))
					.check(bodyString().saveAs("registrationResponseBody"))
					.check(jsonPath("$.uuid").saveAs("patientUuid")),
			pause(2),
			// Redirects to the created patient's chart page
			http("Get Patient Summary Data 1")
					.get("/openmrs/ws/fhir2/R4/Patient/#{patientUuid}?_summary=data"),
			http("Get Patient Visits")
					.get("/openmrs/ws/rest/v1/visit?patient=#{patientUuid}&v=custom:(uuid,encounters:(uuid,encounterDatetime,form:(uuid,name),location:ref,encounterType:ref,encounterProviders:(uuid,display,provider:(uuid,display))),patient:(uuid,uuid),visitType:(uuid,name,display),attributes:(uuid,display,attributeType:(name,datatypeClassname,uuid),value),location:(uuid,name,display),startDatetime,stopDatetime)&includeInactive=false"),
			http("Get Patient Observations Set 1")
					.get("/openmrs/ws/fhir2/R4/Observation?subject:Patient=#{patientUuid}&code=5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5092AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5242AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C165095AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C1343AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA&_summary=data&_sort=-date&_count=100"),
			http("Get Visit Queue Entry")
					.get("/openmrs/ws/rest/v1/visit-queue-entry?patient=#{patientUuid}"),
			http("Get Patient Observations Set 2")
					.get("/openmrs/ws/fhir2/R4/Observation?subject:Patient=#{patientUuid}&code=5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5092AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5242AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C165095AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA&_summary=data&_sort=-date&_count=100"),
			http("Get Patient Observations Set 3")
					.get("/openmrs/ws/fhir2/R4/Observation?subject:Patient=#{patientUuid}&code=5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C1343AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA&_summary=data&_sort=-date&_count=100"),
			http("Get Patient Conditions")
					.get("/openmrs/ws/fhir2/R4/Condition?patient=#{patientUuid}&_count=100&_summary=data"),
			http("Get Active Orders")
					.get("/openmrs/ws/rest/v1/order?patient=#{patientUuid}&careSetting=6f0c9a92-6f24-11e3-af88-005056821db0&status=ACTIVE&orderType=131168f4-15f5-102d-96e4-000c29c2a5d7&v=custom:(uuid,dosingType,orderNumber,accessionNumber,patient:ref,action,careSetting:ref,previousOrder:ref,dateActivated,scheduledDate,dateStopped,autoExpireDate,orderType:ref,encounter:ref,orderer:(uuid,display,person:(display)),orderReason,orderReasonNonCoded,orderType,urgency,instructions,commentToFulfiller,drug:(uuid,display,strength,dosageForm:(display,uuid),concept),dose,doseUnits:ref,frequency:ref,asNeeded,asNeededCondition,quantity,quantityUnits:ref,numRefills,dosingInstructions,duration,durationUnits:ref,route:ref,brandName,dispenseAsWritten)"));
	
	

}
