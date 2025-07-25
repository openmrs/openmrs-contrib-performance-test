package org.openmrs.performance.registries;

import io.gatling.javaapi.core.ChainBuilder;
import org.openmrs.performance.http.ClerkHttpService;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.pause;
import static org.openmrs.performance.Constants.*;

public class ClerkRegistry extends Registry<ClerkHttpService> {

	public ClerkRegistry() {
		super(new ClerkHttpService());
	}

	public ChainBuilder openRegistrationPage() {
		return exec(httpService.getAddressTemplate(), httpService.getPatientIdentifierTypes(),
		    httpService.getPrimaryIdentifierTermMapping(), httpService.getRelationshipTypes(),
		    httpService.getModuleInformation(), httpService.getPersonAttributeType(PERSON_ATTRIBUTE_PHONE_NUMBER),
		    httpService.getAutoGenerationOptions(), httpService.getOrderedAddressHierarchyLevels(),
		    httpService.getIdentifierSources());
	}

	public ChainBuilder registerPatient() {
		return exec(httpService.generateOMRSIdentifier(), httpService.sendPatientRegistrationRequest(), pause(2));
	}

	public ChainBuilder openAppointmentPage() {
		return exec(httpService.getAllAppointmentServices(), httpService.getAppointmentsOfTheDay(),
		    httpService.getAppointmentsSummary(), httpService.getAppointmentByStatus("Scheduled"),
		    httpService.getAppointmentByStatus("Missed"), httpService.getAppointmentByStatus("Completed"),
		    httpService.getAppointmentByStatus("Cancelled"), httpService.getDefaultAppointmentService(),
		    httpService.getAllVisitsOfTheLocationWithDate(OUTPATIENT_CLINIC_LOCATION_UUID));
	}

	public ChainBuilder openAppointmentFormPage(String patientUuid) {
		return exec(httpService.getLocationsByTag("Appointment+Location"), httpService.getPatientLifeStatus(patientUuid),
		    httpService.getPatientSummaryData(patientUuid), httpService.getAllAppointmentServices(),
		    httpService.getPatientIdPhoto(patientUuid), httpService.getPatientQueueEntry(patientUuid),
		    httpService.getAllProviders(), httpService.getActiveVisitOfPatient(patientUuid));
	}

	public ChainBuilder createAppointment() {
		return exec(httpService.checkAppointmentConflicts(), httpService.createAppointment());
	}

	public ChainBuilder checkInPatient(String patientUuid) {
		return exec(httpService.getVisitTypes(), httpService.getLocationsThatSupportVisits(),
		    httpService.getProgramEnrollments(patientUuid), httpService.getLocationsByTag("Visit+Location"),
		    httpService.getAppointmentsOfPatient(patientUuid),
		    httpService.getVisitsOfLocation(OUTPATIENT_CLINIC_LOCATION_UUID),
		    httpService.submitVisitForm(patientUuid, FACULTY_VISIT_TYPE_UUID, OUTPATIENT_CLINIC_LOCATION_UUID),
		    httpService.submitAppointmentStatusChange("#{appointmentUuid}", "CheckedIn"));
	}

	public ChainBuilder checkOutPatient() {
		return exec(httpService.submitEndVisit("#{visitUuid}"), httpService.getVisitTypes(),
		    httpService.getLocationsThatSupportVisits(),
		    httpService.submitAppointmentStatusChange("#{appointmentUuid}", "Completed"));
	}

	public ChainBuilder openPatientLists() {
		return exec(httpService.getAllPatientsList(), httpService.getAllPatientsList(SYSTEM_GENERATED_PATIENT_LIST),
		    httpService.getAllPatientsList(USER_GENERATED_PATIENT_LIST));
	}

	public ChainBuilder createNewPatientList() {
		return exec(httpService.createPatientList(), httpService.getAllPatientsList());
	}

	public ChainBuilder openSpecificPatientList(String patientListUuid) {
		return exec(httpService.getPatientList(patientListUuid), httpService.getMembersOfPatientList(patientListUuid));
	}

	public ChainBuilder addPatientToQueue(String patientUuid) {
		return exec(httpService.getVisitTypes(), httpService.getLocationsThatSupportVisits(),
		    httpService.getProgramEnrollments(patientUuid), httpService.getLocationsByTag("Visit+Location"),
		    httpService.getAppointmentsOfPatient(patientUuid),
		    httpService.submitVisitForm(patientUuid, FACULTY_VISIT_TYPE_UUID, OUTPATIENT_CLINIC_LOCATION_UUID),
		    httpService.getPatientIdPhoto(patientUuid), httpService.getPrimaryIdentifierTermMapping(),
		    httpService.getPatientSummaryData(patientUuid), httpService.getActiveVisitOfPatient(patientUuid),
		    httpService.getPatientQueueEntry(patientUuid), httpService.getPatientLifeStatus(patientUuid),
		    httpService.getQueueEntryNumber("#{visitUuid}"), httpService.submitVisitQueueEntry(),
		    httpService.getQueueEntry());
	}

	public ChainBuilder addNewServiceQueue() {
		return exec(httpService.getServiceConceptSet(), httpService.getConcept("#{serviceConceptSetUuid}"),
		    httpService.submitNewServiceQueue());
	}

	public ChainBuilder transitionPatient() {
		return exec(httpService.submitTransitionRequest(), httpService.getQueueEntry());
	}

	public ChainBuilder openEditPatientTab(String patientUuid) {
		return exec(httpService.getAddressTemplate(), httpService.getPatientIdentifierTypes(),
		    httpService.getPrimaryIdentifierTermMapping(), httpService.getRelationshipTypes(),
		    httpService.getModuleInformation(), httpService.getPersonAttributeType(PERSON_ATTRIBUTE_PHONE_NUMBER),
		    httpService.getAutoGenerationOptions(), httpService.getOrderedAddressHierarchyLevels(),
		    httpService.getIdentifierSources(), httpService.getPatientLifeStatus(patientUuid),
		    httpService.getPatientIdPhoto(patientUuid), httpService.getPatientSummaryData(patientUuid),
		    httpService.getPatientAttributes(patientUuid), httpService.getPatientIdentifiers(patientUuid),
		    httpService.getPatientRelationships(patientUuid));
	}

	public ChainBuilder editPatientDetails(String patientUuid) {
		return exec(httpService.editPatientDetails(patientUuid));
	}
}
