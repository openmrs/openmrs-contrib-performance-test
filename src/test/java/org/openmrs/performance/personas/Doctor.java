package org.openmrs.performance.personas;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;

import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.scenario;
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
			.exec(openVisitsTab("#{patient_uuid}"))
			.exec(openVitalsAndBiometricsTab("#{patient_uuid}"))
			.exec(openMedicationsTab("#{patient_uuid}"))
			.exec(openOrdersTab("#{patient_uuid}"))
			.exec(openLabResultsTab("#{patient_uuid}"))
			.exec(openAllergiesTab("#{patient_uuid}"))
			.exec(openConditionsTab("#{patient_uuid}"))
			.exec(openImmunizationsTab("#{patient_uuid}"))
			.exec(openAttachmentsTab("#{patient_uuid}"))
			.exec(endVisit("#{patient_uuid}"));
}
