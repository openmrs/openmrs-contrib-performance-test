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
		return exec(httpService.getAddressTemplate(), httpService.getPatientIdentifierTypes(),
		    httpService.getPrimaryIdentifierTermMapping(), httpService.getRelationshipTypes(),
		    httpService.getModuleInformation(), httpService.getPersonAttributeType(PERSON_ATTRIBUTE_PHONE_NUMBER),
		    httpService.getAutoGenerationOptions(), httpService.getOrderedAddressHierarchyLevels(),
		    httpService.getIdentifierSources());
	}

	public ChainBuilder registerPatient() {
		return exec(httpService.generateOMRSIdentifier(), httpService.sendPatientRegistrationRequest(), pause(2));
	}

}
