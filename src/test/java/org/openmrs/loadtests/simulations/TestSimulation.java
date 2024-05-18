package org.openmrs.loadtests.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import static org.openmrs.loadtests.personas.Clerk.openRegistrationPage;
import static org.openmrs.loadtests.personas.Clerk.registerPatient;
import static org.openmrs.loadtests.personas.Common.login;
import static org.openmrs.loadtests.personas.Common.openHomePage;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

/**
 * Unit test for simple App.
 */
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
	
	ScenarioBuilder clerk = scenario("Clerk")
			.exec(login)
			.exec(openHomePage)
			.exec(openRegistrationPage)
			.exec(registerPatient);
	
	ScenarioBuilder doctor = scenario("Doctor")
			.exec(login);
	
	{
		setUp(clerk.injectOpen(rampUsers(1).during(10)),
				doctor.injectOpen(rampUsers(1).during(10))
		).protocols(httpProtocol);
	}
}
