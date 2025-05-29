package org.openmrs.performance.registries;

import org.openmrs.performance.http.LabTechHttpService;

public class LabTechRegistry extends Registry<LabTechHttpService> {

	public LabTechRegistry() {
		super(new LabTechHttpService());
	}
}
