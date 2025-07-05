package org.openmrs.performance.scenarios;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.registries.ClerkRegistry;
import org.openmrs.performance.utils.SharedPoolFeeder;

import java.util.Iterator;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.scenario;

public class PatientAppointmentCreationScenario extends Scenario<ClerkRegistry> {

	public PatientAppointmentCreationScenario(float scenarioLoadShare) {
		super(scenarioLoadShare, new ClerkRegistry());
	}

	@Override
	public ScenarioBuilder getScenarioBuilder() {
		Iterator<Map<String, Object>> patientUuidFeeder = SharedPoolFeeder.feeder();
		// @formatter:off
		return  scenario("Clerk - Appointment creation Scenario").feed(patientUuidFeeder)
				.exec(registry.login())
				.exec(registry.openHomePage())
				.pause(3)
				.exec(registry.searchPatient())
				.exec(registry.openAppointmentPage())
				.pause(3)
				.exec(registry.openAppointmentFormPage("#{patient_uuid}"))
				.pause(5)
				.exec(registry.createAppointment())
				.pause(5)
				.exec(registry.checkInPatient("#{patient_uuid}"))
				.pause(10)
				.exec(registry.checkOutPatient())
				.exec(registry.openAppointmentPage())
				.exec(session -> {
					String uuid = session.getString("patient_uuid");
					SharedPoolFeeder.returnUuid(uuid);
					return session;
				});
		// @formatter:on
	}
}
