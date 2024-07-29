package org.openmrs.performance.simulations;

import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import org.openmrs.performance.TrafficConfiguration;
import org.openmrs.performance.personas.ClerkPersona;
import org.openmrs.performance.personas.DoctorPersona;
import org.openmrs.performance.personas.Persona;

import java.util.ArrayList;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class OpenMRSClinic extends Simulation {
	
	private static final TrafficConfiguration trafficConfiguration = TrafficConfiguration.getInstance();
	
	private static final int duration = trafficConfiguration.getDuration();
	
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
		
		Persona clerkPersona = new ClerkPersona(0.5);
		Persona doctorPersona = new DoctorPersona(0.5);
		
		int clerkCount = (int) Math.ceil(trafficConfiguration.getTotalActiveUserCount() * clerkPersona.loadShare);
		int doctorCount = trafficConfiguration.getTotalActiveUserCount() - clerkCount;
		
		List<PopulationBuilder> populations = new ArrayList<>();
		
		clerkPersona.scenarios().forEach(
				scenario -> populations.add(scenario.injectClosed(
						rampConcurrentUsers(0).to(clerkCount)
								.during(60),
						constantConcurrentUsers(clerkCount)
								.during(trafficConfiguration.getDuration())
				))
		);
		
		doctorPersona.scenarios().forEach(
				scenario -> populations.add(scenario.injectClosed(
						rampConcurrentUsers(0).to(doctorCount)
								.during(60),
						constantConcurrentUsers(doctorCount)
								.during(trafficConfiguration.getDuration())
				))
		);
		
		setUp(populations).protocols(httpProtocol);
		
	}
}
