package org.openmrs.performance.registries;

import org.openmrs.performance.http.NurseHttpService;

public class NurseRegistry extends Registry<NurseHttpService> {

	public NurseRegistry() {
		super(new NurseHttpService());
	}
}
