package org.openmrs.performance.http.get;

import io.gatling.javaapi.http.HttpRequestActionBuilder;
import org.openmrs.performance.utils.CommonUtils;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.bodyString;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.Constants.*;

public class PatientVisitHttpService {

	public static HttpRequestActionBuilder getProgramEnrollments(String patientUuid) {
		String customRepresentation = "custom:(uuid,display,program,dateEnrolled,dateCompleted,"
		        + "location:(uuid,display))";

		return http("Get Program Enrollments of Patient")
		        .get("/openmrs/ws/rest/v1/programenrollment?patient=" + patientUuid + "&v=" + customRepresentation);
	}

	public static HttpRequestActionBuilder getVisitTypes() {
		return http("Get Visit Types").get("/openmrs/ws/rest/v1/visittype");
	}

	public static HttpRequestActionBuilder getVisitsOfPatient(String patientUuid) {
		String customRepresentation = "custom:(uuid,location,encounters:(uuid,diagnoses:(uuid,display,rank,diagnosis,voided),"
		        + "form:(uuid,display),encounterDatetime,orders:full,obs:(uuid,concept:(uuid,display,conceptClass:(uuid,display)),"
		        + "display,groupMembers:(uuid,concept:(uuid,display),value:(uuid,display),display),value,obsDatetime),"
		        + "encounterType:(uuid,display,viewPrivilege,editPrivilege),encounterProviders:(uuid,display,encounterRole:(uuid,display),"
		        + "provider:(uuid,person:(uuid,display)))),visitType:(uuid,name,display),startDatetime,stopDatetime,patient,"
		        + "attributes:(attributeType:ref,display,uuid,value)";

		return http("Get Visits of Patient")
		        .get("/openmrs/ws/rest/v1/visit?patient=" + patientUuid + "&v=" + customRepresentation + "&limit=5");
	}

	public static HttpRequestActionBuilder getActiveVisitOfPatient(String patientUuid) {
		String customRepresentation = "custom:(uuid,display,voided,indication,startDatetime,stopDatetime,"
		        + "encounters:(uuid,display,encounterDatetime," + "form:(uuid,name),location:ref," + "encounterType:ref,"
		        + "encounterProviders:(uuid,display," + "provider:(uuid,display)))," + "patient:(uuid,display),"
		        + "visitType:(uuid,name,display),"
		        + "attributes:(uuid,display,attributeType:(name,datatypeClassname,uuid),value),"
		        + "location:(uuid,name,display))";

		return http("Get Active Visits of Patient").get(
		    "/openmrs/ws/rest/v1/visit?patient=" + patientUuid + "&v=" + customRepresentation + "&includeInactive=false");
	}

	public static HttpRequestActionBuilder getVisitWithDiagnosesAndNotes(String patientUuid) {
		return http("Get Visits With Diagnoses and Notes (new endpoint)")
		        .get("/openmrs/ws/rest/v1/emrapi/patient/" + patientUuid + "/visitWithDiagnosesAndNotes?limit=5");
	}

	public static HttpRequestActionBuilder getOrderTypes() {
		return http("Get Order Types").get("/openmrs/ws/rest/v1/ordertype");
	}

	public static HttpRequestActionBuilder getAllActiveOrders(String patientUuid) {
		return http("Get Active Orders").get(
		    "/openmrs/ws/rest/v1/order?patient=" + patientUuid + "&careSetting=" + CARE_SETTING_UUID + "&status=ACTIVE");
	}

	public static HttpRequestActionBuilder getDrugOrdersExceptCancelledAndExpired(String patientUuid) {
		String customRepresentation = """
		        custom:(uuid,dosingType,orderNumber,accessionNumber,patient:ref,action,careSetting:ref,
		        previousOrder:ref,dateActivated,scheduledDate,dateStopped,autoExpireDate,orderType:ref,
		        encounter:ref,orderer:(uuid,display,person:(display)),orderReason,orderReasonNonCoded,
		        orderType,urgency,instructions,commentToFulfiller,drug:(uuid,display,strength,dosageForm:(display,uuid),concept),
		        dose,doseUnits:ref,frequency:ref,asNeeded,asNeededCondition,quantity,quantityUnits:ref,
		        numRefills,dosingInstructions,duration,durationUnits:ref,route:ref,brandName,dispenseAsWritten)
		        """;
		return http("Get Drug Orders except the cancelled and expired").get(
		    "/openmrs/ws/rest/v1/order" + "?patient=" + patientUuid + "&careSetting=" + CARE_SETTING_UUID
		            + "&status=any&orderType=" + DRUG_ORDER + "&excludeCanceledAndExpired=true&v=" + customRepresentation);
	}

	public static HttpRequestActionBuilder getDrugOrdersExceptDiscontinuedOrders(String patientUuid) {
		String customRepresentation = """
		        custom:(uuid,dosingType,orderNumber,accessionNumber,patient:ref,action,careSetting:ref,
		        previousOrder:ref,dateActivated,scheduledDate,dateStopped,autoExpireDate,orderType:ref,
		        encounter:ref,orderer:(uuid,display,person:(display)),orderReason,orderReasonNonCoded,
		        orderType,urgency,instructions,commentToFulfiller,drug:(uuid,display,strength,dosageForm:(display,uuid),concept),
		        dose,doseUnits:ref,frequency:ref,asNeeded,asNeededCondition,quantity,quantityUnits:ref,
		        numRefills,dosingInstructions,duration,durationUnits:ref,route:ref,brandName,dispenseAsWritten)
		        """;
		return http("Get Drug Orders except the discontinued orders").get("/openmrs/ws/rest/v1/order" + "?patient="
		        + patientUuid + "&careSetting=" + CARE_SETTING_UUID + "&status=any&orderType=" + DRUG_ORDER + "&v="
		        + customRepresentation + "&excludeDiscontinueOrders=true");
	}

	public static HttpRequestActionBuilder getActiveOrders(String patientUuid) {
		String customRepresentation = """
		        custom:(uuid,dosingType,orderNumber,accessionNumber,patient:ref,action,careSetting:ref,previousOrder:ref,
		        dateActivated,scheduledDate,dateStopped,autoExpireDate,orderType:ref,encounter:ref,orderer:(uuid,display,
		        person:(display)),orderReason,orderReasonNonCoded,orderType,urgency,instructions,commentToFulfiller,drug:
		        (uuid,display,strength,dosageForm:(display,uuid),concept),dose,doseUnits:ref,frequency:ref,asNeeded,asNeededCondition,
		        quantity,quantityUnits:ref,numRefills,dosingInstructions,duration,durationUnits:ref,route:ref,brandName,dispenseAsWritten)
		        """;
		return http("Get Active Orders").get("/openmrs/ws/rest/v1/order?patient=" + patientUuid + "&careSetting="
		        + CARE_SETTING_UUID + "&status=any&orderType=" + DRUG_ORDER + "&v=" + customRepresentation);
	}

	public static HttpRequestActionBuilder getAllergies(String patientUuid) {
		return http("Get Allergies of Patient")
		        .get("/openmrs/ws/fhir2/R4/AllergyIntolerance?patient=" + patientUuid + "&_summary=data");
	}

	public static HttpRequestActionBuilder getAllergens(String allergenType, String allergenUuid) {
		return http("Get " + allergenType + " Allergens").get("/openmrs/ws/rest/v1/concept/" + allergenUuid + "?v=full");
	}

	public static HttpRequestActionBuilder getConditions(String patientUuid) {
		return http("Get Conditions of Patient")
		        .get("/openmrs/ws/fhir2/R4/Condition?patient=" + patientUuid + "&_count=100&_summary=data");
	}

	public static HttpRequestActionBuilder getAttachments(String patientUuid) {
		return http("Get Attachments of Patient")
		        .get("/openmrs/ws/rest/v1/attachment?patient=" + patientUuid + "&includeEncounterless=true");
	}

	public static HttpRequestActionBuilder getAllowedFileExtensions() {
		return http("Get Allowed File Extensions")
		        .get("/openmrs/ws/rest/v1/systemsetting?&v=custom:(value)&q=attachments.allowedFileExtensions");
	}

	public static HttpRequestActionBuilder getLabResults(String patientUuid) {
		return http("Get Lab Results of Patient").get(
		    "/openmrs/ws/fhir2/R4/Observation?category=laboratory&patient=" + patientUuid + "&_count=100&_summary=data")
		        .check(bodyString().saveAs("labResultsResponse"));
	}

	public static HttpRequestActionBuilder getConcept(String conceptUuid) {
		return http("Get Concept").get("/openmrs/ws/rest/v1/concept/" + conceptUuid + "?v=full");
	}

	public static HttpRequestActionBuilder getImmunizations(String patientUuid) {
		return http("Get Immunizations of Patient")
		        .get("/openmrs/ws/fhir2/R4/Immunization?patient=" + patientUuid + "&_summary=data");
	}

	public static HttpRequestActionBuilder getPrograms() {
		return http("Get Programs")
		        .get("/openmrs/ws/rest/v1/program?v=custom:(uuid,display,allWorkflows,concept:(uuid,display))");
	}

	public static HttpRequestActionBuilder searchForConditions(String searchQuery) {
		return http("Search for Condition").get("/openmrs/ws/rest/v1/concept?name=" + searchQuery
		        + "&searchType=fuzzy&class=" + DIAGNOSIS_CONCEPT + "&v=custom:(uuid,display)");
	}

	public static HttpRequestActionBuilder searchForDrug(String searchQuery) {
		String customRepresentation = """
		        custom:(uuid,display,name,strength,dosageForm:(display,uuid),concept:(display,uuid))
		        """;
		return http("Search for Drug").get("/openmrs/ws/rest/v1/drug?name=" + searchQuery + "&v=" + customRepresentation);
	}

	public static HttpRequestActionBuilder getVisitQueueEntry(String patientUuid) {
		return http("Get Visit Queue Entry").get("/openmrs/ws/rest/v1/visit-queue-entry??v=full&patient=" + patientUuid);
	}

	public static HttpRequestActionBuilder getCurrentVisit(String patientUuid) {
		String customRepresentation = """
		        custom:(uuid,encounters:(uuid,diagnoses:(uuid,display,rank,diagnosis),form:(uuid,display),
		        encounterDatetime,orders:full,obs:full,encounterType:(uuid,display,viewPrivilege,editPrivilege),
		        encounterProviders:(uuid,display,encounterRole:(uuid,display),provider:(uuid,person:(uuid,display)))),
		        visitType:(uuid,name,display),startDatetime,stopDatetime,patient,attributes:(attributeType:ref,display,uuid,value)&limit=5
		        """;
		return http("Get Patient's current visit")
		        .get("/openmrs/ws/rest/v1/visit?patient=" + patientUuid + "&v=" + customRepresentation);
	}
}
