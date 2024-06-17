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
	
	public static ChainBuilder openPatientChartPage(String patientUuid){
		Set<String> observationTypesSet1 = Set.of(
				"5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				"5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				"5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				"5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				"5092AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				"5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				"5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				"5242AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				"165095AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				"1343AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
		);
		
		Set<String> observationTypesSet2 = Set.of(
				"5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				"5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				"5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				"5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				"5092AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				"5242AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				"165095AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
		);
		
		Set<String> observationTypesSet3 = Set.of(
				"5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				"5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				"1343AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
		);
		return exec(
				getPatientSummaryData(patientUuid),
				getPatientVisits(patientUuid),
				getPatientObservations(patientUuid, observationTypesSet1),
				getPatientObservations(patientUuid, observationTypesSet2),
				getPatientObservations(patientUuid, observationTypesSet3),
				getVisitQueueEntry(patientUuid),
				getPatientConditions(patientUuid),
				getActiveOrders(patientUuid));
	}
	
}
