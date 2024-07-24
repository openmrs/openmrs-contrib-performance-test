package org.openmrs.performance.simulations;

import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import org.openmrs.performance.TrafficConfiguration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.personas.Clerk.clerkScenario;
import static org.openmrs.performance.personas.Doctor.doctorScenario;



public class OpenMRSClinic extends Simulation {
	
	private static final TrafficConfiguration trafficConfiguration = TrafficConfiguration.getInstance();
	
	HttpProtocolBuilder httpProtocol =
			http.baseUrl("http://localhost")
					.acceptHeader("application/json, text/plain, */*")
					.acceptLanguageHeader("en-US,en;q=0.5")
					.userAgentHeader(
							"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/119.0"
					)
					.header("Authorization", "Bearer YWRtaW46QWRtaW4xMjM=")
					.header("Content-Type", "application/json");
	
	{
		setUp(
				clerkScenario.injectClosed(
						rampConcurrentUsers(0).to(trafficConfiguration.getActiveDoctorCount())
								.during(60),
						constantConcurrentUsers(trafficConfiguration.getActiveDoctorCount())
								.during(trafficConfiguration.getDuration())
				),
				doctorScenario.injectClosed(
						rampConcurrentUsers(0).to(trafficConfiguration.getActiveDoctorCount())
								.during(60),
						constantConcurrentUsers(trafficConfiguration.getActiveClerkCount())
								.during(trafficConfiguration.getDuration())
				)
		).protocols(httpProtocol);
	}
}
