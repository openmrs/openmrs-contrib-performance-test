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
import java.util.Set;
import java.util.StringJoiner;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.Constants.ALLERGY_REACTION_UUID;
import static org.openmrs.performance.Constants.ARTERIAL_BLOOD_OXYGEN_SATURATION;
import static org.openmrs.performance.Constants.BED_ASSIGNMENT_UUID;
import static org.openmrs.performance.Constants.CLINICIAN_ENCOUNTER_ROLE;
import static org.openmrs.performance.Constants.DIASTOLIC_BLOOD_PRESSURE;
import static org.openmrs.performance.Constants.HEIGHT_CM;
import static org.openmrs.performance.Constants.MID_UPPER_ARM_CIRCUMFERENCE;
import static org.openmrs.performance.Constants.OTHER_NON_CODED_ALLERGEN_UUID;
import static org.openmrs.performance.Constants.PULSE;
import static org.openmrs.performance.Constants.RESPIRATORY_RATE;
import static org.openmrs.performance.Constants.SEVERITY_UUID;
import static org.openmrs.performance.Constants.SYSTOLIC_BLOOD_PRESSURE;
import static org.openmrs.performance.Constants.TEMPERATURE_C;
import static org.openmrs.performance.Constants.TEST_ORDER_TYPE_UUID;
import static org.openmrs.performance.Constants.VITALS_ENCOUNTER_TYPE_UUID;
import static org.openmrs.performance.Constants.VITALS_FORM_UUID;
import static org.openmrs.performance.Constants.VITALS_LOCATION_UUID;
import static org.openmrs.performance.Constants.WARD_ADMISSION_NOTE_UUID;
import static org.openmrs.performance.Constants.WEIGHT_KG;
import static org.openmrs.performance.utils.CommonUtils.getCurrentDateTimeAsString;

public class NurseHttpService extends HttpService {

	public HttpRequestActionBuilder getOrdersWithNullFulfillerStatusAndActivatedDate(String patientUuid) {
		String startDate = getCurrentDateTimeAsString();
		String encoded = URLEncoder.encode(startDate, StandardCharsets.UTF_8);
		return http("Get Orders of admitted patient by Activated Date")
		        .get("/openmrs/ws/rest/v1/order" + "?includeNullFulfillerStatus=true" + "&patient=" + patientUuid
		                + "&orderTypes=" + TEST_ORDER_TYPE_UUID + "&activatedOnOrAfterDate=" + encoded);
	}

	public HttpRequestActionBuilder getInpatientRequest(String locationUuid) {
		String customRepresentation = "custom:(dispositionLocation,dispositionType,disposition,dispositionEncounter:full,"
		        + "patient:(uuid,identifiers,voided,person:(uuid,display,gender,age,birthdate,birthtime,preferredName,"
		        + "preferredAddress,dead,deathDate)),dispositionObsGroup,visit)";

		return http("Get Inpatient Request")
		        .get("/openmrs/ws/rest/v1/emrapi/inpatient/request" + "?dispositionType=ADMIT,TRANSFER"
		                + "&dispositionLocation=" + locationUuid + "&v=" + customRepresentation)
		        .check(jsonPath("$.results[*].dispositionEncounter.patient.uuid").findAll().optional()
		                .saveAs("transferPatientUuid"));
	}

	public HttpRequestActionBuilder getAdmittedPatientInfo(String locationUuid) {
		String customRepresentation = "custom:(visit,patient:(uuid,identifiers:(uuid,display,identifier,identifierType),voided,"
		        + "person:(uuid,display,gender,age,birthdate,birthtime,preferredName,preferredAddress,dead,deathDate)),"
		        + "encounterAssigningToCurrentInpatientLocation:(encounterDatetime),"
		        + "currentInpatientRequest:(dispositionLocation,dispositionType,disposition:(uuid,display),"
		        + "dispositionEncounter:(uuid,display),dispositionObsGroup:(uuid,display),visit:(uuid),patient:(uuid)),"
		        + "firstAdmissionOrTransferEncounter:(encounterDatetime),currentInpatientLocation)";

		return http("Get Admission Info")
		        .get("/openmrs/ws/rest/v1/emrapi/inpatient/admission" + "?currentInpatientLocation=" + locationUuid + "&v="
		                + customRepresentation)
		        .check(jsonPath("$.results[*].visit.patient.uuid").findAll().optional().saveAs("admittedPatientUuid"));
	}

	public HttpRequestActionBuilder getAdmissionLocationInfo(String locationUuid) {
		String customRepresentation = "custom:(ward,totalBeds,occupiedBeds,"
		        + "bedLayouts:(rowNumber,columnNumber,bedNumber,bedId,bedUuid,status,location,"
		        + "patients:(person:full,identifiers,uuid)))";

		return http("Get Admission Location Info")
		        .get("/openmrs/ws/rest/v1/admissionLocation/" + locationUuid + "?v=" + customRepresentation);
	}

	public HttpRequestActionBuilder getBedsByPatientUuid(String patientUuid) {
		return http("Get Beds for Patient").get("/openmrs/ws/rest/v1/beds?patientUuid=" + patientUuid);
	}

	public HttpRequestActionBuilder saveWardEncounter(String wardEncounterType, List<Map<String, Object>> obs,
	        String wardEncounterTypeUuid, String locationUuid) {
		return http("Save Ward Encounter").post("/openmrs/ws/rest/v1/encounter").body(StringBody(session -> {
			Map<String, Object> payload = new HashMap<>();

			Map<String, Object> encounterType = new HashMap<>();
			encounterType.put("uuid", wardEncounterTypeUuid);
			encounterType.put("display", wardEncounterType);

			Map<String, String> provider = new HashMap<>();
			provider.put("provider", session.getString("currentUserUuid"));
			provider.put("encounterRole", CLINICIAN_ENCOUNTER_ROLE);

			Map<String, Object> link = new HashMap<>();
			link.put("rel", "self");
			link.put("uri", "https://dev3.openmrs.org/openmrs/ws/rest/v1/encountertype/" + wardEncounterTypeUuid);
			link.put("resourceAlias", "encountertype");

			encounterType.put("links", List.of(link));

			payload.put("patient", session.getString("patient_uuid"));
			payload.put("encounterType", encounterType);
			payload.put("location", locationUuid);
			payload.put("encounterProviders", List.of(provider));
			payload.put("obs", obs);
			payload.put("visit", session.getString("visitUuid"));
			try {
				return new ObjectMapper().writeValueAsString(payload);
			}
			catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}));
	}

	public HttpRequestActionBuilder getVitalsConceptRefRanges(String patientUuid, Set<String> observationTypes) {
		StringJoiner joiner = new StringJoiner("%2C");
		for (String code : observationTypes) {
			joiner.add(code);
		}
		String codesParam = joiner.toString();

		String url = "/openmrs/ws/rest/v1/conceptreferencerange/?patient=" + patientUuid + "&concept=" + codesParam
		        + "&v=full";

		return http("Get Vitals concept reference ranges").get(url);
	}

	public HttpRequestActionBuilder getCustomTransferLocationsConfiguration() {
		String customRepresentation = "custom:metadataSourceName:ref,orderingProviderEncounterRole:ref,supportsTransferLocationTag:"
		        + "(uuid,display,name,links),unknownLocation:ref,denyAdmissionConcept:ref,admissionForm:ref,"
		        + "exitFromInpatientEncounterType:ref,extraPatientIdentifierTypes:ref,consultFreeTextCommentsConcept:ref,"
		        + "sameAsConceptMapType:ref,testPatientPersonAttributeType:ref,admissionDecisionConcept:ref,"
		        + "supportsAdmissionLocationTag:(uuid,display,name,links),checkInEncounterType:ref,transferWithinHospitalEncounterType:"
		        + "ref,suppressedDiagnosisConcepts:ref,primaryIdentifierType:ref,nonDiagnosisConceptSets:ref,"
		        + "fullPrivilegeLevel:ref,unknownProvider:ref,diagnosisSets:ref,personImageDirectory:ref,visitNoteEncounterType:ref,"
		        + "inpatientNoteEncounterType:ref,transferRequestEncounterType:ref,consultEncounterType:ref,diagnosisMetadata:ref,"
		        + "narrowerThanConceptMapType:ref,clinicianEncounterRole:ref,conceptSourcesForDiagnosisSearch:ref,patientDiedConcept:ref"
		        + ",emrApiConceptSource:ref,lastViewedPatientSizeLimit:ref,identifierTypesToSearch:ref,telephoneAttributeType:ref,"
		        + "checkInClerkEncounterRole:ref,dischargeForm:ref,unknownCauseOfDeathConcept:ref,"
		        + "visitAssignmentHandlerAdjustEncounterTimeOfDayIfNecessary:ref,atFacilityVisitType:ref,visitExpireHours:ref,"
		        + "admissionEncounterType:ref,motherChildRelationshipType:ref,dispositions:ref,dispositionDescriptor:ref,"
		        + "highPrivilegeLevel:ref,supportsLoginLocationTag:(uuid,display,name,links),unknownPatientPersonAttributeType:ref,"
		        + "supportsVisitsLocationTag:(uuid,display,name,links),transferForm:ref,bedAssignmentEncounterType:ref,"
		        + "cancelADTRequestEncounterType:ref,admissionDecisionConcept:ref,denyAdmissionConcept:ref";

		return http("Get Transfer Locations configuration data")
		        .get("/openmrs/ws/rest/v1/emrapi/configuration?v=" + customRepresentation);
	}

	public HttpRequestActionBuilder getTransferableLocations() {
		return http("Get Transferable locations").get("/openmrs/ws/fhir2/R4/Location?_tag=Transfer+Location&partof:below="
		        + BED_ASSIGNMENT_UUID + "&_count=15&_getpagesoffset=0");
	}

	public HttpRequestActionBuilder getAllLocationsSearchSet() {
		return http("Get Locations Search Set").get("/openmrs/ws/fhir2/R4/Location?_count=1&_summary=data");
	}

	public HttpRequestActionBuilder changeTheSessionLocation(String locationUuid) {
		String payload = String.format("{\"sessionLocation\":\"%s\"}", locationUuid);
		return http("Change the session location").post("/openmrs/ws/rest/v1/session").body(StringBody(payload));
	}

	public HttpRequestActionBuilder getPatientAdmissionNote(String patientUuid) {
		String customRepresentation = "custom:(uuid,display,obsDatetime,value,concept:(uuid,display),"
		        + "encounter:(uuid,display,encounterType,encounterDatetime,visit:(uuid,display)))";

		return http("Get patient admission note").get("/openmrs/ws/rest/v1/obs?patient=" + patientUuid + "&concept="
		        + WARD_ADMISSION_NOTE_UUID + "&v=" + customRepresentation);
	}

	public HttpRequestActionBuilder getAllergies(String patientUuid) {
		return http("Get Allergies of Patient")
		        .get("/openmrs/ws/fhir2/R4/AllergyIntolerance?patient=" + patientUuid + "&_summary=data");
	}

	public HttpRequestActionBuilder getAllergens(String allergenType, String allergenUuid) {
		return http("Get " + allergenType + " Allergens").get("/openmrs/ws/rest/v1/concept/" + allergenUuid + "?v=full");
	}

	public HttpRequestActionBuilder saveAllergy(String patientUuid) {
		// Using the 'OTHER' allergen type to create a unique entry and avoid duplication
		return http("Save an Allergy").post("/openmrs/ws/rest/v1/patient/" + patientUuid + "/allergy")
		        .body(StringBody(session -> {
			        try {
				        String random = new Random().ints(7, 'a', 'z' + 1)
				                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
				        Map<String, Object> payload = new HashMap<>();
				        Map<String, String> codedAllergen = new HashMap<>();

				        codedAllergen.put("uuid", OTHER_NON_CODED_ALLERGEN_UUID);
				        Map<String, Object> allergen = new HashMap<>();

				        allergen.put("allergenType", "OTHER");
				        allergen.put("codedAllergen", codedAllergen);
				        allergen.put("nonCodedAllergen", random);

				        Map<String, String> severity = new HashMap<>();
				        severity.put("uuid", SEVERITY_UUID);

				        Map<String, String> reactionUuid = new HashMap<>();
				        reactionUuid.put("uuid", ALLERGY_REACTION_UUID);

				        Map<String, Object> reaction = new HashMap<>();
				        reaction.put("reaction", reactionUuid);

				        List<Map<String, Object>> reactions = Collections.singletonList(reaction);
				        payload.put("allergen", allergen);
				        payload.put("severity", severity);
				        payload.put("comment", "test");
				        payload.put("reactions", reactions);
				        return new ObjectMapper().writeValueAsString(payload);
			        }
			        catch (JsonProcessingException e) {
				        throw new RuntimeException(e);
			        }
		        }));
	}

	public HttpRequestActionBuilder saveVitalsData(String patientUuid) {
		String encounterDatetime = CommonUtils.getCurrentDateTimeAsString();

		Map<String, Object> encounter = new HashMap<>();
		encounter.put("form", VITALS_FORM_UUID);
		encounter.put("patient", patientUuid);
		encounter.put("location", VITALS_LOCATION_UUID);
		encounter.put("encounterType", VITALS_ENCOUNTER_TYPE_UUID);
		encounter.put("encounterDatetime", encounterDatetime);

		List<Map<String, Object>> observations = new ArrayList<>();
		observations.add(Map.of("concept", SYSTOLIC_BLOOD_PRESSURE, "value", 34));
		observations.add(Map.of("concept", DIASTOLIC_BLOOD_PRESSURE, "value", 44));
		observations.add(Map.of("concept", RESPIRATORY_RATE, "value", 20));
		observations.add(Map.of("concept", ARTERIAL_BLOOD_OXYGEN_SATURATION, "value", 20));
		observations.add(Map.of("concept", PULSE, "value", 120));
		observations.add(Map.of("concept", TEMPERATURE_C, "value", 28));
		observations.add(Map.of("concept", WEIGHT_KG, "value", 60));
		observations.add(Map.of("concept", HEIGHT_CM, "value", 121));
		observations.add(Map.of("concept", MID_UPPER_ARM_CIRCUMFERENCE, "value", 34));

		encounter.put("obs", observations);

		try {
			String body = new ObjectMapper().writeValueAsString(encounter); // Convert Map to JSON
			return http("Save Vitals").post("/openmrs/ws/rest/v1/encounter").body(StringBody(body));
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException("Error converting visitNote to JSON", e);
		}
	}

}
