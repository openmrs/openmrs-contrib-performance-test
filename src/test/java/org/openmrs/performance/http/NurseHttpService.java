package org.openmrs.performance.http;

import io.gatling.javaapi.http.HttpRequestActionBuilder;

import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.Constants.INPATEINT_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.Constants.TEST_ORDER_TYPE_UUID;
import static org.openmrs.performance.utils.CommonUtils.getCurrentDateTimeAsString;

public class NurseHttpService extends HttpService {

	public HttpRequestActionBuilder getOrdersWithNullFulfillerStatusAndActivatedDate(String patientUuid) {
		return http("Get Orders of admitted patient by Activated Date")
		        .get("/openmrs/ws/rest/v1/order" + "?includeNullFulfillerStatus=true" + "&patient=" + patientUuid
		                + "&orderTypes=" + TEST_ORDER_TYPE_UUID + "&activatedOnOrAfterDate=" + getCurrentDateTimeAsString());
	}

	public HttpRequestActionBuilder getInpatientRequest() {
		String customRepresentation = "custom:(dispositionLocation,dispositionType,disposition,dispositionEncounter:full,"
		        + "patient:(uuid,identifiers,voided,person:(uuid,display,gender,age,birthdate,birthtime,preferredName,"
		        + "preferredAddress,dead,deathDate)),dispositionObsGroup,visit)";

		return http("Get Inpatient Request")
		        .get("/openmrs/ws/rest/v1/emrapi/inpatient/request" + "?dispositionType=ADMIT,TRANSFER"
		                + "&dispositionLocation=" + INPATEINT_CLINIC_LOCATION_UUID + "&v=" + customRepresentation);
	}

	public HttpRequestActionBuilder getAdmittedPatientInfo() {
		String customRepresentation = "custom:(visit,patient:(uuid,identifiers:(uuid,display,identifier,identifierType),voided,"
		        + "person:(uuid,display,gender,age,birthdate,birthtime,preferredName,preferredAddress,dead,deathDate)),"
		        + "encounterAssigningToCurrentInpatientLocation:(encounterDatetime),"
		        + "currentInpatientRequest:(dispositionLocation,dispositionType,disposition:(uuid,display),"
		        + "dispositionEncounter:(uuid,display),dispositionObsGroup:(uuid,display),visit:(uuid),patient:(uuid)),"
		        + "firstAdmissionOrTransferEncounter:(encounterDatetime),currentInpatientLocation)";

		return http("Get Admission Info")
		        .get("/openmrs/ws/rest/v1/emrapi/inpatient/admission" + "?currentInpatientLocation="
		                + INPATEINT_CLINIC_LOCATION_UUID + "&v=" + customRepresentation)
		        .check(jsonPath("$.results[*].visit.patient.uuid").findAll().optional().saveAs("admittedPatientUuid"));
	}

	public HttpRequestActionBuilder getAdmissionLocationInfo() {
		String customRepresentation = "custom:(ward,totalBeds,occupiedBeds,"
		        + "bedLayouts:(rowNumber,columnNumber,bedNumber,bedId,bedUuid,status,location,"
		        + "patients:(person:full,identifiers,uuid)))";

		return http("Get Admission Location Info").get(
		    "/openmrs/ws/rest/v1/admissionLocation/" + INPATEINT_CLINIC_LOCATION_UUID + "?v=" + customRepresentation);
	}


}