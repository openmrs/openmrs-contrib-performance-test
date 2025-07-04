package org.openmrs.performance.scenarios;

import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.registries.ClerkRegistry;

import static io.gatling.javaapi.core.CoreDsl.scenario;

public class PatientListCreationScenario extends Scenario<ClerkRegistry> {

	public PatientListCreationScenario(float loadShare) {
		super(loadShare, new ClerkRegistry());
	}

	@Override
	public ScenarioBuilder getScenarioBuilder() {
		// @formatter:off
		return scenario("Clerk - Patient List creation")
                .exec(registry.login())
                .exec(registry.openHomePage())
                .pause(3)
		        .exec(registry.openPatientLists())
                .pause(3)
                .exec(registry.createNewPatientList())
                .pause(3)
		        .exec(registry.openSpecificPatientList("#{patientListUuid}"));
        // @formatter:on
	}
}
