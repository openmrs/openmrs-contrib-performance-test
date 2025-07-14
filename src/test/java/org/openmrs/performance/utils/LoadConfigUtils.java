package org.openmrs.performance.utils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

import java.util.HashMap;
import java.util.Map;

public class LoadConfigUtils {

	private static final Config config = ConfigFactory.parseResources("load-config.conf").resolve();

	public static double getPersonaLoad(String persona) {
		return config.getDouble("personas." + persona + ".load");
	}

	public static Map<String, Float> getScenarioLoads(String persona) {
		Config scenarioConfig = config.getConfig("personas." + persona + ".scenarios");

		Map<String, Float> scenarioMap = new HashMap<>();
		for (Map.Entry<String, ConfigValue> entry : scenarioConfig.entrySet()) {
			scenarioMap.put(entry.getKey(), ((Number) entry.getValue().unwrapped()).floatValue());
		}
		return scenarioMap;
	}
}
