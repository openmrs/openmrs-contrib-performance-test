package org.openmrs.performance.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

}
