package org.openmrs.performance.scenarios;

import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.registries.FormBuilderRegistry;
import org.openmrs.performance.utils.SharedPoolFeeder;

import java.util.Iterator;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.scenario;

public class OpenFormBuilderTabScenario extends Scenario<FormBuilderRegistry> {

	public OpenFormBuilderTabScenario(float scenarioLoadShare) {
		super(scenarioLoadShare, new FormBuilderRegistry());
	}

	@Override
	public ScenarioBuilder getScenarioBuilder() {
		Iterator<Map<String, Object>> patientUuidFeeder = SharedPoolFeeder.feeder();

		return scenario("Form Builder - View").exec(registry.login()).pause(2).exec(registry.openHomePage()).pause(2)
		        .exec(registry.openFormBuilderTab()).pause(2);
	}
}
