package org.openmrs.performance.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.RawFileBodyPart;
import static io.gatling.javaapi.http.HttpDsl.StringBodyPart;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.Constants.ADMIN_SUPER_USER_UUID;
import static org.openmrs.performance.Constants.CARE_SETTING_UUID;
import static org.openmrs.performance.Constants.CLINICIAN_ENCOUNTER_ROLE;
import static org.openmrs.performance.Constants.HIV_CARE_TREATMENT;
import static org.openmrs.performance.Constants.IMMUNIZATION_CONCEPT_SET;
import static org.openmrs.performance.Constants.INPATEINT_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.Constants.OBJECTIVE_FINDINGS;
import static org.openmrs.performance.Constants.DRUG_ORDER;
import static org.openmrs.performance.Constants.OUTPATIENT_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.Constants.PLAN;
import static org.openmrs.performance.Constants.POLIO_VACCINATION;
import static org.openmrs.performance.Constants.SOAP_NOTE_TEMPLATE;
import static org.openmrs.performance.Constants.SUBJECTIVE_FINDINGS;
import static org.openmrs.performance.Constants.VISIT_NOTE_CONCEPT_UUID;
import static org.openmrs.performance.Constants.VISIT_NOTE_ENCOUNTER_TYPE_UUID;
import static org.openmrs.performance.Constants.VISIT_NOTE_FORM_UUID;
import static org.openmrs.performance.Constants.BACK_PAIN;
import static org.openmrs.performance.Constants.DIAGNOSIS_CONCEPT;
import static org.openmrs.performance.utils.CommonUtils.getAdjustedDateTimeAsString;
import static org.openmrs.performance.utils.CommonUtils.getCurrentDateTimeAsString;

public class DoctorHttpService extends HttpService {

	public HttpRequestActionBuilder getVisitWithDiagnosesAndNotes(String patientUuid) {
		return http("Get Visits With Diagnoses and Notes (new endpoint)")
		        .get("/openmrs/ws/rest/v1/emrapi/patient/" + patientUuid + "/visitWithDiagnosesAndNotes?limit=5");
	}

	public HttpRequestActionBuilder getDrugOrdersExceptCancelledAndExpired(String patientUuid) {
		String customRepresentation = "custom:(uuid,dosingType,orderNumber,accessionNumber,patient:ref,action,careSetting"
		        + ":ref,previousOrder:ref,dateActivated,scheduledDate,dateStopped,autoExpireDate,orderType:ref,"
		        + "encounter:ref,orderer:(uuid,display,person:(display)),orderReason,orderReasonNonCoded,"
		        + "orderType,urgency,instructions,commentToFulfiller,drug:(uuid,display,strength,dosageForm:(display,uuid)"
		        + ",concept),dose,doseUnits:ref,frequency:ref,asNeeded,asNeededCondition,quantity,quantityUnits:ref,"
		        + "numRefills,dosingInstructions,duration,durationUnits:ref,route:ref,brandName,dispenseAsWritten)";

		return http("Get Drug Orders except the cancelled and expired").get(
		    "/openmrs/ws/rest/v1/order" + "?patient=" + patientUuid + "&careSetting=" + CARE_SETTING_UUID
		            + "&status=any&orderType=" + DRUG_ORDER + "&excludeCanceledAndExpired=true&v=" + customRepresentation);
	}

	public HttpRequestActionBuilder getDrugOrdersExceptDiscontinuedOrders(String patientUuid) {
		String customRepresentation = "custom:(uuid,dosingType,orderNumber,accessionNumber,patient:ref,action,"
		        + "careSetting:ref,previousOrder:ref,dateActivated,scheduledDate,dateStopped,autoExpireDate,"
		        + "orderType:ref,encounter:ref,orderer:(uuid,display,person:(display)),orderReason,orderReasonNonCoded,"
		        + "orderType,urgency,instructions,commentToFulfiller,drug:(uuid,display,strength,dosageForm:(display,uuid),"
		        + "concept),dose,doseUnits:ref,frequency:ref,asNeeded,asNeededCondition,quantity,quantityUnits:ref,"
		        + "numRefills,dosingInstructions,duration,durationUnits:ref,route:ref,brandName,dispenseAsWritten)";

		return http("Get Drug Orders except the discontinued orders").get("/openmrs/ws/rest/v1/order" + "?patient="
		        + patientUuid + "&careSetting=" + CARE_SETTING_UUID + "&status=any&orderType=" + DRUG_ORDER + "&v="
		        + customRepresentation + "&excludeDiscontinueOrders=true");
	}

	public HttpRequestActionBuilder getAttachments(String patientUuid) {
		return http("Get Attachments of Patient")
		        .get("/openmrs/ws/rest/v1/attachment?patient=" + patientUuid + "&includeEncounterless=true");
	}

	public HttpRequestActionBuilder getAllowedFileExtensions() {
		return http("Get Allowed File Extensions")
		        .get("/openmrs/ws/rest/v1/systemsetting?&v=custom:(value)&q=attachments.allowedFileExtensions");
	}

	public HttpRequestActionBuilder uploadAttachment(String patientUuid) {
		return http("Upload Attachment Request").post("/openmrs/ws/rest/v1/attachment")
		        .bodyPart(StringBodyPart("fileCaption", "Test Image")).bodyPart(StringBodyPart("patient", patientUuid))
		        .bodyPart(RawFileBodyPart("file", "Sample_1MB_image.jpg").contentType("image/jpg")
		                .fileName("Sample_1MB_image.jpg"))
		        .asMultipartForm();
	}

	public HttpRequestActionBuilder getLabResults(String patientUuid) {
		return http("Get Lab Results of Patient")
		        .get("/openmrs/ws/fhir2/R4/Observation?category=laboratory&patient=" + patientUuid
		                + "&_count=100&_summary=data")
		        .check(jsonPath("$.entry[*].resource.code.coding[0].code").findAll().optional().saveAs("conceptIDs"));
	}

	public HttpRequestActionBuilder getObservationTree(String patientUuid, String observationTreeUuid) {
		return http("Get Observation Tree Details")
		        .get("/openmrs/ws/rest/v1/obstree?patient=" + patientUuid + "&concept=" + observationTreeUuid);
	}

	public HttpRequestActionBuilder getPatientImmunizations(String patientUuid) {
		return http("Get Immunizations of Patient")
		        .get("/openmrs/ws/fhir2/R4/Immunization?patient=" + patientUuid + "&_summary=data");
	}

	public HttpRequestActionBuilder submitImmunizationForm() {
		return http("Submit Patient immunization").post("/openmrs/ws/fhir2/R4/Immunization?_summary=data")
		        .body(StringBody(session -> {
			        Map<String, Object> payload = new HashMap<>();

			        payload.put("resourceType", "Immunization");
			        payload.put("status", "completed");

			        Map<String, Object> vaccineCode = new HashMap<>();
			        List<Map<String, Object>> codingList = new ArrayList<>();
			        Map<String, Object> coding = new HashMap<>();
			        coding.put("code", POLIO_VACCINATION);
			        coding.put("display", "Polio vaccination, oral");
			        codingList.add(coding);
			        vaccineCode.put("coding", codingList);
			        payload.put("vaccineCode", vaccineCode);

			        Map<String, Object> patient = new HashMap<>();
			        patient.put("type", "Patient");
			        patient.put("reference", "Patient/" + session.getString("patient_uuid"));
			        payload.put("patient", patient);

			        Map<String, Object> encounter = new HashMap<>();
			        encounter.put("type", "Encounter");
			        encounter.put("reference", "Encounter/" + session.getString("visitUuid"));
			        payload.put("encounter", encounter);

			        payload.put("occurrenceDateTime", getCurrentDateTimeAsString());
			        payload.put("expirationDate", getAdjustedDateTimeAsString(30));

			        Map<String, Object> location = new HashMap<>();
			        location.put("type", "Location");
			        location.put("reference", "Location/" + INPATEINT_CLINIC_LOCATION_UUID);
			        payload.put("location", location);

			        List<Map<String, Object>> performerList = new ArrayList<>();
			        Map<String, Object> performer = new HashMap<>();
			        Map<String, Object> actor = new HashMap<>();
			        actor.put("type", "Practitioner");
			        actor.put("reference", "Practitioner/" + ADMIN_SUPER_USER_UUID);
			        performer.put("actor", actor);
			        performerList.add(performer);
			        payload.put("performer", performerList);

			        Map<String, Object> manufacturer = new HashMap<>();
			        manufacturer.put("display", "test");
			        payload.put("manufacturer", manufacturer);

			        payload.put("lotNumber", "123");

			        List<Map<String, Object>> protocolAppliedList = new ArrayList<>();
			        Map<String, Object> protocolApplied = new HashMap<>();
			        protocolApplied.put("doseNumberPositiveInt", 1);
			        protocolApplied.put("series", null);
			        protocolAppliedList.add(protocolApplied);
			        payload.put("protocolApplied", protocolAppliedList);

			        try {
				        return new ObjectMapper().writeValueAsString(payload);
			        }
			        catch (JsonProcessingException e) {
				        throw new RuntimeException(e);
			        }
		        }));
	}

	public HttpRequestActionBuilder getPrograms() {
		return http("Get Programs")
		        .get("/openmrs/ws/rest/v1/program?v=custom:(uuid,display,allWorkflows,concept:(uuid,display))");
	}

	public HttpRequestActionBuilder searchForConditions(String searchQuery) {
		return http("Search for Condition").get("/openmrs/ws/rest/v1/concept?name=" + searchQuery
		        + "&searchType=fuzzy&class=" + DIAGNOSIS_CONCEPT + "&v=custom:(uuid,display)");
	}

	public HttpRequestActionBuilder saveCondition(String patientUuid, String currentUserUuid) {
		String recordedDate = getCurrentDateTimeAsString();
		String onSetDate = getAdjustedDateTimeAsString(-2);
		Map<String, Object> condition = new HashMap<>();
		condition.put("clinicalStatus", Map.of("coding",
		    List.of(Map.of("system", "http://terminology.hl7.org/CodeSystem/condition-clinical", "code", "active"))));
		condition.put("code", Map.of("coding", List.of(Map.of("code", BACK_PAIN, "display", "Back Pain"))));
		condition.put("abatementDateTime", null);
		condition.put("onsetDateTime", onSetDate);
		condition.put("recorder", Map.of("reference", "Practitioner/" + currentUserUuid));
		condition.put("recordedDate", recordedDate);
		condition.put("resourceType", "Condition");
		condition.put("subject", Map.of("reference", "Patient/" + patientUuid));
		try {
			return http("Save Conditions").post("/openmrs/ws/fhir2/R4/Condition?_summary=data")
			        .body(StringBody(new ObjectMapper().writeValueAsString(condition)));
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public HttpRequestActionBuilder saveVisitNote(String patientUuid, String currentUser, String value) {
		String encounterDatetime = getCurrentDateTimeAsString();

		Map<String, Object> visitNote = new HashMap<>();
		visitNote.put("form", VISIT_NOTE_FORM_UUID);
		visitNote.put("patient", patientUuid);
		visitNote.put("location", OUTPATIENT_CLINIC_LOCATION_UUID);
		visitNote.put("encounterType", VISIT_NOTE_ENCOUNTER_TYPE_UUID);
		visitNote.put("encounterDatetime", encounterDatetime);

		Map<String, Object> encounterProvider = new HashMap<>();
		encounterProvider.put("encounterRole", CLINICIAN_ENCOUNTER_ROLE);
		encounterProvider.put("provider", currentUser);

		Map<String, Object> obs = new HashMap<>();
		obs.put("concept", Map.of("uuid", VISIT_NOTE_CONCEPT_UUID));
		obs.put("value", value);

		visitNote.put("encounterProviders", List.of(encounterProvider));
		visitNote.put("obs", List.of(obs));

		try {
			String body = new ObjectMapper().writeValueAsString(visitNote); // Convert Map to JSON

			return http("Save Visit Note").post("/openmrs/ws/rest/v1/encounter").body(StringBody(body))
			        .check(jsonPath("$.uuid").saveAs("encounterUuid")); // Store encounter UUID
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException("Error converting visitNote to JSON", e);
		}
	}

	public HttpRequestActionBuilder saveDiagnosis(String patientUuid, String encounterUuid, String diagnosisUuid,
	        String certainty, int rank) {
		Map<String, Object> patientDiagnosis = new HashMap<>();
		patientDiagnosis.put("patient", patientUuid);
		patientDiagnosis.put("encounter", encounterUuid);
		patientDiagnosis.put("certainty", certainty);
		patientDiagnosis.put("rank", rank);
		patientDiagnosis.put("condition", null);

		Map<String, Object> diagnosis = new HashMap<>();
		diagnosis.put("coded", diagnosisUuid);
		patientDiagnosis.put("diagnosis", diagnosis);

		try {
			String body = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.ALWAYS)
			        .writeValueAsString(patientDiagnosis);

			return http("Save Patient Diagnosis").post("/openmrs/ws/rest/v1/patientdiagnoses").body(StringBody(body));
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException("Error converting patientDiagnosis to JSON", e);
		}
	}

	public HttpRequestActionBuilder addProgramEnrollment() {
		return http("Add new program to patient").post("/openmrs/ws/rest/v1/programenrollment").body(StringBody(session -> {
			Map<String, Object> payload = new HashMap<>();

			payload.put("program", HIV_CARE_TREATMENT);
			payload.put("patient", session.getString("patient_uuid"));
			payload.put("dateEnrolled", getAdjustedDateTimeAsString(-1));
			payload.put("dateCompleted", null);
			payload.put("location", INPATEINT_CLINIC_LOCATION_UUID);

			List<Object> states = new ArrayList<>();
			payload.put("states", states);

			try {
				return new ObjectMapper().writeValueAsString(payload);
			}
			catch (JsonProcessingException e) {
				throw new RuntimeException("Error converting states to JSON", e);
			}
		})).check(jsonPath("$.uuid").saveAs("programUuid"));
	}

	public HttpRequestActionBuilder completeProgramEnrollment(String programUuid) {
		return http("Complete the program").post("/openmrs/ws/rest/v1/programenrollment/" + programUuid)
		        .body(StringBody(session -> {
			        Map<String, Object> payload = new HashMap<>();

			        payload.put("dateEnrolled", getAdjustedDateTimeAsString(-1));
			        payload.put("dateCompleted", getCurrentDateTimeAsString());
			        payload.put("location", INPATEINT_CLINIC_LOCATION_UUID);

			        List<Object> states = new ArrayList<>();
			        payload.put("states", states);

			        try {
				        return new ObjectMapper().writeValueAsString(payload);
			        }
			        catch (JsonProcessingException e) {
				        throw new RuntimeException("Error converting states to JSON", e);
			        }
		        }));
	}

	public HttpRequestActionBuilder getPatientFormEncounters(String patientUuid) {
		String customRepresentation = "custom:(uuid,encounterDatetime,encounterType:(uuid,name,viewPrivilege,editPrivilege),"
		        + "form:(uuid,name,display,encounterType:(uuid,name,viewPrivilege,editPrivilege),version,published,retired,"
		        + "resources:(uuid,name,dataType,valueReference)))";
		return http("Get All Form Encounters")
		        .get("/openmrs/ws/rest/v1/encounter?v=" + customRepresentation + "&patient=" + patientUuid);
	}

	public HttpRequestActionBuilder getAllClinicalForms() {
		String customRepresentation = "custom:(uuid,name,display,encounterType:(uuid,name,viewPrivilege,editPrivilege),"
		        + "version,published,retired,resources:(uuid,name,dataType,valueReference))";
		return http("Get All Forms").get("/openmrs/ws/rest/v1/form?v=" + customRepresentation);
	}

	public HttpRequestActionBuilder getSpecificClinicalForm(String formUuid) {
		return http("Get Clinical Form by UUID").get("/openmrs/ws/rest/v1/o3/forms/" + formUuid)
		        .check(jsonPath("$.conceptReferences.*.uuid").findAll().optional().saveAs("clinicalFormUuid"));
	}

	public HttpRequestActionBuilder getEncounterByUuid(String encounterUuid) {
		String customRepresentation = "custom:(uuid,encounterDatetime,encounterType:(uuid,name,description),"
		        + "location:(uuid,name),patient:(uuid,display),encounterProviders:(uuid,provider:(uuid,name),"
		        + "encounterRole:(uuid,name)),orders:(uuid,display,concept:(uuid,display),voided),"
		        + "diagnoses:(uuid,certainty,condition,formFieldPath,formFieldNamespace,display,rank,voided,"
		        + "diagnosis:(coded:(uuid,display))),obs:(uuid,obsDatetime,comment,voided,groupMembers,"
		        + "formFieldNamespace,formFieldPath,concept:(uuid,name:(uuid,name)),value:(uuid,"
		        + "name:(uuid,name),names:(uuid,conceptNameType,name))))";

		return http("Get Encounter By UUID")
		        .get("/openmrs/ws/rest/v1/encounter/" + encounterUuid + "?v=" + customRepresentation);
	}

	public HttpRequestActionBuilder getEncounterRoles() {
		return http("Get Encounter Roles").get("/openmrs/ws/rest/v1/encounterrole?v=custom:(uuid,display,name)");
	}

	public HttpRequestActionBuilder getLatestVisitNoteEncounter(String patientUuid) {
		return http("Get Latest FHIR Encounter")
		        .get("/openmrs/ws/fhir2/R4/Encounter?patient=" + patientUuid + "&_sort=-date&_count=1&type="
		                + VISIT_NOTE_ENCOUNTER_TYPE_UUID + "&_summary=data")
		        .check(jsonPath("$.entry[0].resource.id").saveAs("clinicalEncounterUuid"));
	}

	public HttpRequestActionBuilder getConcepts(String references) {
		String customRepresentation = "custom:(uuid,display,conceptClass:(uuid,display),answers:(uuid,display),"
		        + "conceptMappings:(conceptReferenceTerm:(conceptSource:(name),code)))";
		return http("Get Concepts by References")
		        .get("/openmrs/ws/rest/v1/concept?references=" + references + "&v=" + customRepresentation + "&limit=100");
	}

	public HttpRequestActionBuilder saveSoapTemplateClinicalForm() {
		String customRepresentation = "custom:(uuid,encounterDatetime,encounterType:(uuid,name,description),location:"
		        + "(uuid,name),patient:(uuid,display),encounterProviders:(uuid,provider:(uuid,name),encounterRole:(uuid,name)),"
		        + "orders:(uuid,display,concept:(uuid,display),voided),diagnoses:(uuid,certainty,condition,formFieldPath,"
		        + "formFieldNamespace,display,rank,voided,diagnosis:(coded:(uuid,display))),obs:(uuid,obsDatetime,comment,"
		        + "voided,groupMembers,formFieldNamespace,formFieldPath,concept:(uuid,name:(uuid,name)),value:(uuid,name:"
		        + "(uuid,name),names:(uuid,conceptNameType,name))))";

		return http("Save a clinical form").post("/openmrs/ws/rest/v1/encounter?v=" + customRepresentation)
		        .body(StringBody(session -> {
			        Map<String, Object> payload = new HashMap<>();

			        payload.put("patient", session.get("patient_uuid"));
			        payload.put("encounterDatetime", getCurrentDateTimeAsString());
			        payload.put("location", INPATEINT_CLINIC_LOCATION_UUID);
			        payload.put("encounterType", VISIT_NOTE_ENCOUNTER_TYPE_UUID);

			        List<Map<String, Object>> encounterProviders = new ArrayList<>();
			        Map<String, Object> providerEntry = new HashMap<>();
			        providerEntry.put("provider", ADMIN_SUPER_USER_UUID);
			        providerEntry.put("encounterRole", CLINICIAN_ENCOUNTER_ROLE);
			        encounterProviders.add(providerEntry);
			        payload.put("encounterProviders", encounterProviders);

			        List<Map<String, Object>> obsList = new ArrayList<>();

			        Map<String, Object> obs1 = new HashMap<>();
			        obs1.put("value", "1");
			        obs1.put("concept", SUBJECTIVE_FINDINGS);
			        obs1.put("formFieldNamespace", "rfe-forms");
			        obs1.put("formFieldPath", "rfe-forms-SOAPSubjectiveFindings");
			        obsList.add(obs1);

			        Map<String, Object> obs2 = new HashMap<>();
			        obs2.put("value", "2");
			        obs2.put("concept", OBJECTIVE_FINDINGS);
			        obs2.put("formFieldNamespace", "rfe-forms");
			        obs2.put("formFieldPath", "rfe-forms-SOAPObjectiveFindings");
			        obsList.add(obs2);

			        Map<String, Object> obs3 = new HashMap<>();
			        obs3.put("value", "3");
			        obs3.put("concept", PLAN);
			        obs3.put("formFieldNamespace", "rfe-forms");
			        obs3.put("formFieldPath", "rfe-forms-SOAPPlan");
			        obsList.add(obs3);

			        payload.put("obs", obsList);

			        Map<String, Object> form = new HashMap<>();
			        form.put("uuid", SOAP_NOTE_TEMPLATE);
			        payload.put("form", form);

			        payload.put("orders", new ArrayList<>());

			        payload.put("diagnoses", new ArrayList<>());

			        try {
				        return new ObjectMapper().writeValueAsString(payload);
			        }
			        catch (JsonProcessingException e) {
				        throw new RuntimeException(e);
			        }
		        }));
	}

	public HttpRequestActionBuilder getAllImmunizations() {
		String customRepresentation = "custom:(uuid,display,answers:(uuid,display),conceptMappings:(conceptReferenceTerm:"
		        + "(conceptSource:(name),code)))";

		return http("Get all immunizations")
		        .get("/openmrs/ws/rest/v1/concept?references=" + IMMUNIZATION_CONCEPT_SET + "&v=" + customRepresentation);
	}
}
