package org.openmrs.performance.personas;

import org.openmrs.performance.registries.DoctorRegistry;
import org.openmrs.performance.scenarios.Scenario;
import org.openmrs.performance.scenarios.PatientVisitScenario;

import java.util.List;
import java.util.Map;

import static org.openmrs.performance.utils.LoadConfigUtils.getScenarioLoads;

public class DoctorPersona extends Persona<DoctorRegistry> {

	public DoctorPersona(double loadShare) {
		super(loadShare);
	}

	@Override
	public List<Scenario<DoctorRegistry>> getScenarios() {
		Map<String, Float> scenarioLoads = getScenarioLoads("doctor");
		return List.of(new PatientVisitScenario(scenarioLoads.get("patientVisitScenario")));
	}
}
