package org.openmrs.performance.scenarios;

import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.registries.NurseRegistry;
import org.openmrs.performance.utils.SharedPoolFeeder;

import java.util.Iterator;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.scenario;
import static org.openmrs.performance.Constants.INPATEINT_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.Constants.WARD1_CLINIC_LOCATION_UUID;

public class PatientWardAdmissionScenario extends Scenario<NurseRegistry> {

	public PatientWardAdmissionScenario(float loadShare) {
		super(loadShare, new NurseRegistry());
	}

	@Override
	public ScenarioBuilder getScenarioBuilder() {
		Iterator<Map<String, Object>> patientUuidFeeder = SharedPoolFeeder.feeder();
		// @formatter:off
        return scenario("Nurse - Patient Ward Admission").feed(patientUuidFeeder)
                .exec(registry.login())
                .exec(registry.openHomePage())
                .pause(3)
                .exec(registry.openWardPage(INPATEINT_CLINIC_LOCATION_UUID))
				.exec(registry.admitPatientToWardPage("#{patient_uuid}"))
				.pause(5)
				.exec(registry.openingPatientDetails("#{patient_uuid}"))
				.exec(registry.transferPatient())
				.pause(5)
				.exec(registry.openingPatientDetails("#{patient_uuid}"))
				.pause(3)
				.exec(registry.transferPatient())
				.pause(3)
				.exec(registry.moveToDifferentLocationSession())
				.exec(registry.openWardPage(WARD1_CLINIC_LOCATION_UUID))
				.pause(3)
				.exec(registry.admitTheTransferPatient())
				.pause(5)
				.exec(registry.dischargePatient());
        // @formatter:on
	}
}
