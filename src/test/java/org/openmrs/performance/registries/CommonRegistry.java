package org.openmrs.performance.registries;

import io.gatling.javaapi.core.ChainBuilder;

import java.util.Set;

import static io.gatling.javaapi.core.CoreDsl.exec;

import static io.gatling.javaapi.core.CoreDsl.pause;
import static org.openmrs.performance.Constants.*;
import static org.openmrs.performance.http.ClerkHttpRequests.*;
import static org.openmrs.performance.http.CommonHttpRequests.*;
public class CommonRegistry {

	// login
	public static ChainBuilder login(){
		return exec(
			loginRequest(),
			pause(1),
			getLocations(),
			pause(5),
			selectLocation());
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
				getCurrentVisit(patientUuid),
				getPatientObservations(patientUuid, observationTypesSet1),
				getPatientObservations(patientUuid, observationTypesSet2),
				getPatientObservations(patientUuid, observationTypesSet3),
				getVisitQueueEntry(patientUuid),
				getPatientConditions(patientUuid),
				getActiveOrders(patientUuid));
	}
}
