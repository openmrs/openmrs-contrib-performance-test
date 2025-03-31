package org.openmrs.performance.personas;

import org.openmrs.performance.registries.ClerkRegistry;
import org.openmrs.performance.scenarios.PatientRegistrationScenario;
import org.openmrs.performance.scenarios.Scenario;

import java.util.List;

public class ClerkPersona extends Persona<ClerkRegistry> {

	public ClerkPersona(double loadShare) {
		super(loadShare);
	}

	@Override
	public List<Scenario<ClerkRegistry>> getScenarios() {
		return List.of(new PatientRegistrationScenario(1));
	}
}
