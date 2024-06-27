package org.openmrs.performance.personas;

import io.gatling.javaapi.core.ScenarioBuilder;

import static io.gatling.javaapi.core.CoreDsl.scenario;
import static org.openmrs.performance.registries.ClerkRegistry.registerPatient;
import static org.openmrs.performance.registries.CommonRegistry.*;
import static org.openmrs.performance.registries.DoctorRegistry.*;

public class Doctor {
	
	private static final String PATIENT_UUID = "e2783cb2-d3fb-4713-bf87-f55b378759d9";
	public static ScenarioBuilder doctorScenario = scenario("Doctor")
			.exec(login())
			// Register a patient per doctor, which isn't ideal. should be changed to pre-registered patients
			.exec(registerPatient())
			.exec(openHomePage())
			.exec(openPatientChartPage("#{patientUuid}"))
			.exec(startVisit("#{patientUuid}"))
			.exec(reviewVitalsAndBiometrics("#{patientUuid}"))
			.exec(reviewMedications("#{patientUuid}"))
			.exec(reviewOrders("#{patientUuid}"))
//			.exec(reviewLabResults("ddd"))
//			.exec(reviewAllergies("ddd"))
//			.exec(reviewConditions("ddd"))
//			.exec(reviewImmunizations("ddd"))
//			.exec(reviewAttachments("ddd"))
			.exec(endVisit("#{patientUuid}"));
}
