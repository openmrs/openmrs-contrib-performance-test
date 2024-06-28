package org.openmrs.performance.personas;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;

import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static org.openmrs.performance.registries.ClerkRegistry.registerPatient;
import static org.openmrs.performance.registries.CommonRegistry.*;
import static org.openmrs.performance.registries.DoctorRegistry.*;

public class Doctor {
	
	public static FeederBuilder<String> patientUuidFeeder = csv("patient_uuids.csv").circular();
	public static ScenarioBuilder doctorScenario = scenario("Doctor")
			.feed(patientUuidFeeder)
			.exec(login())
			.exec(openHomePage())
			.exec(openPatientChartPage("#{patient_uuid}"))
			.exec(startVisit("#{patient_uuid}"))
			.exec(reviewVitalsAndBiometrics("#{patient_uuid}"))
			.exec(reviewMedications("#{patient_uuid}"))
			.exec(reviewOrders("#{patient_uuid}"))
//			.exec(reviewLabResults("#{patient_uuid}"))
			.exec(reviewAllergies("#{patient_uuid}"))
			.exec(reviewConditions("#{patient_uuid}"))
//			.exec(reviewImmunizations("#{patient_uuid}"))
			.exec(reviewAttachments("#{patient_uuid}"))
			.exec(endVisit("#{patient_uuid}"));
}
