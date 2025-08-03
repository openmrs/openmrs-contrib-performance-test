package org.openmrs.performance.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrendDataParser {

	// 2. Create a logger instance for this class
	private static final Logger logger = LoggerFactory.getLogger(TrendDataParser.class);

	private static final Path TREND_CSV_PATH = Paths.get("performance-trends/requests-trend.csv");

	private static final String CSV_HEADER = "Timestamp,RequestName,Total,OK,KO,KO_Percent,Reqs_Per_Sec,Min,p50,p75,p95,p99,Max,Mean,StdDev";

	public static void main(String[] args) throws IOException {
		logger.info("Starting Gatling HTML report parsing for trend data...");

		Path gatlingReportsDir = Paths.get("target", "gatling");
		if (!Files.exists(gatlingReportsDir)) {
			logger.error("Gatling reports directory not found: {}", gatlingReportsDir);
			return;
		}

		Optional<Path> lastReportDir = Files.list(gatlingReportsDir).filter(Files::isDirectory)
		        .max(Comparator.comparingLong(p -> p.toFile().lastModified()));

		if (lastReportDir.isEmpty()) {
			logger.warn("No Gatling report directory found.");
			return;
		}

		Path reportHtmlPath = lastReportDir.get().resolve("index.html");
		if (!Files.exists(reportHtmlPath)) {
			logger.error("index.html not found in {}", lastReportDir.get());
			return;
		}
		logger.info("Parsing file: {}", reportHtmlPath);

		File reportHtmlFile = reportHtmlPath.toFile();
		Document doc = Jsoup.parse(reportHtmlFile, "UTF-8");

		Element statsTableBody = doc.selectFirst("#container_statistics_body > tbody");
		if (statsTableBody == null) {
			logger.error(
			    "Could not find the stats table body with ID 'container_statistics_body'. Report format may have changed.");
			return;
		}

		List<String> csvRows = new ArrayList<>();
		String timestamp = LocalDateTime.now().minusDays(4).format(DateTimeFormatter.ISO_DATE_TIME);

		for (Element row : statsTableBody.select("tr[data-parent=ROOT]")) {
			Elements cells = row.select("td");
			if (cells.size() == 14) {
				String requestName = Objects.requireNonNull(cells.get(0).selectFirst(".ellipsed-name")).text();
				List<String> rowValues = new ArrayList<>();
				rowValues.add(timestamp);
				rowValues.add("\"" + requestName + "\"");
				for (int i = 1; i < cells.size(); i++) {
					rowValues.add(cells.get(i).text());
				}
				csvRows.add(String.join(",", rowValues));
			}
		}

		if (!csvRows.isEmpty()) {
			writeToCsv(csvRows);
			logger.info("Successfully appended {} rows to {}", csvRows.size(), TREND_CSV_PATH);
			try {
				// The CsvTrimmer class would also benefit from using a logger
				CsvTrimmer.trimToRecentRuns(TREND_CSV_PATH, 30);
			}
			catch (IOException e) {
				// 3. Replace printStackTrace() with a structured error log
				logger.error("Failed to trim CSV file.", e);
			}
		} else {
			logger.warn("No request data found to append.");
		}
	}

	private static void writeToCsv(List<String> rows) throws IOException {
		File trendFile = TREND_CSV_PATH.toFile();
		Files.createDirectories(trendFile.getParentFile().toPath());
		boolean fileExists = trendFile.exists();

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(trendFile, true))) {
			if (!fileExists) {
				writer.write(CSV_HEADER);
				writer.newLine();
			}
			for (String row : rows) {
				writer.write(row);
				writer.newLine();
			}
		}
	}
}
