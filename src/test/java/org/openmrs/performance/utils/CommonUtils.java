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
		return timeZone.getID();
	}

}
