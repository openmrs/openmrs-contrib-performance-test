package org.openmrs.performance.utils;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCommonUtils {

	@Test
	public void testExtractConceptIds() {
		String response = """
		        {
		          "resourceType": "Bundle",
		          "entry": [
		            {
		              "resource": {
		                "code": {
		                  "coding": [
		                    {
		                      "code": "123"
		                    }
		                  ]
		                }
		              }
		            },
		            {
		              "resource": {
		                "code": {
		                  "coding": [
		                    {
		                      "code": "456"
		                    }
		                  ]
		                }
		              }
		            }
		          ]
		        }
		        """;

		List<String> expected = List.of("123", "456");

		assertEquals(CommonUtils.extractConceptIds(response), expected);
	}

	@Test
	public void testGetCurrentDateTimeAsString_Format() {
		String currentDateTime = CommonUtils.getCurrentDateTimeAsString();
		assertTrue(currentDateTime.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}(Z|[+-]\\d{2}:\\d{2})$"),
		    "DateTime format is incorrect");
	}

	@Test
	public void testGetAdjustedDateTimeAsString_PositiveAdjustment() {
		int daysToAdjust = 5;
		String adjustedDateTimeStr = CommonUtils.getAdjustedDateTimeAsString(daysToAdjust);
		ZonedDateTime adjustedDateTime = ZonedDateTime.parse(adjustedDateTimeStr,
		    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
		ZonedDateTime expectedDateTime = ZonedDateTime.now().plusDays(daysToAdjust);
		assertEquals(0, Math.abs(ChronoUnit.SECONDS.between(adjustedDateTime, expectedDateTime)),
		    "Date adjustment is incorrect");
	}
}
