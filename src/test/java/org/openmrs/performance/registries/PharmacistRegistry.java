package org.openmrs.performance.registries;

import io.gatling.javaapi.core.ChainBuilder;
import org.openmrs.performance.http.PharmacistHttpService;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.foreach;
import static org.openmrs.performance.Constants.ASPRIN_CONCEPT_UUID;
import static org.openmrs.performance.Constants.ASPRIN_DRUG_UUID;
import static org.openmrs.performance.Constants.REASON_TO_CLOSE_VALUE_SET;
import static org.openmrs.performance.Constants.SUBSTITUTION_REASON_VALUE_SET;
import static org.openmrs.performance.Constants.SUBSTITUTION_TYPE_VALUE_SET;

public class PharmacistRegistry extends Registry<PharmacistHttpService> {

	public PharmacistRegistry() {
		super(new PharmacistHttpService());
	}

	public ChainBuilder openOrdersTab(String patientUuid) {
		return exec(httpService.getOrderTypes()).exec(httpService.getAllActiveOrders(patientUuid));
	}

	public ChainBuilder addDrugOrder(String patientUuid) {
		return exec(httpService.getActiveVisitOfPatient(patientUuid), httpService.searchForDrug("asprin"),
		    httpService.searchForDrug("Tylenol"), httpService.saveOrder());
	}

	public ChainBuilder openDispensingApp() {
		return exec(httpService.getLocationsByTag("Login+Location"), httpService.getMedicationRequestEncounters())
		        .doIf(session -> session.contains("medicalPatientEncounterUuids"))
		        .then(foreach("#{medicalPatientEncounterUuids}", "medicalPatientUuid").on(exec(session -> {
			        String medicalPatientUuid = session.getString("medicalPatientUuid");
			        if (medicalPatientUuid != null) {
				        String modifiedUuid = medicalPatientUuid.split("/")[1];
				        return session.set("modifiedUuid", modifiedUuid);
			        }
			        return session;
		        }).exec(httpService.getPatientAge("#{modifiedUuid}"))));
	}

	public ChainBuilder selectPatientWithPrescription(String patientUuid) {
		return exec(httpService.getPatientAllergyIntolerance(patientUuid),
		    httpService.getSpecificMedicationEncounter("#{orderUuid}"),
		    httpService.getEncounterWithVisitAndDiagnoses("#{orderUuid}"), httpService.getAllProviders(),
		    httpService.getPatientConditions(patientUuid));
	}

	public ChainBuilder openDispensePrescription(String patientUuid) {
		return exec(httpService.getPatientSummaryData(patientUuid), httpService.getOrderEntryConfig(),
		    httpService.getValueSetByUuid(SUBSTITUTION_TYPE_VALUE_SET),
		    httpService.getValueSetByUuid(SUBSTITUTION_REASON_VALUE_SET),
		    httpService.getMedicationRequestByUuid("#{medicationRequestUuid}"),
		    httpService.getMedicationByUuid(ASPRIN_DRUG_UUID), httpService.getAllProviders(),
		    httpService.searchMedicationByCode(ASPRIN_CONCEPT_UUID),
		    httpService.getSpecificMedicationEncounter("#{orderUuid}"), httpService.getPatientIdPhoto(patientUuid),
		    httpService.getPrimaryIdentifierTermMapping(), httpService.getActiveVisitOfPatient(patientUuid),
		    httpService.getVisitQueueEntry(patientUuid), httpService.getPatientLifeStatus(patientUuid));
	}

	public ChainBuilder dispenseMedication() {
		return exec(httpService.dispenseMedicine(), httpService.getMedicationRequestEncounters(),
		    httpService.getMedicationRequestByUuid("#{medicationRequestUuid}"),
		    httpService.getSpecificMedicationEncounter("#{orderUuid}"));
	}

	public ChainBuilder openClosePrescription() {
		return exec(httpService.getPatientSummaryData("#{patient_uuid}"),
		    httpService.getValueSetByUuid(REASON_TO_CLOSE_VALUE_SET), httpService.getPatientIdPhoto("#{patient_uuid}"),
		    httpService.getPrimaryIdentifierTermMapping(), httpService.getActiveVisitOfPatient("#{patient_uuid}"),
		    httpService.getPatientLifeStatus("#{patient_uuid}"));
	}

	public ChainBuilder closeMedication() {
		return exec(httpService.closeMedication(), httpService.getMedicationRequestEncounters(),
		    httpService.getMedicationRequestByUuid("#{medicationRequestUuid}"),
		    httpService.getSpecificMedicationEncounter("#{orderUuid}"));
	}

	public ChainBuilder discontinueDrugOrder() {
		return exec(httpService.discontinueDrugOrder());
	}

}
