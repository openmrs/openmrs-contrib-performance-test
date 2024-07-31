package org.openmrs.performance.scenarios;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.registries.DoctorRegistry;

import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.scenario;

public class VisitPatientScenario extends Scenario<DoctorRegistry> {
	
	public VisitPatientScenario(float scenarioLoadShare) {
		super(scenarioLoadShare, new DoctorRegistry());
	}
	
	@Override
	public ScenarioBuilder getScenarioBuilder() {
		FeederBuilder<String> patientUuidFeeder = csv("patient_uuids.csv").circular();
		
		return scenario("Doctor")
				.feed(patientUuidFeeder)
				.exec(registry.login())
				.exec(registry.openHomePage())
				.exec(registry.openPatientChartPage("#{patient_uuid}"))
				.exec(registry.startVisit("#{patient_uuid}"))
				.exec(registry.openVisitsTab("#{patient_uuid}"))
				.exec(registry.openVitalsAndBiometricsTab("#{patient_uuid}"))
				.exec(registry.openMedicationsTab("#{patient_uuid}"))
				.exec(registry.openOrdersTab("#{patient_uuid}"))
				.exec(registry.openLabResultsTab("#{patient_uuid}"))
				.exec(registry.openAllergiesTab("#{patient_uuid}"))
				.exec(registry.openConditionsTab("#{patient_uuid}"))
				.exec(registry.openImmunizationsTab("#{patient_uuid}"))
				.exec(registry.openAttachmentsTab("#{patient_uuid}"))
				.exec(registry.endVisit("#{patient_uuid}"));
	}
}
