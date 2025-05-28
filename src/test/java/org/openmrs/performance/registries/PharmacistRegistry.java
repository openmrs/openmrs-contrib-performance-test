package org.openmrs.performance.registries;

import org.openmrs.performance.http.PharmacistHttpService;

public class PharmacistRegistry extends Registry<PharmacistHttpService> {

	public PharmacistRegistry() {
		super(new PharmacistHttpService());
	}
}
