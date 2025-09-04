package org.openmrs.performance.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;

public class ResponseSizeLogger {

	private static final String LOG_FILE_PATH = System.getProperty("user.dir") + "/response-sizes/response_sizes.csv";

	private static final PrintWriter writer;

	static {
		System.out.println("ResponseSizeLogger: CSV file will be created at: " + LOG_FILE_PATH);

		try {
			File logFile = new File(LOG_FILE_PATH);
			File parentDir = logFile.getParentFile();

			if (parentDir != null && !parentDir.exists()) {
				if (!parentDir.mkdirs()) {
					throw new IOException("Failed to create parent directory: " + parentDir);
				}
			}

			FileWriter fileWriter = new FileWriter(logFile, false);
			writer = new PrintWriter(fileWriter);

			writer.println("Timestamp,RequestName,ResponseSize(Bytes)");

		}
		catch (IOException e) {
			throw new RuntimeException("Failed to initialize ResponseSizeLogger.", e);
		}
	}

	public static synchronized void log(String requestName, int responseSize) {
		writer.println(Instant.now() + ",\"" + requestName + "\"," + responseSize);
	}

	public static void close() {
		if (writer != null) {
			System.out.println("Closing ResponseSizeLogger CSV writer.");
			writer.flush();
			writer.close();
		}
	}
}
