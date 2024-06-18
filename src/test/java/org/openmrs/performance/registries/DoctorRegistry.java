package org.openmrs.performance.registries;
import io.gatling.javaapi.core.ChainBuilder;

import java.time.ZonedDateTime;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static org.openmrs.performance.Constants.FACULTY_VISIT_TYPE_UUID;
import static org.openmrs.performance.Constants.OUTPATIENT_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.http.CommonHttpRequests.getCurrentVisit;
import static org.openmrs.performance.http.CommonHttpRequests.getVisitQueueEntry;
import static org.openmrs.performance.http.DoctorHttpRequests.*;

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
	
	// Review Vitals and Biometrics
	public static ChainBuilder reviewVitalsAndBiometrics(String patientUuid) {
		return null;
	}
	
	// Medications,
	public static ChainBuilder reviewMedications(String patientUuid) {
		return null;
	}
	
	// orders,
	public static ChainBuilder reviewOrders(String patientUuid) {
		return null;
	}
	// Lab results,
	public static ChainBuilder reviewLabResults(String patientUuid) {
		return null;
	}
	// visits allergirs,
	public static ChainBuilder reviewAllergies(String patientUuid) {
		return null;
	}
	// conditions,
	public static ChainBuilder reviewConditions(String patientUuid) {
		return null;
	}
	// immunizations,
	public static ChainBuilder reviewImmunizations(String patientUuid) {
		return null;
	}
	// attachments
	public static ChainBuilder reviewAttachments(String patientUuid) {
		return null;
	}
	
}
