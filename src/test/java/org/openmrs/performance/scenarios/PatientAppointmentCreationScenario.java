package org.openmrs.performance.scenarios;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.registries.ClerkRegistry;

import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.scenario;

public class PatientAppointmentCreationScenario extends Scenario<ClerkRegistry> {

	public PatientAppointmentCreationScenario(float scenarioLoadShare) {
		super(scenarioLoadShare, new ClerkRegistry());
	}

	@Override
	public ScenarioBuilder getScenarioBuilder() {
		FeederBuilder<String> patientUuidFeeder = csv("patient_uuids.csv").circular();
		// @formatter:off
        return  scenario("Clerk - Appointment creation Scenario").feed(patientUuidFeeder)
                .exec(registry.login())
                .exec(registry.openHomePage())
                .pause(3)
				.exec(registry.searchPatient())
                .exec(registry.openAppointmentPage())
				.exec(registry.openAppointmentFormPage("#{patient_uuid}"))
				.exec(registry.createAppointment("#{patient_uuid}"));
        // @formatter:on
	}
}
