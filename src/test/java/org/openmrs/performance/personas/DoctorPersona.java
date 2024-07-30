package org.openmrs.performance.personas;

import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.scenarios.Scenario;
import org.openmrs.performance.scenarios.VisitPatientScenario;

import java.util.List;

public class DoctorPersona extends Persona{
	
	public DoctorPersona(double loadShare) {
		super(loadShare);
	}
	
	@Override
	public List<Scenario> getScenarios() {
		return List.of(
			new VisitPatientScenario(1)
		);
	}
}
