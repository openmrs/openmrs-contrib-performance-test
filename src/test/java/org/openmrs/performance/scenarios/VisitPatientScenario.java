package org.openmrs.performance.scenarios;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.registries.DoctorRegistry;
import org.openmrs.performance.utils.SharedPoolFeeder;

import java.util.Iterator;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.scenario;

public class VisitPatientScenario extends Scenario<DoctorRegistry> {

	public VisitPatientScenario(float scenarioLoadShare) {
		super(scenarioLoadShare, new DoctorRegistry());
	}

	@Override
	public ScenarioBuilder getScenarioBuilder() {
		Iterator<Map<String, Object>> patientUuidFeeder = SharedPoolFeeder.feeder();

		// @formatter:off
		return scenario("Doctor - Visit Patient").feed(patientUuidFeeder)
				.exec(registry.login())
		        .exec(registry.openHomePage())
				.pause(5)
				.exec(registry.openPatientChartPage("#{patient_uuid}"))
				.pause(5)
		        .exec(registry.startVisit("#{patient_uuid}"))
				.pause(5)
				.exec(registry.openClinicalForm("#{patient_uuid}"))
				.exec(registry.openVisitsTab("#{patient_uuid}"))
				.pause(5)
				.exec(registry.openEditPatientTab("#{patient_uuid}"))
				.pause(5)
				.exec(registry.editPatientDetails("#{patient_uuid}"))
		        .pause(2)
				.exec(registry.getVisitsFromNewEndpoint("#{patient_uuid}"))
				.pause(5)
				.exec(registry.openVitalsAndBiometricsTab("#{patient_uuid}"))
				.pause(5)
		        .exec(registry.recordVitals("#{patient_uuid}"))
				.pause(5)
				.exec(registry.openMedicationsTab("#{patient_uuid}"))
		        .pause(5)
				.exec(registry.openOrdersTab("#{patient_uuid}"))
				.pause(5)
		        .exec(registry.openLabResultsTab("#{patient_uuid}"))
				.pause(8)
		        .exec(registry.openAllergiesTab("#{patient_uuid}"))
				.pause(5)
				.exec(registry.openAllergiesForm())
				.pause(5)
		        .exec(registry.recordAllergy("#{patient_uuid}"))
				.pause(10)
		        .exec(registry.openConditionsTab("#{patient_uuid}"))
				.pause(5)
				.exec(registry.addCondition("#{patient_uuid}", "#{currentUserUuid}"))
				.pause(10)
				.exec(registry.openImmunizationsTab("#{patient_uuid}"))
				.pause(5)
		        .exec(registry.openAttachmentsTab("#{patient_uuid}"))
				.pause(5)
		        .exec(registry.openProgramsTab("#{patient_uuid}"))
				.pause(5)
				.exec(registry.addProgramEnrollment("#{patient_uuid}"))
				.pause(2)
				.exec(registry.completeProgramEnrollment("#{patient_uuid}"))
				.pause(5)
				.exec(registry.addAttachment("#{patient_uuid}"))
		        .pause(5)
				.exec(registry.openAppointmentsTab("#{patient_uuid}"))
				.pause(5)
		        .exec(registry.addDrugOrder("#{patient_uuid}"))
				.pause(5)
				.exec(registry.discontinueDrugOrder()) // Delete drug order end-point was introduces to overcome the drug duplication issue
				.pause(5)
		        .exec(registry.addVisitNote("#{patient_uuid}", "#{currentUserUuid}"))
				.pause(10)
				.exec(registry.addClinicalForm("#{patient_uuid}"))
				.exec(registry.submitClinicalForm("#{patient_uuid}"))
		        .exec(registry.endVisit("#{patient_uuid}"))
				.exec(session -> {
					String uuid = session.getString("patient_uuid");
					SharedPoolFeeder.returnUuid(uuid);
					return session;
				});
		// @formatter:on
	}

}
