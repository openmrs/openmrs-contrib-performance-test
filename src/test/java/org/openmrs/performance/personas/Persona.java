package org.openmrs.performance.personas;

import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;

import java.util.List;

public abstract class Persona {
	public double loadShare;
	
	public Persona(double loadShare) {
		this.loadShare = loadShare;
	}
	
	public abstract List<ScenarioBuilder> scenarios();
}
