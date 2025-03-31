package org.openmrs.performance.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
