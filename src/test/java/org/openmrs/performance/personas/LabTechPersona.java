package org.openmrs.performance.personas;

import org.openmrs.performance.registries.LabTechRegistry;
import org.openmrs.performance.scenarios.PatientLabOrderProcessingScenario;
import org.openmrs.performance.scenarios.Scenario;

import java.util.List;
import java.util.Map;

import static org.openmrs.performance.utils.LoadConfigUtils.getScenarioLoads;

public class LabTechPersona extends Persona<LabTechRegistry> {

	public LabTechPersona(double loadShare) {
		super(loadShare);
	}

	@Override
	public List<Scenario<LabTechRegistry>> getScenarios() {
		Map<String, Float> scenarioLoads = getScenarioLoads("labTech");
		return List.of(new PatientLabOrderProcessingScenario(scenarioLoads.get("patientLabOrderProcessing")));
	}
}
