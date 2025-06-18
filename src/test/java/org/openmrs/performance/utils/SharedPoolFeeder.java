package org.openmrs.performance.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class SharedPoolFeeder {

	public static final BlockingQueue<String> uuidPool = new LinkedBlockingQueue<>();
	private static final Logger logger = Logger.getLogger(SharedPoolFeeder.class.getName());

	static {
		try (Stream<String> lines = Files.lines(Paths.get("src/test/resources/patient_uuids.csv"))) {
			List<String> uuidList = lines.skip(1).toList();
			uuidPool.addAll(uuidList);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Iterator<Map<String, Object>> feeder() {
		return Stream.generate(() -> {
			try {
				// Try to take UUID, but with timeout (avoid infinite blocking)
				String uuid = uuidPool.poll(5, TimeUnit.SECONDS);

				if (uuid == null) {
					// Pool exhausted — log and fail
					logger.severe("UUID pool exhausted: no UUID available after waiting 5 seconds");
					throw new RuntimeException("UUID pool exhausted: no more UUIDs available");
				}

				return Map.<String, Object>of("patient_uuid", uuid);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.severe("Feeder interrupted while waiting for UUID: " + e.getMessage());
				throw new RuntimeException("Feeder interrupted", e);
			}
		}).iterator();
	}

	public static void returnUuid(String uuid) {
		if (uuid != null) {
			boolean offered = uuidPool.offer(uuid);
			if (!offered) {
				logger.warning("Failed to return UUID to pool: " + uuid);
			}
		}
	}

}
