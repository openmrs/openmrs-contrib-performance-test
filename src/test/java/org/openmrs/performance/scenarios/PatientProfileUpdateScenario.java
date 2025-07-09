package org.openmrs.performance.scenarios;

import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.registries.ClerkRegistry;
import org.openmrs.performance.utils.SharedPoolFeeder;

import java.util.Iterator;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.scenario;

public class PatientProfileUpdateScenario extends Scenario<ClerkRegistry> {

	public PatientProfileUpdateScenario(float loadShare) {
		super(loadShare, new ClerkRegistry());
	}

	@Override
	public ScenarioBuilder getScenarioBuilder() {
		Iterator<Map<String, Object>> patientUuidFeeder = SharedPoolFeeder.feeder();

		// @formatter:off
        return scenario("Clerk - Patient Profile Update").feed(patientUuidFeeder)
                .exec(registry.login())
                .exec(registry.openHomePage())
                .exec(registry.openPatientChartPage("#{patient_uuid}"))
                .pause(5)
                .exec(registry.openEditPatientTab("#{patient_uuid}"))
                .pause(5)
                .exec(registry.editPatientDetails("#{patient_uuid}"))
                .exec(session -> {
                    String uuid = session.getString("patient_uuid");
                    SharedPoolFeeder.returnUuid(uuid);
                    return session;
                });
        // @formatter:on
	}
}
