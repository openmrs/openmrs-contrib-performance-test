package org.openmrs.performance.personas;

import org.openmrs.performance.registries.DoctorRegistry;
import org.openmrs.performance.scenarios.Scenario;
import org.openmrs.performance.scenarios.PatientVisitScenario;
import org.openmrs.performance.utils.LoadConfigUtils;

import java.util.List;
import java.util.Map;



public class DoctorPersona extends Persona<DoctorRegistry> {

	public DoctorPersona(double loadShare) {
		super(loadShare);
	}

	@Override
	public List<Scenario<DoctorRegistry>> getScenarios() {
		Map<String, Float> scenarioLoads = LoadConfigUtils.getScenarioLoads("doctor");
		return List.of(new PatientVisitScenario(scenarioLoads.get("patientVisit")));
	}
}
