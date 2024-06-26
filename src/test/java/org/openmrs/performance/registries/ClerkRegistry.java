package org.openmrs.performance.registries;

import io.gatling.javaapi.core.ChainBuilder;

import java.util.Set;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.pause;
import static org.openmrs.performance.Constants.*;
import static org.openmrs.performance.http.ClerkHttpRequests.*;
import static org.openmrs.performance.http.CommonHttpRequests.*;

public class ClerkRegistry {
	
	public static ChainBuilder openRegistrationPage() {
		return exec(
				getAddressTemplate(),
				getPatientIdentifierTypes(),
				getPrimaryIdentifierTermMapping(),
				getRelationshipTypes(),
				getModuleInformation(),
				getPersonAttributeType("14d4f066-15f5-102d-96e4-000c29c2a5d7"),
				getAutoGenerationOptions(),
				getOrderedAddressHierarchyLevels(),
				getIdentifierSources(ID_CARD_SOURCE_UUID),
				getIdentifierSources(OPENMRS_ID_SOURCE_UUID),
				getIdentifierSources(UNKNOWN_TYPE_SOURCE_UUID),
				getIdentifierSources(LEGACY_ID_SOURCE_UUID),
				getIdentifierSources(SSN_SOURCE_UUID),
				getIdentifierSources(UNKNOWN_TYPE_2_SOURCE_UUID));
	}
	
	public static ChainBuilder registerPatient() {
		return exec(
				generateOMRSIdentifier(),
				sendPatientRegistrationRequest(),
				pause(2));
	}
	
	
	
}
