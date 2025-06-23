package org.openmrs.performance.registries;

import io.gatling.javaapi.core.ChainBuilder;
import org.openmrs.performance.http.LabTechHttpService;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static org.openmrs.performance.Constants.TEST_ORDER_TYPE;

public class LabTechRegistry extends Registry<LabTechHttpService> {

	public LabTechRegistry() {
		super(new LabTechHttpService());
	}

	public ChainBuilder addLabOrder(String patientUuid) {
		return exec(httpService.getActiveVisitOfPatient(patientUuid), httpService.getSpecificVisitDetails("#{visitUuid}"),
		    httpService.getOrderTypeDetails(TEST_ORDER_TYPE), httpService.getAllLabOrderDetails(),
		    httpService.getModuleInformation(), httpService.addLabOrder());
	}

	public ChainBuilder loadLaboratory() {
		return exec(httpService.getAllActiveLabOrders(), httpService.getLabOrdersByFullFillerStatus("IN_PROGRESS"),
		    httpService.getLabOrdersByFullFillerStatus("COMPLETED"), httpService.getLabOrdersByFullFillerStatus("DECLINED"));
	}

	public ChainBuilder pickUpLabOrder() {
		return exec(httpService.updateFullFillerStatus("#{labOrderUuid}", "IN_PROGRESS", ""));
	}

	public ChainBuilder completeLabOrder(String patientUuid) {
		return exec(httpService.getEncounterDetails("#{labEncounterUuid}"), httpService.getAlkalineConceptDetails(),
		    httpService.updateSpecificEncounter("#{labEncounterUuid}"), httpService.updateLabOrderCompletion(),
		    httpService.updateFullFillerStatus("#{labOrderUuid}", "COMPLETED", "Test Results Entered"));
	}
}
