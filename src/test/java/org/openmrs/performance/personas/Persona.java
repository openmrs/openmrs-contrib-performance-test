package org.openmrs.performance.personas;

import org.openmrs.performance.registries.Registry;
import org.openmrs.performance.scenarios.Scenario;

import java.util.List;

public abstract class Persona<R extends Registry<?>> {

	public double loadShare;

	public Persona(double loadShare) {
		this.loadShare = loadShare;
	}

	public abstract List<Scenario<R>> getScenarios();
}
