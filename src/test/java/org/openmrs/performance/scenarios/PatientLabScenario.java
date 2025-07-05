package org.openmrs.performance.scenarios;

import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.registries.LabTechRegistry;
import org.openmrs.performance.utils.SharedPoolFeeder;

import java.util.Iterator;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.scenario;

public class PatientLabScenario extends Scenario<LabTechRegistry> {

	public PatientLabScenario(float scenarioLoadShare) {
		super(scenarioLoadShare, new LabTechRegistry());
	}

	@Override
	public ScenarioBuilder getScenarioBuilder() {
		Iterator<Map<String, Object>> patientUuidFeeder = SharedPoolFeeder.feeder();
		// @formatter:off
		return scenario("Lab Tech - Lab report completion").feed(patientUuidFeeder).exec(registry.login())
				.exec(registry.openHomePage())
				.pause(5)
				.exec(registry.openPatientChartPage("#{patient_uuid}"))
				.pause(5)
				.exec(registry.startVisit("#{patient_uuid}"))
				.pause(5)
				.exec(registry.addLabOrder("#{patient_uuid}"))
				.pause(5)
				.exec(registry.loadLaboratory())
				.pause(5)
				.exec(registry.pickUpLabOrder())
				.exec(registry.loadLaboratory())
				.pause(5)
				.exec(registry.completeLabOrder("#{patient_uuid}"))
				.exec(registry.loadLaboratory())
				.pause(5)
				.exec(registry.endVisit("#{patient_uuid}"))
				.exec(session -> {
					String uuid = session.getString("patient_uuid");
					SharedPoolFeeder.returnUuid(uuid);
					return session;
				});
		// @formatter:on
	}
}
