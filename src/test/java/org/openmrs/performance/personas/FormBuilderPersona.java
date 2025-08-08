package org.openmrs.performance.personas;

import org.openmrs.performance.registries.FormBuilderRegistry;
import org.openmrs.performance.scenarios.OpenFormBuilderTabScenario;
import org.openmrs.performance.scenarios.Scenario;

import java.util.List;

public class FormBuilderPersona extends Persona<FormBuilderRegistry> {

	public FormBuilderPersona(double loadShare) {
		super(loadShare);
	}

	@Override
	public List<Scenario<FormBuilderRegistry>> getScenarios() {
		return List.of(new OpenFormBuilderTabScenario(1));
	}
}
