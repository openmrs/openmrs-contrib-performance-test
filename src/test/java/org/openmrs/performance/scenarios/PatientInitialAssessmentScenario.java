package org.openmrs.performance.scenarios;

import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.registries.NurseRegistry;
import org.openmrs.performance.utils.SharedPoolFeeder;

import java.util.Iterator;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.scenario;

public class PatientInitialAssessmentScenario extends Scenario<NurseRegistry> {

	public PatientInitialAssessmentScenario(float loadShare) {
		super(loadShare, new NurseRegistry());
	}

	@Override
	public ScenarioBuilder getScenarioBuilder() {
		Iterator<Map<String, Object>> patientUuidFeeder = SharedPoolFeeder.feeder();

		// @formatter:off
        return scenario("Nurse - Patient Initial Assessment").feed(patientUuidFeeder)
                .exec(registry.login())
                .exec(registry.openHomePage())
                .exec(registry.openPatientChartPage("#{patient_uuid}"))
                .pause(5)
                .exec(registry.openVitalsAndBiometricsTab("#{patient_uuid}"))
                .pause(5)
                .exec(registry.recordVitals("#{patient_uuid}"))
                .pause(5)
                .exec(registry.openAllergiesTab("#{patient_uuid}"))
                .exec(registry.openAllergiesForm())
                .pause(5)
                .exec(registry.recordAllergy("#{patient_uuid}"))
                .exec(session -> {
                    String uuid = session.getString("patient_uuid");
                    SharedPoolFeeder.returnUuid(uuid);
                    return session;
                });
        // @formatter:on
	}
}
