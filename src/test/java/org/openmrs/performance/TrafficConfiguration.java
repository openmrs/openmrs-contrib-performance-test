package org.openmrs.performance;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TrafficConfiguration {
	
	private static final Logger logger = Logger.getLogger(TrafficConfiguration.class.getName());
	
	private static final double DOCTOR_COUNT_RATIO = 0.5;
	
	private static final double CLERK_COUNT_RATIO = 0.5;
	
	private static final int DEFAULT_DURATION = 60 * 60;
	
	private int duration;
	
	private int totalActiveUserCount;
	
	private int activeDoctorCount;
	
	private int activeClerkCount;
	
	private TrafficConfiguration() {
		String loadSimulationType = System.getenv("LOAD_SIMULATION_TYPE");
		
		duration = DEFAULT_DURATION;
		
		if (loadSimulationType == null) {
			loadSimulationType = "standard";
		}
		
		setUserCountAndDurationBasedOnType(loadSimulationType);
		
		activeDoctorCount = (int) Math.ceil(totalActiveUserCount * DOCTOR_COUNT_RATIO);
		activeClerkCount = totalActiveUserCount - activeDoctorCount;
		
		logger.log(Level.INFO, "Running {0} load simulation with {1} concurrent users for {2} minutes.",
				new Object[] { loadSimulationType, totalActiveUserCount, duration / 60 });
	}
	
	private static class TrafficConfigurationHolder {
		
		private static final TrafficConfiguration INSTANCE = new TrafficConfiguration();
	}
	
	public static TrafficConfiguration getInstance() {
		return TrafficConfigurationHolder.INSTANCE;
	}
	
	private void setUserCountAndDurationBasedOnType(String loadSimulationType) {
		int totalActiveUserCount;
		int duration = DEFAULT_DURATION;
		switch (LoadSimulationType.fromString(loadSimulationType)) {
			case STANDARD -> totalActiveUserCount = 70;
			case HIGH -> totalActiveUserCount = 100;
			case PEAK -> totalActiveUserCount = 200;
			case DEV -> {
				String userCountEnv = System.getenv("ACTIVE_USERS");
				String durationMinutesEnv = System.getenv("DURATION_MINUTES");
				try {
					totalActiveUserCount = Integer.parseInt(userCountEnv);
					duration = Integer.parseInt(durationMinutesEnv) * 60;
				}
				catch (NumberFormatException e) {
					throw new IllegalArgumentException("Invalid user count or duration", e);
				}
			}
			default -> throw new IllegalArgumentException("Invalid load simulation type: " + loadSimulationType);
		}
		this.totalActiveUserCount = totalActiveUserCount;
		this.duration = duration;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public int getActiveDoctorCount() {
		return activeDoctorCount;
	}
	
	public int getActiveClerkCount() {
		return activeClerkCount;
	}
	
	private enum LoadSimulationType {
		STANDARD("standard"),
		HIGH("high"),
		PEAK("peak"),
		DEV("dev");
		
		private final String type;
		
		LoadSimulationType(String type) {
			this.type = type;
		}
		
		public static LoadSimulationType fromString(String type) {
			for (LoadSimulationType loadSimulationType : LoadSimulationType.values()) {
				if (loadSimulationType.type.equalsIgnoreCase(type)) {
					return loadSimulationType;
				}
			}
			throw new IllegalArgumentException("Unknown load simulation type: " + type);
		}
	}
}
