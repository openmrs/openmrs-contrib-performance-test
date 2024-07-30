package org.openmrs.performance.personas;

import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.scenarios.PatientRegistrationScenario;
import org.openmrs.performance.scenarios.Scenario;

import java.util.List;

public class ClerkPersona extends Persona {
	
	public ClerkPersona(double loadShare) {
		super(loadShare);
	}
	
	@Override
	public List<Scenario> getScenarios() {
		return List.of(
				new PatientRegistrationScenario(1)
		);
	}
}
