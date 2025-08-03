package org.openmrs.performance.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvTrimmer {

	/**
	 * Reads a CSV file, keeps only the records from the most recent N runs, and overwrites the file
	 * with the trimmed data.
	 *
	 * @param csvPath The path to the CSV file.
	 * @param maxRunsToKeep The maximum number of test runs to keep.
	 */
	public static void trimToRecentRuns(Path csvPath, int maxRunsToKeep) throws IOException {
		if (!Files.exists(csvPath)) {
			System.out.println("CSV file not found for trimming. Skipping.");
			return;
		}

		List<String> lines = Files.readAllLines(csvPath);
		if (lines.size() <= 1) {
			return;
		}

		String header = lines.get(0);
		List<String> dataLines = lines.subList(1, lines.size());

		Map<String, List<String>> runsByTimestamp = dataLines.stream()
		        .collect(Collectors.groupingBy(line -> line.split(",")[0]));

		int totalRuns = runsByTimestamp.size();
		if (totalRuns <= maxRunsToKeep) {
			System.out.println(
			    "CSV has " + totalRuns + " runs, which is within the limit of " + maxRunsToKeep + ". No trimming needed.");
			return;
		}

		System.out.println("Trimming CSV file. Found " + totalRuns + " runs, keeping the most recent " + maxRunsToKeep + ".");

		List<String> recentTimestamps = runsByTimestamp.keySet().stream().sorted(Comparator.reverseOrder())
		        .limit(maxRunsToKeep).toList();

		List<String> trimmedLines = new ArrayList<>();
		trimmedLines.add(header);

		for (String timestamp : recentTimestamps) {
			trimmedLines.addAll(runsByTimestamp.get(timestamp));
		}

		Files.write(csvPath, trimmedLines);
		System.out.println("Successfully trimmed CSV file to " + maxRunsToKeep + " runs.");
	}
}
