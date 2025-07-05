package org.openmrs.performance.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.http.HttpRequestActionBuilder;
import org.openmrs.performance.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.RawFileBodyPart;
import static io.gatling.javaapi.http.HttpDsl.StringBodyPart;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.Constants.ADMIN_SUPER_USER_UUID;
import static org.openmrs.performance.Constants.ALLERGY_REACTION_UUID;
import static org.openmrs.performance.Constants.ARTERIAL_BLOOD_OXYGEN_SATURATION;
import static org.openmrs.performance.Constants.ASPRIN_CONCEPT_UUID;
import static org.openmrs.performance.Constants.ASPRIN_DRUG_UUID;
import static org.openmrs.performance.Constants.CARE_SETTING_UUID;
import static org.openmrs.performance.Constants.CLINICIAN_ENCOUNTER_ROLE;
import static org.openmrs.performance.Constants.HIV_CARE_TREATMENT;
import static org.openmrs.performance.Constants.INPATEINT_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.Constants.OBJECTIVE_FINDINGS;
import static org.openmrs.performance.Constants.OTHER_NON_CODED_ALLERGEN_UUID;
import static org.openmrs.performance.Constants.DAYS;
import static org.openmrs.performance.Constants.DEFAULT_DOSING_TYPE;
import static org.openmrs.performance.Constants.DIASTOLIC_BLOOD_PRESSURE;
import static org.openmrs.performance.Constants.DRUG_ORDER;
import static org.openmrs.performance.Constants.HEIGHT_CM;
import static org.openmrs.performance.Constants.MID_UPPER_ARM_CIRCUMFERENCE;
import static org.openmrs.performance.Constants.ONCE_DAILY;
import static org.openmrs.performance.Constants.ORAL;
import static org.openmrs.performance.Constants.ORDER;
import static org.openmrs.performance.Constants.OUTPATIENT_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.Constants.PATIENT_IDENTIFIER_UUID;
import static org.openmrs.performance.Constants.PLAN;
import static org.openmrs.performance.Constants.PULSE;
import static org.openmrs.performance.Constants.RESPIRATORY_RATE;
import static org.openmrs.performance.Constants.SEVERITY_UUID;
import static org.openmrs.performance.Constants.SOAP_NOTE_TEMPLATE;
import static org.openmrs.performance.Constants.SUBJECTIVE_FINDINGS;
import static org.openmrs.performance.Constants.SYSTOLIC_BLOOD_PRESSURE;
import static org.openmrs.performance.Constants.TABLET;
import static org.openmrs.performance.Constants.TEMPERATURE_C;
import static org.openmrs.performance.Constants.VISIT_NOTE_CONCEPT_UUID;
import static org.openmrs.performance.Constants.VISIT_NOTE_ENCOUNTER_TYPE_UUID;
import static org.openmrs.performance.Constants.VISIT_NOTE_FORM_UUID;
import static org.openmrs.performance.Constants.VITALS_ENCOUNTER_TYPE_UUID;
import static org.openmrs.performance.Constants.VITALS_FORM_UUID;
import static org.openmrs.performance.Constants.VITALS_LOCATION_UUID;
import static org.openmrs.performance.Constants.WEIGHT_KG;
import static org.openmrs.performance.Constants.BACK_PAIN;
import static org.openmrs.performance.Constants.DIAGNOSIS_CONCEPT;

public class DoctorHttpService extends HttpService {

	public HttpRequestActionBuilder getVisitWithDiagnosesAndNotes(String patientUuid) {
		return http("Get Visits With Diagnoses and Notes (new endpoint)")
		        .get("/openmrs/ws/rest/v1/emrapi/patient/" + patientUuid + "/visitWithDiagnosesAndNotes?limit=5");
	}

	public HttpRequestActionBuilder getOrderTypes() {
		return http("Get Order Types").get("/openmrs/ws/rest/v1/ordertype");
	}

	public HttpRequestActionBuilder getAllActiveOrders(String patientUuid) {
		return http("Get Active Orders").get(
		    "/openmrs/ws/rest/v1/order?patient=" + patientUuid + "&careSetting=" + CARE_SETTING_UUID + "&status=ACTIVE");
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

	public HttpRequestActionBuilder getImmunizations(String patientUuid) {
		return http("Get Immunizations of Patient")
		        .get("/openmrs/ws/fhir2/R4/Immunization?patient=" + patientUuid + "&_summary=data");
	}

	public HttpRequestActionBuilder getPrograms() {
		return http("Get Programs")
		        .get("/openmrs/ws/rest/v1/program?v=custom:(uuid,display,allWorkflows,concept:(uuid,display))");
	}

	public HttpRequestActionBuilder searchForConditions(String searchQuery) {
		return http("Search for Condition").get("/openmrs/ws/rest/v1/concept?name=" + searchQuery
		        + "&searchType=fuzzy&class=" + DIAGNOSIS_CONCEPT + "&v=custom:(uuid,display)");
	}

	public HttpRequestActionBuilder searchForDrug(String searchQuery) {
		String customRepresentation = "custom:(uuid,display,name,strength,dosageForm:(display,uuid),concept:(display,uuid))";
		return http("Search for Drug").get("/openmrs/ws/rest/v1/drug?name=" + searchQuery + "&v=" + customRepresentation);
	}

	public HttpRequestActionBuilder saveCondition(String patientUuid, String currentUserUuid) {
		String recordedDate = CommonUtils.getCurrentDateTimeAsString();
		String onSetDate = CommonUtils.getAdjustedDateTimeAsString(-2);
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

	public HttpRequestActionBuilder saveOrder() {

		return http("Save Drug Order").post("/openmrs/ws/rest/v1/encounter").body(StringBody(session -> {
			Map<String, Object> order = new HashMap<>();
			order.put("action", "NEW");
			order.put("asNeeded", false);
			order.put("asNeededCondition", null);
			order.put("careSetting", CARE_SETTING_UUID);
			order.put("concept", ASPRIN_CONCEPT_UUID);
			order.put("dose", 1);
			order.put("doseUnits", TABLET);
			order.put("dosingInstructions", "");
			order.put("dosingType", DEFAULT_DOSING_TYPE);
			order.put("drug", ASPRIN_DRUG_UUID);
			order.put("duration", null);
			order.put("durationUnits", DAYS);
			order.put("encounter", null);
			order.put("frequency", ONCE_DAILY);
			order.put("numRefills", 0);
			order.put("orderReasonNonCoded", "reason");
			order.put("orderer", session.getString("currentUserUuid"));
			order.put("patient", session.getString("patient_uuid"));
			order.put("quantity", 1);
			order.put("quantityUnits", TABLET);
			order.put("route", ORAL);
			order.put("type", "drugorder");

			Map<String, Object> encounter = new HashMap<>();

			encounter.put("encounterType", ORDER);
			encounter.put("location", OUTPATIENT_CLINIC_LOCATION_UUID);
			encounter.put("patient", session.getString("patient_uuid"));
			encounter.put("visit", session.getString("visitUuid"));
			encounter.put("obs", new Object[0]);
			encounter.put("orders", new Object[] { order });
			encounter.put("encounterDatetime", CommonUtils.getCurrentDateTimeAsString());
			try {
				return new ObjectMapper().writeValueAsString(encounter);
			}
			catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		})).check(jsonPath("$.uuid").saveAs("orderUuid"));
	}

	public HttpRequestActionBuilder discontinueDrugOrder() {
		return http("Discontinue the drug order").post("/openmrs/ws/rest/v1/order").body(StringBody(session -> {
			Map<String, Object> order = new HashMap<>();

			order.put("action", "DISCONTINUE");
			order.put("type", "drugorder");
			order.put("previousOrder", null);
			order.put("orderer", session.getString("currentUserUuid"));
			order.put("patient", session.getString("patient_uuid"));
			order.put("careSetting", CARE_SETTING_UUID);
			order.put("drug", ASPRIN_DRUG_UUID);
			order.put("concept", ASPRIN_CONCEPT_UUID);
			order.put("orderReasonNonCoded", "reason");
			order.put("encounter", session.getString("orderUuid"));

			try {
				return new ObjectMapper().writeValueAsString(order);
			}
			catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}));
	}

	public HttpRequestActionBuilder saveVisitNote(String patientUuid, String currentUser, String value) {
		String encounterDatetime = CommonUtils.getCurrentDateTimeAsString();

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

	public HttpRequestActionBuilder getPatientAttributes(String personUuid) {
		String customRepresentation = "custom:(uuid,display,attributeType:(uuid,display,format),value)";
		return http("Get Person Attributes")
		        .get("/openmrs/ws/rest/v1/person/" + personUuid + "/attribute?v=" + customRepresentation);
	}

	public HttpRequestActionBuilder getPatientIdentifiers(String patientUuid) {
		String customRepresentation = "custom:(uuid,identifier,identifierType:(uuid,required,name),preferred)";
		return http("Get Patient Identifiers")
		        .get("/openmrs/ws/rest/v1/patient/" + patientUuid + "/identifier?v=" + customRepresentation);
	}

	public HttpRequestActionBuilder getPatientRelationships(String personUuid) {
		String customRepresentation = "custom:(display,uuid,personA:(age,display,birthdate,uuid),personB:(age,display,birthdate,uuid),relationshipType:(uuid,display,description,aIsToB,bIsToA))";
		return http("Get Relationships of Person")
		        .get("/openmrs/ws/rest/v1/relationship?v=" + customRepresentation + "&person=" + personUuid);
	}

	public HttpRequestActionBuilder editPatientDetails(String patientUuid) {
		return http("Edit patient details").post("/openmrs/ws/rest/v1/patient/" + patientUuid).body(StringBody(session -> {
			Map<String, Object> payload = new HashMap<>();

			payload.put("uuid", session.get("patient_uuid"));

			Map<String, Object> person = new HashMap<>();
			person.put("uuid", session.get("patient_uuid"));

			List<Map<String, Object>> names = new ArrayList<>();
			Map<String, Object> nameItem = new HashMap<>();
			nameItem.put("uuid", session.getString("patientNameId"));
			nameItem.put("preferred", true);
			nameItem.put("givenName", "Mark");
			nameItem.put("familyName", "Williams");
			names.add(nameItem);
			person.put("names", names);

			person.put("gender", "M");
			person.put("birthdate", "1962-4-5");
			person.put("birthdateEstimated", false);
			person.put("attributes", new ArrayList<>());

			List<Map<String, Object>> addresses = new ArrayList<>();
			Map<String, Object> address = new HashMap<>();
			address.put("address1", "Address16582");
			address.put("cityVillage", "City6582");
			address.put("stateProvince", "State6582");
			address.put("postalCode", "898989");
			address.put("country", "Country6582");
			addresses.add(address);
			person.put("addresses", addresses);

			person.put("dead", false);

			payload.put("person", person);

			List<Map<String, Object>> identifiers = new ArrayList<>();
			Map<String, Object> identifier = new HashMap<>();
			identifier.put("uuid", session.getString("patientIdentifierId"));
			identifier.put("identifier", session.getString("patientIdentifierValue"));
			identifier.put("identifierType", PATIENT_IDENTIFIER_UUID);
			identifier.put("location", OUTPATIENT_CLINIC_LOCATION_UUID);
			identifier.put("preferred", true);
			identifiers.add(identifier);

			payload.put("identifiers", identifiers);

			try {
				return new ObjectMapper().writeValueAsString(payload);
			}
			catch (JsonProcessingException e) {
				throw new RuntimeException("Error converting identifiers to JSON", e);
			}
		}));
	}

	public HttpRequestActionBuilder addProgramEnrollment() {
		return http("Add new program to patient").post("/openmrs/ws/rest/v1/programenrollment").body(StringBody(session -> {
			Map<String, Object> payload = new HashMap<>();

			payload.put("program", HIV_CARE_TREATMENT);
			payload.put("patient", session.getString("patient_uuid"));
			payload.put("dateEnrolled", CommonUtils.getAdjustedDateTimeAsString(-1));
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

			        payload.put("dateEnrolled", CommonUtils.getAdjustedDateTimeAsString(-1));
			        payload.put("dateCompleted", CommonUtils.getCurrentDateTimeAsString());
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
			        payload.put("encounterDatetime", CommonUtils.getCurrentDateTimeAsString());
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
}
