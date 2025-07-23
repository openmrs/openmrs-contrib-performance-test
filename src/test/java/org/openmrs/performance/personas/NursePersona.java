package org.openmrs.performance.personas;

import org.openmrs.performance.registries.NurseRegistry;
import org.openmrs.performance.scenarios.PatientInitialAssessmentScenario;
import org.openmrs.performance.scenarios.PatientWardAdmissionScenario;
import org.openmrs.performance.scenarios.Scenario;

import java.util.List;
import java.util.Map;

import static org.openmrs.performance.utils.LoadConfigUtils.getScenarioLoads;

public class NursePersona extends Persona<NurseRegistry> {

	public NursePersona(double loadShare) {
		super(loadShare);
	}

	@Override
	public List<Scenario<NurseRegistry>> getScenarios() {
		Map<String, Float> scenarioLoads = getScenarioLoads("nurse");
		return List.of(new PatientWardAdmissionScenario(scenarioLoads.get("patientWardAdmissionScenario")),
		    new PatientInitialAssessmentScenario(scenarioLoads.get("patientInitialAssessmentScenario")));
	}
}
