package org.openmrs.performance.personas;

import org.openmrs.performance.registries.ClerkRegistry;
import org.openmrs.performance.scenarios.PatientAppointmentCreationScenario;
import org.openmrs.performance.scenarios.PatientListCreationScenario;
import org.openmrs.performance.scenarios.PatientProfileUpdateScenario;
import org.openmrs.performance.scenarios.PatientRegistrationScenario;
import org.openmrs.performance.scenarios.PatientServiceQueueScenario;
import org.openmrs.performance.scenarios.Scenario;

import java.util.List;
import java.util.Map;

import static org.openmrs.performance.utils.LoadConfigUtils.getScenarioLoads;

public class ClerkPersona extends Persona<ClerkRegistry> {

	public ClerkPersona(double loadShare) {
		super(loadShare);
	}

	@Override
	public List<Scenario<ClerkRegistry>> getScenarios() {
		Map<String, Float> scenarioLoads = getScenarioLoads("clerk");
		return List.of(new PatientRegistrationScenario(scenarioLoads.get("patientRegistration")),
		    new PatientAppointmentCreationScenario(scenarioLoads.get("patientAppointmentCreation")),
		    new PatientListCreationScenario(scenarioLoads.get("patientListCreation")),
		    new PatientProfileUpdateScenario(scenarioLoads.get("patientProfileUpdate")),
		    new PatientServiceQueueScenario(scenarioLoads.get("patientServiceQueue")));
	}
}
