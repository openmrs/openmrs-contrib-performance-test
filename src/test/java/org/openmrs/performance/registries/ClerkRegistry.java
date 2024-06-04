package org.openmrs.performance.registries;

import io.gatling.javaapi.core.ChainBuilder;

import java.util.Set;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.pause;
import static org.openmrs.performance.http.ClerkHttpRequests.*;

public class ClerkRegistry {
	
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
