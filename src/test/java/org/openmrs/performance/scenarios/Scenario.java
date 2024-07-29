package org.openmrs.performance.scenarios;

import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.registries.Registry;

public abstract class Scenario<T extends Registry<?>> {
	public float loadShare;
	public T registry;
	public Scenario(float loadShare, T registry) {
		this.loadShare = loadShare;
		this.registry = registry;
	}
	
	public abstract ScenarioBuilder getScenarioBuilder();
}
