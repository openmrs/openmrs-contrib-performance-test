package org.openmrs.performance.simulations;

import io.gatling.javaapi.core.ClosedInjectionStep;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import org.openmrs.performance.personas.ClerkPersona;
import org.openmrs.performance.personas.DoctorPersona;
import org.openmrs.performance.personas.FormBuilderPersona;
import org.openmrs.performance.personas.NursePersona;
import org.openmrs.performance.personas.LabTechPersona;

import org.openmrs.performance.personas.Persona;
import org.openmrs.performance.personas.PharmacistPersona;
import org.openmrs.performance.scenarios.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.forAll;
import static io.gatling.javaapi.core.CoreDsl.rampConcurrentUsers;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.Constants.ENV_SIMULATION_PRESET;
import static org.openmrs.performance.utils.LoadConfigUtils.getPersonaLoad;
import static org.openmrs.performance.utils.PresetConfigUtil.getPresetValues;

public class OpenMRSClinic extends Simulation {

	private static final Logger logger = LoggerFactory.getLogger(OpenMRSClinic.class.getName());

	{
		String preset = System.getenv(ENV_SIMULATION_PRESET);

		int[] loadParams = getPresetValues(preset);

		int userIncrementPerTier = loadParams[0];
		int tierDurationMinutes = loadParams[1];
		int tierCount = loadParams[2];

		HttpProtocolBuilder httpProtocol = getHttpProtocol();

		logger.info("Setting up simulation with preset: {} user increment per tier: {}, tier duration: {}, tier count: {}",
		    preset, userIncrementPerTier, tierDurationMinutes, tierCount);

		List<Persona<?>> personas = List.of(new ClerkPersona(getPersonaLoad("clerk")),
		    new DoctorPersona(getPersonaLoad("doctor")), new LabTechPersona(getPersonaLoad("labTech")),
		    new NursePersona(getPersonaLoad("nurse")), new PharmacistPersona(getPersonaLoad("pharmacist")),
		    new FormBuilderPersona(getPersonaLoad("formBuilder")));

		List<PopulationBuilder> populations = buildPopulations(personas, userIncrementPerTier, tierDurationMinutes,
		    tierCount);

		setUp(populations).protocols(httpProtocol).assertions(forAll().failedRequests().percent().lte(3.0));

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
				int scenarioUserIncrementPerTier = (int) Math.ceil(personaUserIncrementPerTier * scenario.scenarioLoadShare);
				logger.info("\t building scenario: {}, users increment for the scenario: {}",
				    scenario.getClass().getSimpleName(), scenarioUserIncrementPerTier);

				int userCount = 0;
				List<ClosedInjectionStep> steps = new ArrayList<>();

				for (int i = 0; i < tierCount; i++) {
					int startUserCount = userCount;
					int endUserCount = userCount + scenarioUserIncrementPerTier;

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
