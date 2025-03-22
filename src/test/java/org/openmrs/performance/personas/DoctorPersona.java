package org.openmrs.performance.personas;

import org.openmrs.performance.registries.DoctorRegistry;
import org.openmrs.performance.scenarios.Scenario;
import org.openmrs.performance.scenarios.VisitPatientScenario;

import java.util.List;

public class DoctorPersona extends Persona<DoctorRegistry> {

	public DoctorPersona(double loadShare) {
		super(loadShare);
	}

	@Override
	public List<Scenario<DoctorRegistry>> getScenarios() {
		return List.of(new VisitPatientScenario(1));
	}
}
