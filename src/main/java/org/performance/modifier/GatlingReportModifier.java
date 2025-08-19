package org.performance.modifier;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Iterator;

public class GatlingReportModifier {

	public static void main(String[] args) {
		try {
			// Step 1: Dynamically find the report directory
			Path reportDir = findReportDirectory("target/gatling", "openmrsclinic-*");

			if (reportDir == null) {
				System.err.println("Error: Could not find a Gatling report directory matching the pattern.");
				return;
			}

			System.out.println("Found Gatling report directory: " + reportDir);

			// Define paths for index.html and style.css inside the found directory
			Path htmlPath = reportDir.resolve("index.html");
			Path cssPath = reportDir.resolve("style/style.css");

			if (!Files.exists(htmlPath) || !Files.exists(cssPath)) {
				System.err.println("Error: index.html or style/style.css not found in the report directory.");
				return;
			}

			// --- Step 2: Modify index.html ---
			Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");
			addCustomButtons(doc);
			String cssRules = makeAssertionsCollapsible(doc); // This returns the CSS
			Files.writeString(htmlPath, doc.outerHtml(), StandardCharsets.UTF_8);
			System.out.println("Successfully modified Gatling report: " + htmlPath);

			// --- Step 3: Append styles to style.css ---
			if (cssRules != null && !cssRules.isBlank()) {
				appendCssToFile(cssPath, cssRules);
			}

		}
		catch (IOException e) {
			System.err.println("An error occurred while processing the files.");
			e.printStackTrace();
		}
	}

	/**
	 * Finds a directory within a base directory that matches a glob pattern.
	 *
	 * @param baseDir The directory to search in (e.g., "target/gatling")
	 * @param pattern The pattern to match (e.g., "openmrsclinic-*")
	 * @return The Path to the found directory, or null if not found.
	 */
	private static Path findReportDirectory(String baseDir, String pattern) throws IOException {
		Path gatlingPath = Paths.get(baseDir);
		if (!Files.isDirectory(gatlingPath)) {
			System.err.println("Base directory not found: " + baseDir);
			return null;
		}

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(gatlingPath, pattern)) {
			Iterator<Path> iterator = stream.iterator();
			if (iterator.hasNext()) {
				// Return the first directory that matches the pattern
				return iterator.next();
			}
		}
		return null; // No matching directory found
	}

	private static void addCustomButtons(Document doc) {
		Element docLink = doc.selectFirst("a.gatling-documentation");
		if (docLink != null) {
			// The href attributes have been updated to the specified URLs
			String reportSizesButton = "<a class=\"gatling-documentation\" href=\"https://o3-performance.openmrs"
			        + ".org/response_sizes\" target=\"_blank\">Response-Sizes</a>";
			String performanceTrendsButton = "<a class=\"gatling-documentation\" href=\"https://o3-performance.openmrs"
			        + ".org/performance-trends\" target=\"_blank\">Performance-Trends</a>";

			docLink.after(performanceTrendsButton);
			docLink.after(reportSizesButton);
			System.out.println("Added custom buttons with updated hyperlinks to index.html.");
		} else {
			System.err.println("Could not find the 'Documentation' button.");
		}
	}

	private static String makeAssertionsCollapsible(Document doc) {
		// This method remains unchanged
		Element assertionsContainer = doc.selectFirst("div.statistics.extensible-geant");
		if (assertionsContainer != null) {
			assertionsContainer.addClass("assertions-wrapper");

			String script = """
			        <script>
			            document.addEventListener('DOMContentLoaded', function() {
			                const assertionsTitle = document.querySelector('.assertions-wrapper .title');
			                if (assertionsTitle) {
			                    assertionsTitle.addEventListener('click', () => {
			                        assertionsTitle.parentElement.classList.toggle('collapsed');
			                    });
			                }
			            });
			        </script>
			        """;
			doc.body().append(script);
			System.out.println("Enabled collapsible assertions in index.html.");

			return """

			        /* --- Custom styles for Collapsible Assertions --- */
			        .assertions-wrapper .title {
			            cursor: pointer;
			            position: relative;
			            user-select: none;
			        }
			        .assertions-wrapper .title::after {
			            content: '▼';
			            position: absolute;
			            right: 15px;
			            font-size: 0.8em;
			            transition: transform 0.2s ease-in-out;
			        }
			        .assertions-wrapper.collapsed .title::after {
			            transform: rotate(-90deg);
			        }
			        .assertions-wrapper.collapsed > .statistics-in {
			            display: none;
			        }
			        """;
		} else {
			System.err.println("Could not find the assertions container in index.html.");
			return "";
		}
	}

	private static void appendCssToFile(Path cssPath, String cssContent) throws IOException {
		// This method remains unchanged
		String fileContent = Files.readString(cssPath, StandardCharsets.UTF_8);
		if (fileContent.contains("/* --- Custom styles for Collapsible Assertions --- */")) {
			System.out.println("CSS rules already exist in style.css. Skipping.");
			return;
		}
		Files.writeString(cssPath, cssContent, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
		System.out.println("Successfully appended new styles to " + cssPath);
	}
}
