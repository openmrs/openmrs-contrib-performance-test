package org.openmrs.performance.personas;

import org.openmrs.performance.registries.PharmacistRegistry;
import org.openmrs.performance.scenarios.PatientMedicationDispenseScenario;
import org.openmrs.performance.scenarios.Scenario;
import org.openmrs.performance.utils.LoadConfigUtils;

import java.util.List;
import java.util.Map;

public class PharmacistPersona extends Persona<PharmacistRegistry> {

	public PharmacistPersona(double loadShare) {
		super(loadShare);
	}

	@Override
	public List<Scenario<PharmacistRegistry>> getScenarios() {
		Map<String, Float> scenarioLoads = LoadConfigUtils.getScenarioLoads("pharmacist");
		return List.of(new PatientMedicationDispenseScenario(scenarioLoads.get("patientMedicationDispense")));
	}
}
