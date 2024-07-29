package org.openmrs.performance.personas;

import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.scenarios.PatientRegistrationScenario;

import java.util.List;

public class ClerkPersona extends Persona {
	
	public ClerkPersona(double loadShare) {
		super(loadShare);
	}
	
	@Override
	public List<ScenarioBuilder> scenarios() {
		return List.of(
				new PatientRegistrationScenario(1).getScenarioBuilder()
		);
	}
}
