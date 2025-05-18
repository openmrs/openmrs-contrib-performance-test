package org.openmrs.performance.registries;

import io.gatling.javaapi.core.ChainBuilder;
import org.openmrs.performance.http.ClerkHttpService;
import org.openmrs.performance.http.get.ClinicDetailsHttpService;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.pause;
import static org.openmrs.performance.Constants.*;

public class ClerkRegistry extends Registry<ClerkHttpService> {

	public ClerkRegistry() {
		super(new ClerkHttpService());
	}

	public ChainBuilder openRegistrationPage() {
		return exec(ClinicDetailsHttpService.getAddressTemplate(), ClinicDetailsHttpService.getPatientIdentifierTypes(),
				ClinicDetailsHttpService.getPrimaryIdentifierTermMapping(), ClinicDetailsHttpService.getRelationshipTypes(),
				ClinicDetailsHttpService.getModuleInformation(), ClinicDetailsHttpService.getPersonAttributeType(PERSON_ATTRIBUTE_PHONE_NUMBER),
				ClinicDetailsHttpService.getAutoGenerationOptions(), ClinicDetailsHttpService.getOrderedAddressHierarchyLevels(),
				ClinicDetailsHttpService.getIdentifierSources());
	}

	public ChainBuilder registerPatient() {
		return exec(httpService.generateOMRSIdentifier(), httpService.sendPatientRegistrationRequest(), pause(2));
	}

}
