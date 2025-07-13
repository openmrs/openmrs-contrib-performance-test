package org.openmrs.performance.scenarios;

import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.registries.PharmacistRegistry;
import org.openmrs.performance.utils.SharedPoolFeeder;

import java.util.Iterator;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.scenario;

public class PatientMedicationDispenseScenario extends Scenario<PharmacistRegistry> {

	public PatientMedicationDispenseScenario(float loadShare) {
		super(loadShare, new PharmacistRegistry());
	}

	@Override
	public ScenarioBuilder getScenarioBuilder() {
		Iterator<Map<String, Object>> patientUuidFeeder = SharedPoolFeeder.feeder();
		// @formatter:off
		return  scenario("Pharmacist - Dispensing medication Scenario").feed(patientUuidFeeder)
				.exec(registry.login())
				.exec(registry.openHomePage())
				.pause(3)
				.exec(registry.startVisit("#{patient_uuid}"))
				.pause(3)
				.exec(registry.openOrdersTab("#{patient_uuid}"))
				.pause(5)
				.exec(registry.addDrugOrder("#{patient_uuid}"))
				.pause(5)
				.exec(registry.openDispensingApp())
				.pause(3)
				.exec(registry.selectPatientWithPrescription("#{patient_uuid}"))
				.pause(3)
				.exec(registry.openDispensePrescription("#{patient_uuid}"))
				.pause(5)
				.exec(registry.dispenseMedication())
				.pause(3)
				.exec(registry.openClosePrescription())
				.pause(3)
				.exec(registry.closeMedication())
				.exec(registry.discontinueDrugOrder())
				.exec(registry.endVisit("#{patient_uuid}"))
				.exec(session -> {
					String uuid = session.getString("patient_uuid");
					SharedPoolFeeder.returnUuid(uuid);
					return session;
				});
		// @formatter:on
	}
}
