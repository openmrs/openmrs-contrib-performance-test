package org.openmrs.performance.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CommonUtils {

	public static List<String> extractConceptIds(String response) {
		List<String> conceptIds = new ArrayList<>();

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonObject = objectMapper.readTree(response);
			JsonNode entries = jsonObject.get("entry");

			if (entries != null && entries.isArray()) {
				return StreamSupport.stream(entries.spliterator(), false).map(entry -> entry.get("resource"))
				        .filter(Objects::nonNull).map(resource -> resource.path("code").path("coding"))
				        .filter(coding -> coding.isArray() && !coding.isEmpty())
				        .map(coding -> coding.get(0).get("code").asText()).collect(Collectors.toList());
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return conceptIds;
	}

	public static List<String> extractPatientIds(String response) {
		List<String> patientIds = new ArrayList<>();
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonObject = objectMapper.readTree(response);
			JsonNode results = jsonObject.get("results");

			if (results != null && results.isArray()) {
				return StreamSupport.stream(results.spliterator(), false).map(result -> result.get("uuid").asText())
				        .collect(Collectors.toList());
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return patientIds;
	}

	public static String getCurrentDateTimeAsString() {
		ZonedDateTime now = ZonedDateTime.now();
		return formatDateTime(now);
	}

	public static String getAdjustedDateTimeAsString(int daysToAdjust, int minToAdjust) {
		ZonedDateTime adjustedDateTime = ZonedDateTime.now().plusDays(daysToAdjust).plusMinutes(minToAdjust);
		return formatDateTime(adjustedDateTime);
	}

	public static String getAdjustedDateTimeAsString(int daysToAdjust) {
		return getAdjustedDateTimeAsString(daysToAdjust, 0);
	}

	private static String formatDateTime(ZonedDateTime dateTime) {
		return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
	}

	public static String getCurrentTimeZone() {
		TimeZone timeZone = TimeZone.getDefault();
		return  timeZone.getID();
	}

}
