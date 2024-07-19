package org.openmrs.performance.simulations;

import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.personas.Clerk.clerkScenario;
import static org.openmrs.performance.personas.Doctor.doctorScenario;



public class OpenMRSClinic extends Simulation {
	
	HttpProtocolBuilder httpProtocol =
			http.baseUrl("https://dev3.openmrs.org")
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
						rampConcurrentUsers(0).to(1).during(5)
//						constantConcurrentUsers(100).during(60)
				)
//				doctorScenario.injectClosed(
//						rampConcurrentUsers(0).to(100).during(20),
//						constantConcurrentUsers(100).during(60)
//				)
		).protocols(httpProtocol);
	}
}
