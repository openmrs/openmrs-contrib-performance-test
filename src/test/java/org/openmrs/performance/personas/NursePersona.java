package org.openmrs.performance.personas;

import org.openmrs.performance.registries.NurseRegistry;
import org.openmrs.performance.scenarios.PatientInitialAssessmentScenario;
import org.openmrs.performance.scenarios.PatientWardAdmissionScenario;
import org.openmrs.performance.scenarios.Scenario;
import org.openmrs.performance.utils.LoadConfigUtils;

import java.util.List;
import java.util.Map;


public class NursePersona extends Persona<NurseRegistry> {

	public NursePersona(double loadShare) {
		super(loadShare);
	}

	@Override
	public List<Scenario<NurseRegistry>> getScenarios() {
		Map<String, Float> scenarioLoads = LoadConfigUtils.getScenarioLoads("nurse");
		return List.of(new PatientWardAdmissionScenario(scenarioLoads.get("patientWardAdmission")),
		    new PatientInitialAssessmentScenario(scenarioLoads.get("patientInitialAssessment")));
	}
}
