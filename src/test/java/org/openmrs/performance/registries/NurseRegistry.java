package org.openmrs.performance.registries;

import io.gatling.javaapi.core.ChainBuilder;
import org.openmrs.performance.http.NurseHttpService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.foreach;
import static org.openmrs.performance.Constants.ADMISSION_ENCOUNTER_TYPE_UUID;
import static org.openmrs.performance.Constants.ARTERIAL_BLOOD_OXYGEN_SATURATION;
import static org.openmrs.performance.Constants.CLINICAL_NOTES;
import static org.openmrs.performance.Constants.DIASTOLIC_BLOOD_PRESSURE;
import static org.openmrs.performance.Constants.DISCHARGE_ENCOUNTER_TYPE_UUID;
import static org.openmrs.performance.Constants.FACULTY_VISIT_TYPE_UUID;
import static org.openmrs.performance.Constants.INPATEINT_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.Constants.PULSE;
import static org.openmrs.performance.Constants.RESPIRATORY_RATE;
import static org.openmrs.performance.Constants.SYSTOLIC_BLOOD_PRESSURE;
import static org.openmrs.performance.Constants.TEMPERATURE_C;
import static org.openmrs.performance.Constants.TRANSFER_DISPOSITION;
import static org.openmrs.performance.Constants.TRANSFER_LOCATION;
import static org.openmrs.performance.Constants.TRANSFER_OBSERVATION;
import static org.openmrs.performance.Constants.TRANSFER_PATIENT_REQUEST;
import static org.openmrs.performance.Constants.UNKNOWN_OBSERVATION_TYPE;
import static org.openmrs.performance.Constants.WARD1_CLINIC_LOCATION_UUID;

public class NurseRegistry extends Registry<NurseHttpService> {

	public NurseRegistry() {
		super(new NurseHttpService());
	}

	public ChainBuilder openWardPage(String locationUuid) {
		return exec(httpService.getAdmissionLocationInfo(locationUuid), httpService.getAdmittedPatientInfo(locationUuid),
		    httpService.getInpatientRequest(locationUuid)).doIf(session -> session.contains("admittedPatientUuid"))
		        .then(foreach("#{admittedPatientUuid}", "uuid")
		                .on(exec(httpService.getOrdersWithNullFulfillerStatusAndActivatedDate("#{uuid}"))));
	}

	public ChainBuilder admitPatientToWardPage(String patientUuid) {
		return exec(httpService.getVisitTypes(), httpService.getLocationsThatSupportVisits(),
		    httpService.getProgramEnrollments(patientUuid), httpService.getLocationsByTag("Visit+Location"),
		    httpService.getAppointmentsOfPatient(patientUuid),
		    httpService.getVisitsOfLocation(INPATEINT_CLINIC_LOCATION_UUID),
		    httpService.submitVisitForm(patientUuid, FACULTY_VISIT_TYPE_UUID, INPATEINT_CLINIC_LOCATION_UUID),
		    httpService.getBedsByPatientUuid(patientUuid), httpService.saveWardEncounter("Admission",
		        Collections.emptyList(), ADMISSION_ENCOUNTER_TYPE_UUID, INPATEINT_CLINIC_LOCATION_UUID));
	}

	public ChainBuilder openingPatientDetails(String patientUuid) {
		Set<String> vitals = Set.of(SYSTOLIC_BLOOD_PRESSURE, DIASTOLIC_BLOOD_PRESSURE, PULSE, TEMPERATURE_C,
		    ARTERIAL_BLOOD_OXYGEN_SATURATION, RESPIRATORY_RATE, UNKNOWN_OBSERVATION_TYPE);

		Set<String> refRangesConcept = Set.of(SYSTOLIC_BLOOD_PRESSURE, DIASTOLIC_BLOOD_PRESSURE, PULSE, TEMPERATURE_C,
		    RESPIRATORY_RATE, ARTERIAL_BLOOD_OXYGEN_SATURATION);

		return exec(httpService.getActiveVisitOfPatient(patientUuid), httpService.getIsVisitsEnabled(),
		    httpService.getVitalConceptSetDetails(), httpService.getPatientObservations(patientUuid, vitals),
		    httpService.getVitalsConceptRefRanges(patientUuid, refRangesConcept),
		    httpService.getSpecificVisitDetails("#{visitUuid}"));
	}

	public ChainBuilder transferPatient() {
		Map<String, Object> obs = new HashMap<>();

		obs.put("concept", TRANSFER_OBSERVATION);

		Map<String, Object> transferLocationConcept = new HashMap<>();
		transferLocationConcept.put("concept", TRANSFER_LOCATION);
		transferLocationConcept.put("value", WARD1_CLINIC_LOCATION_UUID);

		Map<String, Object> groupMember = new HashMap<>();
		groupMember.put("concept", TRANSFER_DISPOSITION);
		groupMember.put("value", "CIEL:167731");

		Map<String, Object> transferLocationNote = new HashMap<>();
		transferLocationNote.put("concept", CLINICAL_NOTES);
		transferLocationNote.put("value", "test");

		obs.put("groupMembers", List.of(transferLocationConcept, groupMember, transferLocationNote));

		return exec(httpService.getCustomTransferLocationsConfiguration(), httpService.getTransferableLocations(),
		    httpService.saveWardEncounter("Transfer", List.of(obs), TRANSFER_PATIENT_REQUEST,
		        INPATEINT_CLINIC_LOCATION_UUID));
	}

	public ChainBuilder moveToDifferentLocationSession() {
		return exec(httpService.getLocations(), httpService.getAllLocationsSearchSet(),
		    httpService.changeTheSessionLocation(WARD1_CLINIC_LOCATION_UUID));
	}

	public ChainBuilder admitTheTransferPatient() {
		return exec(httpService.getPrimaryIdentifierTermMapping()).doIf(session -> session.contains("transferPatientUuid"))
		        .then(foreach("#{transferPatientUuid}", "uuid").on(exec(httpService.getPatientAdmissionNote("#{uuid}"))))
		        .exec(httpService.getBedsByPatientUuid("#{patient_uuid}")).exec(httpService.saveWardEncounter("Admission",
		            Collections.emptyList(), ADMISSION_ENCOUNTER_TYPE_UUID, WARD1_CLINIC_LOCATION_UUID));
	}

	public ChainBuilder dischargePatient() {
		return exec(httpService.saveWardEncounter("Discharge", Collections.emptyList(), DISCHARGE_ENCOUNTER_TYPE_UUID,
		    WARD1_CLINIC_LOCATION_UUID)).exec(httpService.submitEndVisit("#{visitUuid}"));
	}
}
