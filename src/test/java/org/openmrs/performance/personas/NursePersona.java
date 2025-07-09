package org.openmrs.performance.personas;

import org.openmrs.performance.registries.NurseRegistry;
import org.openmrs.performance.scenarios.PatientInitialAssessmentScenario;
import org.openmrs.performance.scenarios.PatientWardAdmissionScenario;
import org.openmrs.performance.scenarios.Scenario;

import java.util.List;

public class NursePersona extends Persona<NurseRegistry> {

	public NursePersona(double loadShare) {
		super(loadShare);
	}

	@Override
	public List<Scenario<NurseRegistry>> getScenarios() {
		return List.of(new PatientWardAdmissionScenario(0.5F), new PatientInitialAssessmentScenario(0.5F));
	}

}
