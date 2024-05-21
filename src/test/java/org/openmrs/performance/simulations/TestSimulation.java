package org.openmrs.performance.simulations;

import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.personas.Clerk.clerkScenario;
import static org.openmrs.performance.personas.Doctor.doctorScenario;



public class TestSimulation extends Simulation {
	
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
		// Ramp users from 0 to 40 in 60 s and keep concurent 40 users for 1 minute
		setUp(
				clerkScenario.injectOpen(
						atOnceUsers(10), // 2
						rampUsers(10).during(5), // 3
						constantUsersPerSec(20).during(15),
						stressPeakUsers(30).during(60)
				),
				doctorScenario.injectOpen(
						rampUsers(5).during(10)            // Ramp up to 1 user in 10 seconds
				)
		).protocols(httpProtocol);
	}
}
