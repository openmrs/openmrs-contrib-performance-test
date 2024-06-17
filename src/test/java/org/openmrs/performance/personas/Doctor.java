package org.openmrs.performance.personas;

import io.gatling.javaapi.core.ScenarioBuilder;

import static io.gatling.javaapi.core.CoreDsl.scenario;
import static org.openmrs.performance.registries.CommonRegistry.login;

public class Doctor {
	public static ScenarioBuilder doctorScenario = scenario("Doctor")
			.exec(login());
}
