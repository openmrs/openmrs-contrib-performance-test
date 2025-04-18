package org.openmrs.performance.registries;

import io.gatling.javaapi.core.ChainBuilder;
import org.openmrs.performance.http.HttpService;

import java.util.Set;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.pause;
import static org.openmrs.performance.Constants.ARTERIAL_BLOOD_OXYGEN_SATURATION;
import static org.openmrs.performance.Constants.DIASTOLIC_BLOOD_PRESSURE;
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
		    httpService.getVisitsOfLocation(OUTPATIENT_CLINIC_LOCATION_UUID), httpService.getAutoGenerationOptions(),
		    httpService.getIdentifierSources());
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
}

//openPatientChartPage
//https://dev3.openmrs.org/openmrs/ws/rest/v1/systemsetting/visits.enabled?v=custom:(value)
//https://dev3.openmrs.org/openmrs/ws/rest/v1/person/73366b93-0838-4c62-9f19-53bb4612b09f?v=custom:(causeOfDeath:(display),causeOfDeathNonCoded)
//https://dev3.openmrs.org/openmrs/ws/rest/v1/metadatamapping/termmapping?v=custom:(metadataUuid)&code=emr.primaryIdentifierType
//https://dev3.openmrs.org/openmrs/ws/rest/v1/concept/1114AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA?v=custom:(setMembers:(uuid,display,hiNormal,hiAbsolute,hiCritical,lowNormal,lowAbsolute,lowCritical,units))
//https://dev3.openmrs.org/openmrs/ws/rest/v1/visit?patient=c457027f-b1fa-4f26-8555-90c5360625ce&v=custom:(uuid,display,voided,indication,startDatetime,stopDatetime,encounters:(uuid,display,encounterDatetime,form:(uuid,name),location:ref,encounterType:ref,encounterProviders:(uuid,display,provider:(uuid,display))),patient:(uuid,display),visitType:(uuid,name,display),attributes:(uuid,display,attributeType:(name,datatypeClassname,uuid),value),location:(uuid,name,display))&includeInactive=false
