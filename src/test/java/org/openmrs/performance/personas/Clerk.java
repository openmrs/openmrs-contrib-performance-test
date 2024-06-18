package org.openmrs.performance.personas;

import io.gatling.javaapi.core.ScenarioBuilder;

import static io.gatling.javaapi.core.CoreDsl.scenario;
import static org.openmrs.performance.registries.ClerkRegistry.*;
import static org.openmrs.performance.registries.CommonRegistry.*;

public class Clerk {
	public static ScenarioBuilder clerkScenario = scenario("Clerk")
			.exec(login())
			.exec(openHomePage())
			.pause(3)
			.exec(openRegistrationPage())
			.pause(10)
			.exec(registerPatient())
			// redirect to patient chart page
			.exec(openPatientChartPage("#{patientUuid}"));
}
