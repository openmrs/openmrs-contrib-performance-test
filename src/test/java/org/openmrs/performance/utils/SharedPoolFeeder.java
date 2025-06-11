package org.openmrs.performance.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

public class SharedPoolFeeder {

	public static final BlockingQueue<String> uuidPool = new LinkedBlockingQueue<>();

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
				String uuid = uuidPool.take();
				return Map.<String, Object> of("patient_uuid", uuid);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("Feeder interrupted", e);
			}
		}).iterator();
	}

	public static void returnUuid(String uuid) {
		if (uuid != null) {
			uuidPool.offer(uuid); // Non-blocking add back to pool
		}
	}

}
