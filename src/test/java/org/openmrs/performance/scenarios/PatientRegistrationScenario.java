package org.openmrs.performance.scenarios;

import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.registries.ClerkRegistry;

import static io.gatling.javaapi.core.CoreDsl.scenario;

public class PatientRegistrationScenario extends Scenario<ClerkRegistry> {

	public PatientRegistrationScenario(float scenarioLoadShare) {
		super(scenarioLoadShare, new ClerkRegistry());
	}

	@Override
	public ScenarioBuilder getScenarioBuilder() {
		return scenario("Clerk - Patient Registration").exec(registry.login()).exec(registry.openHomePage()).pause(3)
		        .exec(registry.openRegistrationPage()).pause(10).exec(registry.registerPatient())
		        // redirect to patient chart page
		        .exec(registry.openPatientChartPage("#{patientUuid}"));
	}

}
