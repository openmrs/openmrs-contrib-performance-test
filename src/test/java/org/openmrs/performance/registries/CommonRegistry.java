package org.openmrs.performance.registries;

import io.gatling.javaapi.core.ChainBuilder;

import static io.gatling.javaapi.core.CoreDsl.exec;

import static io.gatling.javaapi.core.CoreDsl.pause;
import static org.openmrs.performance.Constants.*;
import static org.openmrs.performance.http.CommonHttpRequests.*;
public class CommonRegistry {

	// login
	public static ChainBuilder login(){
		return exec(
			loginRequest(),
			pause(1),
			getLocations(),
			pause(5),
			selectLocation()
	);
	}
	
	public static ChainBuilder openHomePage() {
		return exec(
				getAddressTemplate(),
				getRelationshipTypes(),
				getAppointmentsForSpecificDate("2024-05-15T00:00:00.000+0530"),
				getModuleInformation(),
				getPatientIdentifierTypes(),
				getPrimaryIdentifierTermMapping(),
				getVisits(OUTPATIENT_CLINIC_LOCATION_UUID),
				getAutoGenerationOptions(),
				getIdentifierSources(ID_CARD_SOURCE_UUID),
				getIdentifierSources(OPENMRS_ID_SOURCE_UUID),
				getIdentifierSources(UNKNOWN_TYPE_SOURCE_UUID),
				getIdentifierSources(LEGACY_ID_SOURCE_UUID),
				getIdentifierSources(SSN_SOURCE_UUID),
				getIdentifierSources(UNKNOWN_TYPE_2_SOURCE_UUID)
		);
	}
}
