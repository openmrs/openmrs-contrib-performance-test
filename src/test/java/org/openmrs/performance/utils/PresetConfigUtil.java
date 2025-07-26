package org.openmrs.performance.utils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class PresetConfigUtil {

	private static final Config config = ConfigFactory.parseResources("simulation-config.conf").resolve();

	public static int[] getPresetValues(String preset) {
		if (preset == null || preset.isEmpty()) {
			throw new IllegalArgumentException("ENV_SIMULATION_PRESET variable is not set");
		}

		if (!config.hasPath("presets." + preset)) {
			throw new IllegalArgumentException("Invalid value for ENV_SIMULATION_PRESET: " + preset);
		}

		Config presetConfig = config.getConfig("presets." + preset);

		int userIncrementPerTier = presetConfig.getInt("userIncrementPerTier");
		int tierDurationMinutes = presetConfig.getInt("tierDurationMinutes");
		int tierCount = Integer.parseInt(presetConfig.getString("tierCount"));

		return new int[] { userIncrementPerTier, tierDurationMinutes, tierCount };
	}
}
