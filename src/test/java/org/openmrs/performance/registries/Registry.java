package org.openmrs.performance.registries;

import io.gatling.javaapi.core.ChainBuilder;
import org.openmrs.performance.http.HttpService;

import java.util.Set;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.foreach;
import static io.gatling.javaapi.core.CoreDsl.pause;
import static org.openmrs.performance.Constants.ARTERIAL_BLOOD_OXYGEN_SATURATION;
import static org.openmrs.performance.Constants.DIASTOLIC_BLOOD_PRESSURE;
import static org.openmrs.performance.Constants.FACULTY_VISIT_TYPE_UUID;
import static org.openmrs.performance.Constants.HEIGHT_CM;
import static org.openmrs.performance.Constants.MID_UPPER_ARM_CIRCUMFERENCE;
import static org.openmrs.performance.Constants.OUTPATIENT_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.Constants.PULSE;
import static org.openmrs.performance.Constants.RESPIRATORY_RATE;
import static org.openmrs.performance.Constants.SYSTOLIC_BLOOD_PRESSURE;
import static org.openmrs.performance.Constants.TEMPERATURE_C;
import static org.openmrs.performance.Constants.UNKNOWN_OBSERVATION_TYPE;
import static org.openmrs.performance.Constants.WEIGHT_KG;

public abstract class Registry<H extends HttpService> {

	public H httpService;

	public Registry(H httpService) {
		this.httpService = httpService;
	}

	public ChainBuilder login() {
		return exec(httpService.loginRequest(), pause(1), httpService.getLocations(), pause(5),
		    httpService.selectLocation());
	}

	public ChainBuilder openHomePage() {
		return exec(httpService.getAddressTemplate(), httpService.getRelationshipTypes(),
		    httpService.getAppointmentsForSpecificDate("2024-05-15T00:00:00.000+0530"), httpService.getModuleInformation(),
		    httpService.getPatientIdentifierTypes(), httpService.getPrimaryIdentifierTermMapping(),
		    httpService.getIdentifierSources(), httpService.getLocationsByTag("queue location"),
		    httpService.getVisitsOfLocation(OUTPATIENT_CLINIC_LOCATION_UUID), httpService.getAutoGenerationOptions(),
		    httpService.getIdentifierSources(), httpService.getQueueEntry());
	}

	public ChainBuilder openPatientChartPage(String patientUuid) {
		Set<String> unknownObservationSet = Set.of(SYSTOLIC_BLOOD_PRESSURE, DIASTOLIC_BLOOD_PRESSURE, PULSE, TEMPERATURE_C,
		    ARTERIAL_BLOOD_OXYGEN_SATURATION, HEIGHT_CM, WEIGHT_KG, RESPIRATORY_RATE, UNKNOWN_OBSERVATION_TYPE,
		    MID_UPPER_ARM_CIRCUMFERENCE);

		Set<String> vitals = Set.of(SYSTOLIC_BLOOD_PRESSURE, DIASTOLIC_BLOOD_PRESSURE, PULSE, TEMPERATURE_C,
		    ARTERIAL_BLOOD_OXYGEN_SATURATION, RESPIRATORY_RATE, UNKNOWN_OBSERVATION_TYPE);

		Set<String> biometrics = Set.of(HEIGHT_CM, WEIGHT_KG, MID_UPPER_ARM_CIRCUMFERENCE);
		return exec(httpService.getPatientSummaryData(patientUuid), httpService.getActiveVisitOfPatient(patientUuid),
		    httpService.getPrimaryIdentifierTermMapping(), httpService.getIsVisitsEnabled(),
		    httpService.getPatientLifeStatus(patientUuid), httpService.getVitalConceptSetDetails(),
		    httpService.getPatientObservations(patientUuid, unknownObservationSet),
		    httpService.getPatientObservations(patientUuid, vitals),
		    httpService.getPatientObservations(patientUuid, biometrics), httpService.getVisitQueueEntry(patientUuid),
		    httpService.getPatientConditions(patientUuid), httpService.getActiveOrders(patientUuid));
	}

	public ChainBuilder searchPatient() {
		return exec(httpService.getPatients("jay")).doIf(session -> session.contains("patientIDs"))
		        .then(foreach("#{patientIDs}", "patientId").on(exec(httpService.getActiveVisitOfPatient("#{patientId}"),
		            httpService.getPatientIdPhoto("#{patientId}"), httpService.getPatientLifeStatus("#{patientId}"))));
	}

	public ChainBuilder startVisit(String patientUuid) {
		return exec(httpService.getVisitTypes()).exec(httpService.getCurrentVisit(patientUuid))
		        .exec(httpService.getVisitsOfPatient(patientUuid)).exec(httpService.getProgramEnrollments(patientUuid))
		        .exec(httpService.getVisitQueueEntry(patientUuid)).exec(httpService.getAppointmentsOfPatient(patientUuid))
		        .pause(5)
		        .exec(httpService.submitVisitForm(patientUuid, FACULTY_VISIT_TYPE_UUID, OUTPATIENT_CLINIC_LOCATION_UUID))
		        .exec(httpService.getCurrentVisit(patientUuid)).exec(httpService.getVisitsOfPatient(patientUuid));
	}

	public ChainBuilder endVisit(String patientUuid) {
		return exec(httpService.submitEndVisit("#{visitUuid}")).exec(httpService.getCurrentVisit(patientUuid))
		        .exec(httpService.getVisitsOfPatient(patientUuid));

	}
}
