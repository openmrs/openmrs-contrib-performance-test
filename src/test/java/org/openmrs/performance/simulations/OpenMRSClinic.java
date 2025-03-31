package org.openmrs.performance.simulations;

import io.gatling.javaapi.core.ClosedInjectionStep;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import org.openmrs.performance.personas.ClerkPersona;
import org.openmrs.performance.personas.DoctorPersona;
import org.openmrs.performance.personas.Persona;
import org.openmrs.performance.scenarios.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.rampConcurrentUsers;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.Constants.ENV_SIMULATION_PRESET;
import static org.openmrs.performance.Constants.ENV_TIER_COUNT;
import static org.openmrs.performance.Constants.ENV_TIER_DURATION;
import static org.openmrs.performance.Constants.ENV_USER_INCREMENT_PER_TIER;

public class OpenMRSClinic extends Simulation {

	private static final Logger logger = LoggerFactory.getLogger(OpenMRSClinic.class.getName());

	{
		Map<String, Map<String, Integer>> presetsMap = Map.of("standard",
		    Map.of("tierCount", 6, "tierDurationMinutes", 30, "userIncrementPerTier", 32), "commit",
		    Map.of("tierCount", 1, "tierDurationMinutes", 1, "userIncrementPerTier", 20), "pull_request",
		    Map.of("tierCount", 1, "tierDurationMinutes", 1, "userIncrementPerTier", 20), "dev",
		    Map.of("tierCount", Integer.parseInt(System.getenv().getOrDefault(ENV_TIER_COUNT, "1")), "tierDurationMinutes",
		        Integer.parseInt(System.getenv().getOrDefault(ENV_TIER_DURATION, "1")), "userIncrementPerTier",
		        Integer.parseInt(System.getenv().getOrDefault(ENV_USER_INCREMENT_PER_TIER, "10"))));

		String preset = System.getenv(ENV_SIMULATION_PRESET);

		int[] loadParams = getSimulationParameters(preset, presetsMap);

		int userIncrementPerTier = loadParams[0];
		int tierDurationMinutes = loadParams[1];
		int tierCount = loadParams[2];

		HttpProtocolBuilder httpProtocol = getHttpProtocol();

		logger.info("Setting up simulation with preset: {} user increment per tier: {}, tier duration: {}, tier count: {}",
		    preset, userIncrementPerTier, tierDurationMinutes, tierCount);

		List<Persona<?>> personas = List.of(new ClerkPersona(0.5), new DoctorPersona(0.5));

		List<PopulationBuilder> populations = buildPopulations(personas, userIncrementPerTier, tierDurationMinutes,
		    tierCount);

		setUp(populations).protocols(httpProtocol).assertions(global().successfulRequests().percent().shouldBe(100.0));

	}

	private int[] getSimulationParameters(String preset, Map<String, Map<String, Integer>> loadSimulationTypeMap) {
		int userIncrementPerTier;
		int tierDurationMinutes;
		int tierCount;

		if (preset == null) {
			throw new IllegalArgumentException(ENV_SIMULATION_PRESET + " variable is not set");
		}

		if (loadSimulationTypeMap.containsKey(preset)) {
			Map<String, Integer> loadSimulationType = loadSimulationTypeMap.get(preset);
			userIncrementPerTier = loadSimulationType.get("userIncrementPerTier");
			tierDurationMinutes = loadSimulationType.get("tierDurationMinutes");
			tierCount = loadSimulationType.get("tierCount");
		} else {
			throw new IllegalArgumentException(
			        "Invalid value for environment variable " + ENV_SIMULATION_PRESET + ": " + preset);
		}

		return new int[] { userIncrementPerTier, tierDurationMinutes, tierCount };
	}

	private List<PopulationBuilder> buildPopulations(List<Persona<?>> personas, int userIncrementPerTier,
	        int tierDurationMinutes, int tierCount) {
		List<PopulationBuilder> populations = new ArrayList<>();
		int rampDurationMinutes = 1;

		for (Persona<?> persona : personas) {
			int personaUserIncrementPerTier = (int) Math.ceil(userIncrementPerTier * persona.loadShare);
			logger.info("building persona: {}, user increment per tier: {}", persona.getClass().getSimpleName(),
			    personaUserIncrementPerTier);

			for (Scenario<?> scenario : persona.getScenarios()) {
				logger.info("\t building scenario: {}", scenario.getClass().getSimpleName());

				int userCount = 0;
				List<ClosedInjectionStep> steps = new ArrayList<>();

				for (int i = 0; i < tierCount; i++) {
					int startUserCount = userCount;
					int endUserCount = userCount + personaUserIncrementPerTier;

					ClosedInjectionStep rampPhase = rampConcurrentUsers(startUserCount).to(endUserCount)
					        .during(rampDurationMinutes * 60L);
					ClosedInjectionStep constantPhase = constantConcurrentUsers(endUserCount)
					        .during(tierDurationMinutes * 60L);
					// Inject both phases into the scenario
					steps.add(rampPhase);
					steps.add(constantPhase);

					logger.info(
					    "\t\t Tier: {}, Start Users: {}, End Users: {}, Ramp Duration: {} minutes, Constant Duration: {} minutes",
					    i + 1, startUserCount, endUserCount, rampDurationMinutes, tierDurationMinutes);

					// Update the user count for the next tier
					userCount = endUserCount;
				}
				populations.add(scenario.getScenarioBuilder().injectClosed(steps));
			}
		}

		return populations;
	}

	private HttpProtocolBuilder getHttpProtocol() {
		return http.baseUrl(org.openmrs.performance.Constants.BASE_URL).acceptHeader("application/json, text/plain, */*")
		        .acceptLanguageHeader("en-US,en;q=0.5")
		        .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/119.0")
		        .header("Authorization", "Bearer YWRtaW46QWRtaW4xMjM=").header("Content-Type", "application/json");
	}
}
