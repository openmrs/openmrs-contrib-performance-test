package org.openmrs.performance.http;

import io.gatling.javaapi.http.HttpRequestActionBuilder;
import java.util.Set;
import java.util.StringJoiner;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.Constants.OUTPATIENT_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.Constants.VITAL_SIGNS_CONCEPT_SET;
import static org.openmrs.performance.Constants.CARE_SETTING_UUID;
import static org.openmrs.performance.Constants.DRUG_ORDER;

public abstract class HttpService {

	public HttpRequestActionBuilder loginRequest() {
		return http("Login").get("/openmrs/ws/rest/v1/session").header("Authorization", "Basic YWRtaW46QWRtaW4xMjM=")
		        .check(jsonPath("$.authenticated").is("true"))
		        .check(jsonPath("$.currentProvider.uuid").saveAs("currentUserUuid"));
	}

	public HttpRequestActionBuilder getLocations() {
		return http("Get locations").get("/openmrs/ws/fhir2/R4/Location?_summary=data&_count=50&_tag=Login+Location");
	}

	public HttpRequestActionBuilder selectLocation() {
		return http("Select Location").get("/openmrs/ws/rest/v1/session")
		        .body(StringBody("{\"sessionLocation\":\"" + OUTPATIENT_CLINIC_LOCATION_UUID + "\"}"));
	}

	public HttpRequestActionBuilder getAddressTemplate() {
		return http("Get Address Template").get("/openmrs/ws/rest/v1/addresstemplate");
	}

	public HttpRequestActionBuilder getRelationshipTypes() {
		return http("Get Relationship Types").get("/openmrs/ws/rest/v1/relationshiptype?v=default");
	}

	public HttpRequestActionBuilder getAppointmentsForSpecificDate(String date) {
		return http("Get Appointments for Specific Date").get("/openmrs/ws/rest/v1/appointment/all?forDate=" + date);
	}

	public HttpRequestActionBuilder getModuleInformation() {
		return http("Get Module Information").get("/openmrs/ws/rest/v1/module?v=custom:(uuid,version)");
	}

	public HttpRequestActionBuilder getPatientIdentifierTypes() {
		return http("Get Patient Identifier Types").get(
		    "/openmrs/ws/rest/v1/patientidentifiertype?v=custom:(display,uuid,name,format,required,uniquenessBehavior)");
	}

	public HttpRequestActionBuilder getPrimaryIdentifierTermMapping() {
		return http("Get Primary Identifier Term Mapping")
		        .get("/openmrs/ws/rest/v1/metadatamapping/termmapping?v=full&code=emr.primaryIdentifierType");
	}

	public HttpRequestActionBuilder getVisitsOfLocation(String locationUuid) {
		String customRepresentation = "custom:(uuid,patient:(uuid,identifiers:(identifier,uuid,identifierType:(name,uuid)),"
		        + "person:(age,display,gender,uuid,attributes:(value,attributeType:(uuid,display)))),visitType:(uuid,name,display),"
		        + "location:(uuid,name,display),startDatetime,stopDatetime)";

		return http("Get Visits").get("/openmrs/ws/rest/v1/visit?v=" + customRepresentation
		        + "&includeInactive=false&totalCount=true&location=" + locationUuid);
	}

	public HttpRequestActionBuilder getIdentifierSources() {
		return http("Get Identifier Source").get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default");
	}

	public HttpRequestActionBuilder getAutoGenerationOptions() {
		return http("Get Auto Generation Options").get("/openmrs/ws/rest/v1/idgen/autogenerationoption?v=full");
	}

	public HttpRequestActionBuilder getPatientEncounters() {
		String customString = "custom:(uuid,display,encounterDatetime,form,encounterType,visit,patient,"
		        + "obs:(uuid,concept:(uuid,display,conceptClass:(uuid,display)),display,groupMembers:(uuid,concept:"
		        + "(uuid,display),value:(uuid,display),display),value,obsDatetime),encounterProviders:(provider:(person)))";

		return http("Get Patient Encounters")
		        .get("/openmrs/ws/rest/v1/encounter?patient=15e1a39b-005f-4659-97ce-8dbe60a84579&v=" + customString
		                + "order=desc&limit=20&startIndex=0&totalCount=true");
	}

	public HttpRequestActionBuilder getVisitQueueEntry(String patientUuid) {
		return http("Get Visit Queue Entry").get("/openmrs/ws/rest/v1/visit-queue-entry??v=full&patient=" + patientUuid);
	}

	public HttpRequestActionBuilder getActiveVisitOfPatient(String patientUuid) {
		String customRepresentation = "custom:(uuid,display,voided,indication,startDatetime,stopDatetime,"
		        + "encounters:(uuid,display,encounterDatetime," + "form:(uuid,name),location:ref," + "encounterType:ref,"
		        + "encounterProviders:(uuid,display," + "provider:(uuid,display)))," + "patient:(uuid,display),"
		        + "visitType:(uuid,name,display),"
		        + "attributes:(uuid,display,attributeType:(name,datatypeClassname,uuid),value),"
		        + "location:(uuid,name,display))";

		return http("Get Active Visits of Patient").get(
		    "/openmrs/ws/rest/v1/visit?patient=" + patientUuid + "&v=" + customRepresentation + "&includeInactive=false");
	}

	public HttpRequestActionBuilder getCurrentVisit(String patientUuid) {
		String customRepresentation = "custom:(uuid,encounters:(uuid,diagnoses:(uuid,display,rank,diagnosis),form:(uuid,display),"
		        + "encounterDatetime,orders:full,obs:full,encounterType:(uuid,display,viewPrivilege,editPrivilege),"
		        + "encounterProviders:(uuid,display,encounterRole:(uuid,display),provider:(uuid,person:(uuid,display)))),"
		        + "visitType:(uuid,name,display),startDatetime,stopDatetime,patient,attributes:(attributeType:ref,display,uuid,value)";

		return http("Get Current Visit of Patient")
		        .get("/openmrs/ws/rest/v1/visit?patient=" + patientUuid + "&v=" + customRepresentation + "&limit=5");
	}

	public HttpRequestActionBuilder getPatientSummaryData(String patientUuid) {
		return http("Get Patient Summary Data").get("/openmrs/ws/fhir2/R4/Patient/" + patientUuid + "?_summary=data");
	}

	public HttpRequestActionBuilder getPatientObservations(String patientUuid, Set<String> observationTypes) {
		// Join the observationTypes array into a single string with "%2C" as the delimiter
		StringJoiner joiner = new StringJoiner("%2C");
		for (String code : observationTypes) {
			joiner.add(code);
		}
		String codesParam = joiner.toString();

		// Construct the URL with the dynamically joined observationTypes
		String url = String.format(
		    "/openmrs/ws/fhir2/R4/Observation?subject:Patient=%s&code=%s&_summary=data&_sort=-date&_count=100", patientUuid,
		    codesParam);

		return http("Get Patient Observations").get(url);
	}

	public HttpRequestActionBuilder getPatientConditions(String patientUuid) {
		return http("Get Patient Conditions")
		        .get("/openmrs/ws/fhir2/R4/Condition?patient=" + patientUuid + "&_count=100&_summary=data");
	}

	public HttpRequestActionBuilder getActiveOrders(String patientUuid) {
		String customRepresentation = "custom:(uuid,dosingType,orderNumber,accessionNumber,patient:ref,action,careSetting:ref,previousOrder:ref,"
		        + "dateActivated,scheduledDate,dateStopped,autoExpireDate,orderType:ref,encounter:ref,orderer:(uuid,display,"
		        + "person:(display)),orderReason,orderReasonNonCoded,orderType,urgency,instructions,commentToFulfiller,drug:"
		        + "(uuid,display,strength,dosageForm:(display,uuid),concept),dose,doseUnits:ref,frequency:ref,asNeeded,asNeededCondition,"
		        + "quantity,quantityUnits:ref,numRefills,dosingInstructions,duration,durationUnits:ref,route:ref,brandName,dispenseAsWritten)";

		return http("Get Active Orders").get("/openmrs/ws/rest/v1/order?patient=" + patientUuid + "&careSetting="
		        + CARE_SETTING_UUID + "&status=any&orderType=" + DRUG_ORDER + "&v=" + customRepresentation);
	}

	public HttpRequestActionBuilder getIsVisitsEnabled() {
		return http("Get isVisitsEnabled").get("/openmrs/ws/rest/v1/systemsetting/visits.enabled?v=custom:(value)");
	}

	public HttpRequestActionBuilder getPatientLifeStatus(String patientUuid) {
		return http("Get Patient Death Status").get(
		    "/openmrs/ws/rest/v1/person/" + patientUuid + "?v=custom:(causeOfDeath:(display),causeOfDeathNonCoded)");
	}

	public HttpRequestActionBuilder getVitalConceptSetDetails() {
		return http("Get all the constraints on the vital concepts").get("/openmrs/ws/rest/v1/concept/"
		        + VITAL_SIGNS_CONCEPT_SET
		        + "?v=custom:(setMembers:(uuid,display,hiNormal,hiAbsolute,hiCritical,lowNormal,lowAbsolute,lowCritical,units))");
	}

}
