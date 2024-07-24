package org.openmrs.performance.registries;

import io.gatling.javaapi.core.ChainBuilder;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static org.openmrs.performance.Constants.*;
import static org.openmrs.performance.http.ClerkHttpRequests.getPatientObservations;
import static org.openmrs.performance.http.CommonHttpRequests.getCurrentVisit;
import static org.openmrs.performance.http.CommonHttpRequests.getVisitQueueEntry;
import static org.openmrs.performance.http.DoctorHttpRequests.*;
import static org.openmrs.performance.utils.CommonUtils.extractConceptIds;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DoctorRegistry {
	
	public static ChainBuilder startVisit(String patientUuid) {
		
		return exec(getVisitTypes())
				.exec(getCurrentVisit(patientUuid))
				.exec(getVisits(patientUuid))
				.exec(getProgramEnrollments(patientUuid))
				.exec(getVisitQueueEntry(patientUuid))
				.exec(getAppointments(patientUuid))
				.pause(5)
				.exec(submitVisitForm(patientUuid, FACULTY_VISIT_TYPE_UUID, OUTPATIENT_CLINIC_LOCATION_UUID))
				.exec(getCurrentVisit(patientUuid))
				.exec(getVisits(patientUuid));
	}
	
	public static ChainBuilder endVisit(String patientUuid) {
		return exec(submitEndVisit("#{visitUuid}", OUTPATIENT_CLINIC_LOCATION_UUID, FACULTY_VISIT_TYPE_UUID))
				.exec(getCurrentVisit(patientUuid))
				.exec(getVisits(patientUuid));
	}
	
	public static ChainBuilder reviewVitalsAndBiometrics(String patientUuid) {
		
		Set<String> vitals = Set.of(
				SYSTOLIC_BLOOD_PRESSURE,
				DIASTOLIC_BLOOD_PRESSURE,
				PULSE,
				TEMPERATURE_C,
				ARTERIAL_BLOOD_OXYGEN_SATURATION,
				RESPIRATORY_RATE,
				UNKNOWN_OBSERVATION_TYPE
		);
		
		Set<String> biometrics = Set.of(
				HEIGHT_CM,
				WEIGHT_KG,
				MID_UPPER_ARM_CIRCUMFERENCE
		);
		return exec(getPatientObservations(patientUuid, vitals))
				.exec(getPatientObservations(patientUuid, biometrics));
	}
	
	public static ChainBuilder reviewMedications(String patientUuid) {
		return exec(getDrugOrders(patientUuid));
	}
	
	public static ChainBuilder reviewOrders(String patientUuid) {
		return exec(getOrderTypes())
				.exec(getAllActiveOrders(patientUuid));
	}
	
	public static ChainBuilder reviewLabResults(String patientUuid) {
		return exec(getLabResults(patientUuid))
				.exec(session -> {
					// Extract concept IDs from the lab results response
					String response = session.getString("labResultsResponse");
					List<String> conceptIds = extractConceptIds(response);
					System.out.println("Concept IDs: " + conceptIds);
					// Save concept IDs in the session
					return session.set("labResultConceptIds", conceptIds);
				})
				.foreach("#{labResultConceptIds}", "conceptId").on(
						exec(getConcept("#{conceptId}"))
				);
	}
	
	public static ChainBuilder reviewAllergies(String patientUuid) {
		return exec(getAllergies(patientUuid));
	}
	
	public static ChainBuilder reviewConditions(String patientUuid) {
		return exec(getConditions(patientUuid));
	}
	
	public static ChainBuilder reviewImmunizations(String patientUuid) {
		return null;
	}
	
	public static ChainBuilder reviewAttachments(String patientUuid) {
		return exec(getAttachments(patientUuid))
				.exec(getAllowedFileExtensions());
	}
}
