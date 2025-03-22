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

		return scenario("Doctor - Visit Patient").feed(patientUuidFeeder).exec(registry.login())
		        .exec(registry.openHomePage()).pause(5).exec(registry.openPatientChartPage("#{patient_uuid}")).pause(5)
		        .exec(registry.startVisit("#{patient_uuid}")).pause(5).exec(registry.openVisitsTab("#{patient_uuid}"))
		        .pause(2).exec(registry.openVitalsAndBiometricsTab("#{patient_uuid}")).pause(5)
		        .exec(registry.recordVitals("#{patient_uuid}")).pause(5).exec(registry.openMedicationsTab("#{patient_uuid}"))
		        .pause(5).exec(registry.openOrdersTab("#{patient_uuid}")).pause(5)
		        .exec(registry.openLabResultsTab("#{patient_uuid}")).pause(8)
		        .exec(registry.openAllergiesTab("#{patient_uuid}")).pause(5).exec(registry.openAllergiesForm()).pause(5)
		        .exec(registry.recordAllergy("#{patient_uuid}")).pause(10)
		        .exec(registry.openConditionsTab("#{patient_uuid}")).pause(5)
		        .exec(registry.openImmunizationsTab("#{patient_uuid}")).pause(5)
		        .exec(registry.openAttachmentsTab("#{patient_uuid}")).pause(5)
		        .exec(registry.openProgramsTab("#{patient_uuid}")).pause(5).exec(registry.addAttachment("#{patient_uuid}"))
		        .pause(5).exec(registry.openAppointmentsTab("#{patient_uuid}")).pause(5)
		        .exec(registry.addDrugOrder("#{patient_uuid}", "#{visitUuid}", "#{currentUserUuid}")).pause(5)
		        .exec(registry.addVisitNote("#{patient_uuid}", "#{currentUserUuid}")).pause(10)
		        .exec(registry.endVisit("#{patient_uuid}"));
	}
}
