package org.openmrs.performance.registries;

import io.gatling.javaapi.core.ChainBuilder;
import org.openmrs.performance.http.ClerkHttpService;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.pause;
import static org.openmrs.performance.Constants.*;

public class ClerkRegistry extends Registry<ClerkHttpService> {
	
	public ClerkRegistry() {
		super(new ClerkHttpService());
	}
	
	public ChainBuilder openRegistrationPage() {
		return exec(
				httpService.getAddressTemplate(),
				httpService.getPatientIdentifierTypes(),
				httpService.getPrimaryIdentifierTermMapping(),
				httpService.getRelationshipTypes(),
				httpService.getModuleInformation(),
				httpService.getPersonAttributeType("14d4f066-15f5-102d-96e4-000c29c2a5d7"),
				httpService.getAutoGenerationOptions(),
				httpService.getOrderedAddressHierarchyLevels(),
				httpService.getIdentifierSources(ID_CARD_SOURCE_UUID),
				httpService.getIdentifierSources(OPENMRS_ID_SOURCE_UUID),
				httpService.getIdentifierSources(UNKNOWN_TYPE_SOURCE_UUID),
				httpService.getIdentifierSources(LEGACY_ID_SOURCE_UUID),
				httpService.getIdentifierSources(SSN_SOURCE_UUID),
				httpService.getIdentifierSources(UNKNOWN_TYPE_2_SOURCE_UUID));
	}
	
	public ChainBuilder registerPatient() {
		return exec(
				httpService.generateOMRSIdentifier(),
				httpService.sendPatientRegistrationRequest(),
				pause(2));
	}
	
	
	
}
