package org.openmrs.performance.scenarios;

import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.registries.NurseRegistry;

import static io.gatling.javaapi.core.CoreDsl.scenario;

public class PatientWardAdmissionScenario extends Scenario<NurseRegistry> {

	public PatientWardAdmissionScenario(float loadShare) {
		super(loadShare, new NurseRegistry());
	}

	@Override
	public ScenarioBuilder getScenarioBuilder() {
		// @formatter:off
        return scenario("Nurse - Patient Ward Admission")
                .exec(registry.login())
                .exec(registry.openHomePage())
                .pause(3)
                .exec(registry.openWardPage());
        // @formatter:on
	}
}
