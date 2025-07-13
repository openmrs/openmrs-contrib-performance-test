package org.openmrs.performance.scenarios;

import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.registries.DoctorRegistry;
import org.openmrs.performance.utils.SharedPoolFeeder;

import java.util.Iterator;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.scenario;

public class PatientVisitScenario extends Scenario<DoctorRegistry> {

	public PatientVisitScenario(float scenarioLoadShare) {
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
				.exec(registry.openClinicalFormWorkspace("#{patient_uuid}"))
				.pause(5)
				.exec(registry.openSoapTemplateForm("#{patient_uuid}"))
				.pause(5)
				.exec(registry.saveSoapTemplateForm("#{patient_uuid}"))
				.pause(3)
				.exec(registry.openVisitsTab("#{patient_uuid}"))
				.pause(2)
				.exec(registry.getVisitsFromNewEndpoint("#{patient_uuid}"))
				.pause(5)
				.exec(registry.openMedicationsTab("#{patient_uuid}"))
				.pause(5)
				.exec(registry.openLabResultsTab("#{patient_uuid}"))
				.pause(10)
				.exec(registry.openConditionsTab("#{patient_uuid}"))
				.pause(5)
				.exec(registry.addCondition("#{patient_uuid}", "#{currentUserUuid}"))
				.pause(10)
				.exec(registry.openImmunizationsTab("#{patient_uuid}"))
				.pause(5)
				.exec(registry.openImmunizationForm("#{patient_uuid}"))
				.pause(5)
				.exec(registry.addImmunization("#{currentUserUuid}"))
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
				.exec(registry.addVisitNote("#{patient_uuid}", "#{currentUserUuid}"))
				.pause(10)
				.exec(registry.endVisit("#{patient_uuid}"))
				.exec(session -> {
					String uuid = session.getString("patient_uuid");
					SharedPoolFeeder.returnUuid(uuid);
					return session;
				});
		// @formatter:on
	}

}
