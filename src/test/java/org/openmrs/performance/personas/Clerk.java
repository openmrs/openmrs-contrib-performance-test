package org.openmrs.performance.personas;

import io.gatling.javaapi.core.ScenarioBuilder;

import static io.gatling.javaapi.core.CoreDsl.scenario;
import static org.openmrs.performance.http.ClerkHttpRequests.openRegistrationPage;
import static org.openmrs.performance.http.ClerkHttpRequests.registerPatient;
import static org.openmrs.performance.http.CommonHttpRequests.login;
import static org.openmrs.performance.http.CommonHttpRequests.openHomePage;

public class Clerk {
	public static ScenarioBuilder clerkScenario = scenario("Clerk")
			.exec(login)
			.exec(openHomePage)
			.pause(3)
			.exec(openRegistrationPage)
			.pause(10)
			.exec(registerPatient);
	
}
