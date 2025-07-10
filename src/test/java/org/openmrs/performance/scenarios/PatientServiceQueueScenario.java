package org.openmrs.performance.scenarios;

import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.registries.ClerkRegistry;
import org.openmrs.performance.utils.SharedPoolFeeder;

import java.util.Iterator;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.scenario;

public class PatientServiceQueueScenario extends Scenario<ClerkRegistry> {

	public PatientServiceQueueScenario(float scenarioLoadShare) {
		super(scenarioLoadShare, new ClerkRegistry());
	}

	@Override
	public ScenarioBuilder getScenarioBuilder() {
		Iterator<Map<String, Object>> patientUuidFeeder = SharedPoolFeeder.feeder();
		// @formatter:off
        return  scenario("Clerk - Patient Service Scenario").feed(patientUuidFeeder)
                .exec(registry.login())
                .exec(registry.openHomePage())
                .pause(3)
                .exec(registry.searchPatient())
                .pause(3)
                .exec(registry.addPatientToQueue("#{patient_uuid}"))
                .pause(3)
                .exec(registry.addNewServiceQueue())
                .pause(3)
                .exec(registry.transitionPatient())
                .exec(registry.endVisit("#{patient_uuid}"))
                .exec(session -> {
                    String uuid = session.getString("patient_uuid");
                    SharedPoolFeeder.returnUuid(uuid);
                    return session;
                });
        // @formatter:on
	}
}
