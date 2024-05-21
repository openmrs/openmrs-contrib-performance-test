package org.openmrs.performance.simulations;

import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
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
		setUp(
				clerkScenario.injectOpen(
						rampUsers(100).during(60),         // Ramp up to 200 users in 1 minute
						constantUsersPerSec(100).during(120) // Maintain 100 users for 2 minutes
				),
				doctorScenario.injectOpen(
						rampUsers(5).during(10)            // Ramp up to 1 user in 10 seconds
				)
		).protocols(httpProtocol);
	}
}
