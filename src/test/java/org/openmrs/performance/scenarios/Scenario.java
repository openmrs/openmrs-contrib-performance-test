package org.openmrs.performance.scenarios;

import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.registries.Registry;

public abstract class Scenario<R extends Registry<?>> {

	public float scenarioLoadShare;

	public R registry;

	public Scenario(float scenarioLoadShare, R registry) {
		this.scenarioLoadShare = scenarioLoadShare;
		this.registry = registry;
	}

	public abstract ScenarioBuilder getScenarioBuilder();
}
