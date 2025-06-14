package org.openmrs.performance.registries;

import io.gatling.javaapi.core.ChainBuilder;
import org.openmrs.performance.http.NurseHttpService;

import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.foreach;

public class NurseRegistry extends Registry<NurseHttpService> {

	public NurseRegistry() {
		super(new NurseHttpService());
	}

	public ChainBuilder openWardPage() {
		return exec(httpService.getAdmissionLocationInfo(), httpService.getAdmittedPatientInfo(),
		    httpService.getInpatientRequest()).doIf(session -> session.contains("admittedPatientUuid"))
		        .then(foreach("#admittedPatientUuid", "uuid")
		                .on(exec(httpService.getOrdersWithNullFulfillerStatusAndActivatedDate("#{uuid}"))));
	}
}
