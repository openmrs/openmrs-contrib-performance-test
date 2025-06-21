package org.openmrs.performance.personas;

import org.openmrs.performance.registries.LabTechRegistry;
import org.openmrs.performance.scenarios.PatientLabScenario;
import org.openmrs.performance.scenarios.Scenario;

import java.util.List;

public class LabTechPersona extends Persona<LabTechRegistry> {

	public LabTechPersona(double loadShare) {
		super(loadShare);
	}

	@Override
	public List<Scenario<LabTechRegistry>> getScenarios() {
		return List.of(new PatientLabScenario(1));
	}
}
