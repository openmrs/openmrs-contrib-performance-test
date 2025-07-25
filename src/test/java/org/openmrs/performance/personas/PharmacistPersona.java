package org.openmrs.performance.personas;

import org.openmrs.performance.registries.PharmacistRegistry;
import org.openmrs.performance.scenarios.PatientMedicationDispenseScenario;
import org.openmrs.performance.scenarios.Scenario;

import java.util.List;

public class PharmacistPersona extends Persona<PharmacistRegistry> {

	public PharmacistPersona(double loadShare) {
		super(loadShare);
	}

	@Override
	public List<Scenario<PharmacistRegistry>> getScenarios() {
		return List.of(new PatientMedicationDispenseScenario(1));
	}
}
